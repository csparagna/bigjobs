package bigjobs;

import java.util.Map;

public interface Job {
    JobStatus getStatus();
    String getJobId();
    String getType();
    Map<String,String> getAttributes();
    String getTechnology();

    void remove() throws BJException;

    void cancel() throws BJException;

    }

//PENDING: The job is scheduled and waiting to be run.
//RUNNING: The job is in progress.
//DONE
