package bigjobs.bigquery;

import com.google.cloud.bigquery.*;
import bigjobs.BJException;
import bigjobs.Job;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@ToString
public class BigQueryJob implements Job {

    public static final String TECHNOLOGY = "BigQuery";

    BigQuery bigquery;
    BigQuery getBigQuery(){
        if (bigquery==null) bigquery = BigQueryOptions.newBuilder().setProjectId(gcpProjectId).build().getService();
        return  bigquery;
    }


    @Getter
    @Setter(AccessLevel.PROTECTED)
    String type;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    bigjobs.JobStatus status;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    Map<String,String> attributes = new HashMap<>();

    @Override
    public String getJobId() { return "gcp/"+gcpProjectId+"/"+getBigQueryJobId().getJob(); }

    public JobId getBigQueryJobId() {
        return bigQueryJobId;
    }

    @Override
    public String getTechnology() { return TECHNOLOGY; }


    @Getter
    @Setter(AccessLevel.PROTECTED)
    String gcpProjectId;

    @Override
    public void cancel() throws BJException {
        getBigQuery().cancel( getBigQueryJobId() );
    }

    @Override
    public void remove() throws BJException {
        boolean deleted = getBigQuery().delete(getBigQueryJobId());
        System.out.println("removed from bq "+deleted + " "+getBigQueryJobId());
    }

    @Setter(AccessLevel.PROTECTED)
    JobId bigQueryJobId;


    public static BigQueryJob create(com.google.cloud.bigquery.Job job){
        JobConfiguration configuration = job.getConfiguration();
        Map<String,String> labels = null;
        if (configuration instanceof QueryJobConfiguration){ labels = ((QueryJobConfiguration) configuration).getLabels(); }
        else if (configuration instanceof LoadJobConfiguration){ labels = ((LoadJobConfiguration) configuration).getLabels(); }
        else if (configuration instanceof ExtractJobConfiguration){ labels = ((ExtractJobConfiguration) configuration).getLabels(); }
        else if (configuration instanceof CopyJobConfiguration){ labels = ((CopyJobConfiguration) configuration).getLabels(); }


        bigjobs.JobStatus status = null;
        com.google.cloud.bigquery.JobStatus.State state = job.getStatus().getState();
        if (state==com.google.cloud.bigquery.JobStatus.State.DONE){ status = bigjobs.JobStatus.DONE; }
        else if (state==com.google.cloud.bigquery.JobStatus.State.PENDING){ status = bigjobs.JobStatus.PENDING; }
        else if (state==com.google.cloud.bigquery.JobStatus.State.RUNNING){ status = bigjobs.JobStatus.RUNNING; }

        BigQueryJob bigQueryJob = new BigQueryJob();
        if (labels!=null) bigQueryJob.getAttributes().putAll(labels);
        bigQueryJob.setGcpProjectId( job.getBigQuery().getOptions().getProjectId() );
        bigQueryJob.setStatus(status);
        bigQueryJob.setBigQueryJobId( job.getJobId() );
        return bigQueryJob;
    }


}


/*ùLoad jobs.
Copy jobs.
Export (extract) jobs.
Query jobs.*/

//    JobId jobId = JobId.newBuilder().setJob(jobName).build();
//    JobInfo jobInfo = JobInfo.of(jobId, loadJobConfiguration);
//BigQuery bigquery = BigQueryOptions.newBuilder().setProjectId(gcpProjectId).build().getService();
//    com.google.cloud.bigquery.Job job = bigquery.create(jobInfo);
/*
      // generate gcp file uri
        String gcpFileUri = "generateFinalGcpFileUri(this.gcpBucketUri, reportId)";

        // define the load job
        String jobName = "generateLoadJobName(reportId)";
        String gcpProjectId = "generateLoadJobName(reportId)";
        Map<String, String> labelMap = new HashMap<>();

        BigQuery bigquery = BigQueryOptions.newBuilder().setProjectId(gcpProjectId).build().getService();

        LoadJobConfiguration loadJobConfiguration = LoadJobConfiguration
                .newBuilder(TableId.of("","",""), gcpFileUri)
                .setLabels(labelMap)
                .setAutodetect(true)
                .setFormatOptions(FormatOptions.csv())
                .build();
        JobId jobId = JobId.newBuilder().setJob(jobName).build();
        JobInfo jobInfo = JobInfo.of(jobId, loadJobConfiguration);
        Job job = bigquery.create(jobInfo);

    }
 */