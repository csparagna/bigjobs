package bigjobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class BigJobs {


    List<Job> jobsSnapshot = new ArrayList<>();
    List<Trigger> triggers = new ArrayList<>();
    BigJobsJobsManager bigJobsJobsManager = new BigJobsJobsManager(this);

    public void register(BigJobsUpdater updater){
        updater.setJobsManager(bigJobsJobsManager);
        updater.start();
    }

    private synchronized void applyUpdate(Job job, List<Job> toCheck){
        String jobId = job.getJobId();
        System.out.println("applyUpdate to jobId: "+jobId);
        Optional<Job> oldJobOpt = toCheck.stream().filter(job1 -> { return job1.getJobId().equals(jobId); }).findFirst();

        if (oldJobOpt.isPresent()) {
            // aggiorna
            Job oldJob = oldJobOpt.get();
            toCheck.remove(oldJob);
            jobsSnapshot.remove(oldJob);
            jobsSnapshot.add(job);
            if (!oldJob.getStatus().equals(job.getStatus())) {
                JobEvent event = JobEvent.builder()
                        .jobOldValue(oldJob)
                        .jobActualValue(job)
                        .type(EventType.CHANGED_JOB_STATUS)
                        .build();
                fire(event);
            }
        } else {
                // inserisci
                jobsSnapshot.add(job);
                JobEvent event = JobEvent.builder()
                        .jobOldValue(null)
                        .jobActualValue(job)
                        .type(EventType.CHANGED_JOB_STATUS)
                        .build();
                fire(event);
            }

        // se DONE rimuovere
        if (job.getStatus().equals(JobStatus.DONE)){
            try {
                job.remove();
                jobsSnapshot.remove(job);
            } catch (BJException e) { e.printStackTrace(); }
        }

    }

    private synchronized void removed(Job job){
        jobsSnapshot.remove(job);
        JobEvent event = JobEvent.builder()
                .jobOldValue(job)
                .jobActualValue(null)
                .type(EventType.REMOVED)
                .build();
        fire(event);
    }


    protected void update(List<Job> jobsUpdated, Predicate<Job> jobFilter){
        i++;
        System.out.println(i+" jobsUpdated: "+jobsUpdated);
        List<Job> toCheck = jobsSnapshot.stream().filter(jobFilter).collect(Collectors.toList());

        List<Job> untouched = new ArrayList<>(jobsUpdated);
        untouched.removeAll(untouched);

        for (Job jobUpdated: jobsUpdated){ applyUpdate(jobUpdated, toCheck); }
        for (Job jobToRemove: toCheck){ removed(jobToRemove); }

        System.out.println(i+" jobsSnapshot: "+jobsSnapshot);
    }


    int i = 0;

    // implementazione semplicistica
    protected void fire(Event event){
        System.out.println("fire: "+event);
        synchronized (triggers) {
            triggers.forEach(trigger -> { if (trigger.shouldFire(event)) trigger.fire(); } );
        }
    }


    /** Migliorare: voglio poter gestire differenti tipologie di evento senza ciclare su tutti i trigger */
    public TriggerBuilder on(Predicate<Event> event){
        TriggerBuilder triggerBuilder = new TriggerBuilder();
        triggerBuilder.withBigJobs(this);
        triggerBuilder.on(event);
        return triggerBuilder;
    }

    public void add(Trigger trigger) { triggers.add(trigger); }
    public void remove(Trigger trigger) { synchronized (triggers) { triggers.remove(trigger); } }
}



