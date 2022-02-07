package bigjobs.bigquery;

import bigjobs.JobStatus;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Job;
import bigjobs.BigJobsJobsManager;
import bigjobs.BigJobsUpdater;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BigQueryUpdater implements BigJobsUpdater {

    String projectId;
    public BigQueryUpdater(String projectId){
        this.projectId = projectId;
    }

    BigJobsJobsManager manager;

    @Override
    public void setJobsManager(BigJobsJobsManager manager) {
        this.manager = manager;
    }

    JobStatus status = JobStatus.DONE;
    Thread thread;


    public void start(){
        thread = new Thread(() -> {
            if (status!=JobStatus.DONE){ return; }
            status = JobStatus.RUNNING;
            while (status == JobStatus.RUNNING){
                poll(projectId);
                System.out.println("waiting next poll");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            status = JobStatus.DONE;
        });
        thread.start();
    }
    public void stop(){
        status = JobStatus.PENDING;
    }

    public void poll(String gpcProjectId){
        List<Job> jobs = listProjectJobs(gpcProjectId);
        List<bigjobs.Job> bjJobs = jobs
                .stream()
                .map(job -> { return BigQueryJob.create(job); } )
                .collect(Collectors.toList());

        manager.update(bjJobs, job -> {
            if (job instanceof BigQueryJob){
                BigQueryJob bigQueryJob = (BigQueryJob) job;
                return bigQueryJob.getGcpProjectId().equals(gpcProjectId)
                        && BigQueryJob.TECHNOLOGY.equals(job.getType());
            }
            return false;
        } );
    }



    public List<Job> listProjectJobs(String gcpProjectId) {
        BigQuery bigquery = BigQueryOptions.newBuilder().setProjectId(gcpProjectId).build().getService();

        List<Job> jobList = new LinkedList<>();
        com.google.api.gax.paging.Page<Job> jobPage = bigquery.listJobs();
        if (jobPage != null) {
            for (Job job : jobPage.getValues()) {
                if (job.getJobId().getProject().equals(gcpProjectId)) {
                    jobList.add(job);
                }
            }
        }
        return jobList;
    }


}
