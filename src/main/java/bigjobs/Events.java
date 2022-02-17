package bigjobs;

import java.util.function.Predicate;


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
