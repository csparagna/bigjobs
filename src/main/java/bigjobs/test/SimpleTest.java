package bigjobs.test;

import bigjobs.BigJobs;
import bigjobs.Events;
import bigjobs.Jobs;
import com.google.cloud.bigquery.*;
import bigjobs.bigquery.BigQueryUpdater;

import java.util.HashMap;
import java.util.Map;

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
public class SimpleTest {
    public static void main(String[] args){

        // da cloud storage a bigquery
        // da bigquery a bigquery

        String projectId = "smart-reporting-332510";
        String file = "gs://smart-repo-storage/ip_security.csv";

        BigJobs bigJobs = new BigJobs();

        BigQueryUpdater bigQueryUpdater = new BigQueryUpdater(projectId);
        bigJobs.register(bigQueryUpdater);

        createLoadJob(bigJobs, projectId,file);
        bigJobs .on(Events.onDone(Jobs.withAttribute("type","example")))
                .then( () -> {
                    createCopyJob(bigJobs,projectId);
                } )
                .build();
    }

    public static void createCopyJob(BigJobs bigJobs, String gcpProjectId){
        String jobName = "load_"+System.currentTimeMillis();
        String dataset = "sample";
        String table = "victory";
        String table2 = "victoryyy";

        Map<String,String> labelMap = new HashMap<>();
        labelMap.put("type","other");
        labelMap.put("autodelete","true");

        BigQuery bigquery = BigQueryOptions.newBuilder().setProjectId(gcpProjectId).build().getService();


        CopyJobConfiguration copyJobConfiguration = CopyJobConfiguration.
                newBuilder(
                TableId.of(gcpProjectId,dataset,table),
                TableId.of(gcpProjectId,dataset,table2)
        ).setLabels(labelMap).setCreateDisposition(JobInfo.CreateDisposition.CREATE_IF_NEEDED)
                .build();
        JobId jobId = JobId.newBuilder().setJob(jobName).build();
        JobInfo jobInfo = JobInfo.of(jobId, copyJobConfiguration);
        Job job = bigquery.create(jobInfo);
    }

    public static void createLoadJob(BigJobs bigJobs, String gcpProjectId, String gcpFileUri){
        String jobName = "load_"+System.currentTimeMillis();
        String dataset = "sample";
        String table = "victory";

        Map<String,String> labelMap = new HashMap<>();
        labelMap.put("type","example");
        labelMap.put("autodelete","true");

        BigQuery bigquery = BigQueryOptions.newBuilder().setProjectId(gcpProjectId).build().getService();
        LoadJobConfiguration loadJobConfiguration = LoadJobConfiguration
                .newBuilder(TableId.of(gcpProjectId,dataset,table), gcpFileUri)
                .setLabels(labelMap)
                .setAutodetect(true)
                .setCreateDisposition(JobInfo.CreateDisposition.CREATE_IF_NEEDED)
                .setWriteDisposition(JobInfo.WriteDisposition.WRITE_APPEND)
                .setFormatOptions(FormatOptions.csv())
                .build();
        JobId jobId = JobId.newBuilder().setJob(jobName).build();
        JobInfo jobInfo = JobInfo.of(jobId, loadJobConfiguration);
        Job job = bigquery.create(jobInfo);
    }
}
