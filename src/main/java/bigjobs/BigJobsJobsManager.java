package bigjobs;

import bigjobs.repository.InMemoryJobRepo;
import bigjobs.repository.JobRepo;
import bigjobs.repository.UnitOfWork;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.StreamSupport.stream;

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
    private final JobRepo jobRepo = new InMemoryJobRepo(this::fire);
    private final List<Trigger> triggers = new ArrayList<>();

    public void add(Trigger trigger) {
        triggers.add(trigger);
    }

    public void remove(Trigger trigger) {
        synchronized (triggers) {
            triggers.remove(trigger);
        }
    }

    private synchronized void applyUpdate(Job job, List<Job> toCheck) {
        System.out.printf("applyUpdate to jobId: %s\n", job.getJobId());
        UnitOfWork unitOfWork = jobRepo.unitOfWork();
        unitOfWork.registerUpsert(job);
        if (jobRepo.byId(job.getJobId()).isPresent()) {
            toCheck.remove(job);
        }

        if (job.getStatus().equals(JobStatus.DONE)) {
            try {
                job.remove();
                unitOfWork.registerRemove(job.getJobId());
            } catch (BJException e) {
                e.printStackTrace();
            }
        }

        unitOfWork.commit();
    }

    private synchronized void remove(Job job) {
        this.jobRepo.remove(job.getJobId());
    }

    public void update(List<Job> jobsUpdated, Predicate<Job> jobFilter) {
        List<Job> toCheck = getJobStream().filter(jobFilter).collect(Collectors.toList());

        for (Job jobUpdated: jobsUpdated){ applyUpdate(jobUpdated, toCheck); }
        for (Job jobToRemove: toCheck){ remove(jobToRemove); }
    }

    private Stream<Job> getJobStream() {
        return stream(this.jobRepo.spliterator(), false);
    }

    // implementazione semplicistica
    protected void fire(Event event) {
        synchronized (this.triggers) {
            this.triggers.forEach(trigger -> {
                if (trigger.shouldFire(event)) trigger.fire(event);
            });
        }
    }

    public List<Job> getJobs() {
        return unmodifiableList(getJobStream().collect(Collectors.toList()));
    }
}
