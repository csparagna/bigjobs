/*
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
package bigjobs.repository;

import bigjobs.Event;
import bigjobs.Job;
import bigjobs.JobEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static bigjobs.JobEventType.CHANGED_JOB_STATUS;
import static bigjobs.JobEventType.REMOVED;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableCollection;

/**
 * Thread-safe in-memory implementation of {@link JobRepo}
 */
public final class InMemoryJobRepo implements JobRepo {
    private final ConcurrentMap<String, Job> map;
    private final Collection<Consumer<Event>> eventsConsumers;

    /**
     * Builds a repo
     */
    public InMemoryJobRepo() {
        this(new ConcurrentHashMap<>(), new LinkedBlockingQueue<>());
    }

    /**
     * Builds a repo with a single event consumer
     *
     * @param eventConsumer The event consumer
     */
    public InMemoryJobRepo(final Consumer<Event> eventConsumer) {
        this(new ConcurrentHashMap<>(), singleton(eventConsumer));
    }

    InMemoryJobRepo(final ConcurrentMap<String, Job> map, final Collection<Consumer<Event>> eventsConsumers) {
        this.map = map;
        this.eventsConsumers = eventsConsumers;
    }

    @Override
    public void upsert(final Job job) {
        Job previousJob = this.map.put(job.getJobId(), job);
        if (previousJob == null || previousJob.getStatus().equals(job.getStatus())) {
            fire(
                    JobEvent.builder()
                            .jobOldValue(previousJob)
                            .jobActualValue(job)
                            .jobEventType(CHANGED_JOB_STATUS)
                            .build()
            );
        }
    }

    private void fire(final Event event) {
        this.eventsConsumers.forEach(eventConsumer -> eventConsumer.accept(event));
    }

    @Override
    public void remove(final String id) {
        Job removedJob = this.map.remove(id);
        if (removedJob != null) {
            fire(
                    JobEvent.builder()
                            .jobOldValue(removedJob)
                            .jobEventType(REMOVED)
                            .build()
            );
        }
    }

    @Override
    public Optional<Job> byId(final String id) {
        return Optional.ofNullable(this.map.get(id));
    }

    @Override
    public void subscribe(final Consumer<Event> eventsConsumer) {
        this.eventsConsumers.add(eventsConsumer);
    }

    @Override
    public UnitOfWork unitOfWork() {
        return new InMemoryUnitOfWork();
    }

    @Override
    public Iterator<Job> iterator() {
        return unmodifiableCollection(this.map.values()).iterator();
    }

    final class InMemoryUnitOfWork implements UnitOfWork {
        private final BlockingQueue<Runnable> actions;

        InMemoryUnitOfWork() {
            this(new LinkedBlockingQueue<>());
        }

        InMemoryUnitOfWork(final BlockingQueue<Runnable> actions) {
            this.actions = actions;
        }

        @Override
        public void registerUpsert(final Job job) {
            this.actions.add(() -> InMemoryJobRepo.this.upsert(job));
        }

        @Override
        public void registerRemove(final String id) {
            this.actions.add(() -> InMemoryJobRepo.this.remove(id));
        }

        @Override
        public void commit() {
            drainedActions().forEach(Runnable::run);
        }

        private Iterable<Runnable> drainedActions() {
            Queue<Runnable> result = new ConcurrentLinkedQueue<>();
            this.actions.drainTo(result);
            return result;
        }
    }
}
