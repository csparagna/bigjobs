package bigjobs;

import bigjobs.repository.InMemoryJobRepo;
import bigjobs.repository.JobRepo;
import bigjobs.repository.UnitOfWork;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
public class BigJobsJobsManager {

    List<Trigger> triggers = new ArrayList<>();

    JobRepo jobRepo = new InMemoryJobRepo();

    public BigJobsJobsManager(BigJobs bigJobs){
        this.bigJobs = bigJobs;
    }

    @Getter
    BigJobs bigJobs;


    public void add(Trigger trigger) { triggers.add(trigger); }
    public void remove(Trigger trigger) { synchronized (triggers) { triggers.remove(trigger); } }


    private synchronized void applyUpdate(Job job, List<Job> toCheck){
        String jobId = job.getJobId();
        System.out.println("applyUpdate to jobId: "+jobId);
        Optional<Job> oldJobOpt = toCheck.stream().filter(job1 -> job1.getJobId().equals(jobId)).findFirst();

        UnitOfWork unitOfWork = jobRepo.unitOfWork();

        unitOfWork.registerUpsert(job);
        if (oldJobOpt.isPresent()) {
            // aggiorna
            Job oldJob = oldJobOpt.get();
            toCheck.remove(oldJob);
            if (!oldJob.getStatus().equals(job.getStatus())) {
                JobEvent event = JobEvent.builder()
                        .jobOldValue(oldJob)
                        .jobActualValue(job)
                        .jobEventType(JobEventType.CHANGED_JOB_STATUS)
                        .build();
                fire(event);
            }
        } else {
            // inserisci
            JobEvent event = JobEvent.builder()
                    .jobOldValue(null)
                    .jobActualValue(job)
                    .jobEventType(JobEventType.CHANGED_JOB_STATUS)
                    .build();
            fire(event);
        }

        // se DONE rimuovere
        if (job.getStatus().equals(JobStatus.DONE)){
            try {
                job.remove();
                unitOfWork.registerRemove(jobId);
            } catch (BJException e) { e.printStackTrace(); }
        }

        unitOfWork.commit();
    }

    private synchronized void remove(Job job){
        jobRepo.remove(job.getJobId());
        JobEvent event = JobEvent.builder()
                .jobOldValue(job)
                .jobActualValue(null)
                .jobEventType(JobEventType.REMOVED)
                .build();
        fire(event);
    }


    public void update(List<Job> jobsUpdated, Predicate<Job> jobFilter){
        List<Job> toCheck = StreamSupport.stream(jobRepo.spliterator(), false).filter(jobFilter).collect(Collectors.toList());

        for (Job jobUpdated: jobsUpdated){ applyUpdate(jobUpdated, toCheck); }
        for (Job jobToRemove: toCheck){ remove(jobToRemove); }

    }



    // implementazione semplicistica
    protected void fire(Event event){
        synchronized (triggers) {
            triggers.forEach(trigger -> { if (trigger.shouldFire(event)) trigger.fire(event); } );
        }
    }


}
