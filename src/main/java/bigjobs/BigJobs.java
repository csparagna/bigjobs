package bigjobs;

import java.util.List;
import java.util.function.Predicate;

/**
 *
 */
public class BigJobs {


    BigJobsJobsManager bigJobsJobsManager = new BigJobsJobsManager(this);

    public void addPlugin(BigJobsPlugin updater){
        updater.register(bigJobsJobsManager);
        updater.start();
    }


    public List<Job> getJobs(){ return bigJobsJobsManager.jobsSnapshot; }

    public void add(Trigger trigger) { bigJobsJobsManager.add(trigger); }
    public void remove(Trigger trigger) { bigJobsJobsManager.remove(trigger); }


    /** Migliorare: voglio poter gestire differenti tipologie di evento senza ciclare su tutti i trigger */
    public TriggerBuilder on(Predicate<Event> event){
        TriggerBuilder triggerBuilder = new TriggerBuilder();
        triggerBuilder.withBigJobs(this);
        triggerBuilder.on(event);
        return triggerBuilder;
    }

}



