package bigjobs.bigquery;

import com.google.cloud.bigquery.*;

import java.util.LinkedList;
import java.util.List;

// periodicamenrte deve fare polling
public class BigQueryManager {


    public List<Job> listProjectJobs(String gcpProjectId) {
        BigQuery bigquery = BigQueryOptions.newBuilder().setProjectId(gcpProjectId).build().getService();

        List<Job> jobList = new LinkedList<>();
        com.google.api.gax.paging.Page<Job> jobPage = bigquery.listJobs();
        if (jobPage != null) {
            for (Job job : jobPage.getValues()) {
                if (job.getJobId().getProject().equals(gcpProjectId)) {
                    jobList.add(job);
                    //boolean deleted = bigquery.delete(job.getJobId());
                    //System.out.println("deleted "+deleted);
                }
            }
        }
        return jobList;
    }

    public void deleteProjectJobsDone(String gcpProjectId) {
        BigQuery bigquery = BigQueryOptions.newBuilder().setProjectId(gcpProjectId).build().getService();
        listProjectJobs(gcpProjectId)
                .stream()
                .filter(job -> job.getStatus().getState().equals(JobStatus.State.DONE))
                .forEach(job -> bigquery.delete(job.getJobId()));
    }

    public void createJob(String gcpProjectId, String jobName, JobConfiguration jobConfiguration) {
        BigQuery bigquery = BigQueryOptions.newBuilder().setProjectId(gcpProjectId).build().getService();
        JobId jobId = JobId.newBuilder().setJob(jobName).build();
        JobInfo jobInfo = JobInfo.of(jobId, jobConfiguration);
        Job job = bigquery.create(jobInfo);
    }

}
