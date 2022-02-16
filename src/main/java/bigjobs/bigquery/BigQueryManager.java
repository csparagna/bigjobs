package bigjobs.bigquery;

import com.google.cloud.bigquery.*;

import java.util.LinkedList;
import java.util.List;

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
