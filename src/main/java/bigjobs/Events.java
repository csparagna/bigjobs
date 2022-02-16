package bigjobs;

import java.util.function.Predicate;

public class Events {

    public static Predicate<Event> onChangedStatus(Predicate<Job> jobPredicate){
        return event -> {
            if (! (event instanceof JobEvent)){ return false; }
            JobEvent jobEvent = (JobEvent) event;
            return jobEvent.getJobEventType()== JobEventType.CHANGED_JOB_STATUS;
            };
    }

    public static Predicate<Event> onDone(Predicate<Job> jobPredicate){
        return event -> {
            if (! (event instanceof JobEvent)){ return false; }
            JobEvent jobEvent = (JobEvent) event;
            return jobEvent.getJobEventType()== JobEventType.CHANGED_JOB_STATUS && jobEvent.jobActualValue.getStatus()==JobStatus.DONE
                && jobPredicate.test( jobEvent.jobActualValue );
        };
    }


}
