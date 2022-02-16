package bigjobs.bigquery;

import bigjobs.JobStatus;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Job;
import bigjobs.BigJobsJobsManager;
import bigjobs.BigJobsPlugin;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This file is part of BigJobs.
 *
 *     BigJobs is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     BigJobs is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with BigJobs.  If not, see <http://www.gnu.org/licenses/>.
 */
@Log4j2
public class BigQueryUpdater implements BigJobsPlugin {

    String projectId;
    public BigQueryUpdater(String projectId){
        this.projectId = projectId;
    }

    BigJobsJobsManager manager;

    @Override
    public void register(BigJobsJobsManager manager) {
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
                assert bigQueryJob.gcpProjectId!=null;
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
