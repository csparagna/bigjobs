package bigjobs.bigquery2;

import bigjobs.BigJobsJobsManager;
import bigjobs.BigJobsPlugin;
import bigjobs.Job;
import bigjobs.JobStatus;
import bigjobs.bigquery.BigQueryJob;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Singular;
import lombok.extern.log4j.Log4j2;
import bigjobs.pubsub.PubSubAdapter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Pub/Sub-driven BigQuery job updates for BigJobs.
 *
 * This plugin subscribes to one or more Pub/Sub subscriptions that receive BigQuery job completion
 * events (typically via Cloud Logging sinks) and updates the in-memory snapshot in BigJobs.
 */
@Log4j2
public class BigQueryPubSubPlugin implements BigJobsPlugin, AutoCloseable {

    private final List<String> subscriptions; // formats accepted: fully-qualified or project+name
    private final Duration startupTimeout;

    private volatile BigJobsJobsManager manager;
    private final PubSubAdapter adapter;
    private AutoCloseable adapterHandle;
    private final Gson gson = new Gson();

    @Builder
    public BigQueryPubSubPlugin(@Singular List<String> subscriptions, Duration startupTimeout, PubSubAdapter adapter) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            throw new IllegalArgumentException("At least one Pub/Sub subscription must be configured");
        }
        if (adapter == null) {
            throw new IllegalArgumentException("PubSubAdapter must be provided");
        }
        this.subscriptions = new ArrayList<>(subscriptions);
        this.startupTimeout = startupTimeout != null ? startupTimeout : Duration.ofSeconds(10);
        this.adapter = adapter;
    }

    @Override
    public void register(BigJobsJobsManager manager) {
        this.manager = manager;
    }

    @Override
    public void start() {
        if (manager == null) {
            throw new IllegalStateException("register(manager) must be called before start()");
        }
        try {
            this.adapterHandle = adapter.start(subscriptions, this::onMessageData);
            log.info("BigQueryPubSubPlugin started with {} subscription(s)", subscriptions.size());
        } catch (Exception e) {
            log.error("Error starting PubSubAdapter: {}", e.getMessage(), e);
            throw new IllegalStateException("Cannot start PubSubAdapter", e);
        }
    }

    private void onMessageData(String data) {
        try {
            if (data == null || data.isEmpty()) {
                log.debug("Skipping empty Pub/Sub message");
                return;
            }
            JsonObject root = gson.fromJson(data, JsonObject.class);
            BigQueryEvent evt = parseEvent(root);
            if (evt == null || evt.projectId == null || evt.jobId == null) {
                log.warn("Could not parse projectId/jobId from message payload");
                return;
            }
            JobStatus status = evt.status != null ? evt.status : JobStatus.DONE;
            BigQueryJob mapped = BigQueryJob.fromEvent(evt.projectId, evt.jobId, status, evt.labels);

            Predicate<Job> predicate = job -> {
                if (job instanceof BigQueryJob) {
                    BigQueryJob bq = (BigQueryJob) job;
                    return Objects.equals(bq.getGcpProjectId(), evt.projectId)
                            && Objects.equals(bq.getBigQueryJobId().getJob(), evt.jobId)
                            && Objects.equals(job.getTechnology(), BigQueryJob.TECHNOLOGY);
                }
                return false;
            };

            manager.update(java.util.Collections.singletonList(mapped), predicate);
        } catch (Throwable t) {
            log.error("Error handling Pub/Sub message: {}", t.getMessage(), t);
        }
    }

    // Minimal tolerant parser for BigQuery job completion events via Cloud Logging → Pub/Sub
    private BigQueryEvent parseEvent(JsonObject root) {
        BigQueryEvent evt = new BigQueryEvent();
        // Try common locations for projectId
        evt.projectId = getString(root, "resource", "labels", "project_id");
        if (evt.projectId == null) evt.projectId = getString(root, "resource", "labels", "projectId");
        if (evt.projectId == null) evt.projectId = getString(root, "protoPayload", "resourceName"); // rarely helpful

        // Job id and status from serviceData.jobCompletedEvent
        JsonObject svcData = getObj(root, "protoPayload", "serviceData");
        JsonObject jobCompleted = svcData != null ? getObj(svcData, "jobCompletedEvent") : null;
        if (jobCompleted != null) {
            JsonObject job = getObj(jobCompleted, "job");
            JsonObject jobName = job != null ? getObj(job, "jobName") : null;
            if (jobName != null) {
                String jid = getString(jobName, "jobId");
                if (jid != null) evt.jobId = jid;
                String pj = getString(jobName, "projectId");
                if (pj != null) evt.projectId = pj;
            }
            // If there is an errorResult field, consider it a terminal failure; otherwise DONE
            if (getObj(jobCompleted, "jobStatus") != null) {
                JsonObject js = getObj(jobCompleted, "jobStatus");
                String state = getString(js, "state");
                if ("DONE".equalsIgnoreCase(state)) evt.status = JobStatus.DONE;
                else if ("PENDING".equalsIgnoreCase(state)) evt.status = JobStatus.PENDING;
                else if ("RUNNING".equalsIgnoreCase(state)) evt.status = JobStatus.RUNNING;
            } else if (jobCompleted.has("errorResult")) {
                // No explicit FAILED state in our enum; mark DONE to trigger cleanup, or leave RUNNING.
                evt.status = JobStatus.DONE;
            } else {
                evt.status = JobStatus.DONE;
            }
        }

        // Labels are not always present in audit logs; leave null if missing.
        // If present in protoPayload.metadata.jobChange.after or job configuration, map as attributes.
        JsonObject meta = getObj(root, "protoPayload", "metadata");
        JsonObject after = meta != null ? getObj(meta, "jobChange") : null;
        JsonObject afterJob = after != null ? getObj(after, "after") : null;
        JsonObject config = afterJob != null ? getObj(afterJob, "jobConfiguration") : null;
        JsonObject labels = config != null ? getObj(config, "labels") : null;
        if (labels != null && labels.entrySet() != null) {
            java.util.HashMap<String,String> map = new java.util.HashMap<>();
            for (Map.Entry<String, JsonElement> e : labels.entrySet()) {
                map.put(e.getKey(), e.getValue().getAsString());
            }
            evt.labels = map;
        }
        return evt;
    }

    private static JsonObject getObj(JsonObject obj, String... path){
        JsonObject cur = obj;
        for (String p : path){
            if (cur == null || !cur.has(p) || !cur.get(p).isJsonObject()) return null;
            cur = cur.getAsJsonObject(p);
        }
        return cur;
    }
    private static String getString(JsonObject obj, String... path){
        if (obj == null) return null;
        if (path.length == 0) return null;
        JsonObject cur = obj;
        for (int i=0;i<path.length-1;i++){
            String p = path[i];
            if (!cur.has(p) || !cur.get(p).isJsonObject()) return null;
            cur = cur.getAsJsonObject(p);
        }
        String leaf = path[path.length-1];
        if (!cur.has(leaf)) return null;
        JsonElement el = cur.get(leaf);
        if (el.isJsonPrimitive()) return el.getAsString();
        return null;
    }

    @Override
    public void close() {
        try {
            if (adapterHandle != null) {
                adapterHandle.close();
                adapterHandle = null;
            }
        } catch (Exception ignored) { }
        log.info("BigQueryPubSubPlugin stopped");
    }

    private static class BigQueryEvent {
        String projectId;
        String jobId;
        JobStatus status;
        Map<String,String> labels;
    }
}
