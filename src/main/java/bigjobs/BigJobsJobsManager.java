package bigjobs;

import lombok.Getter;

import java.util.List;
import java.util.function.Predicate;

public class BigJobsJobsManager {

    public BigJobsJobsManager(BigJobs bigJobs){
        this.bigJobs = bigJobs;
    }

    @Getter
    BigJobs bigJobs;

    public void update(List<Job> jobsUpdated, Predicate<Job> jobFilter){
        bigJobs.update(jobsUpdated,jobFilter);
    }

}
