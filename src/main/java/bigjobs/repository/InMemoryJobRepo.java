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

import bigjobs.Job;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Collections.unmodifiableCollection;

/**
 * Thread-safe in-memory implementation of {@link JobRepo}
 */
public final class InMemoryJobRepo implements JobRepo {
    private final ConcurrentMap<String, Job> map;

    public InMemoryJobRepo() {
        this(new ConcurrentHashMap<>());
    }

    InMemoryJobRepo(final ConcurrentMap<String, Job> map) {
        this.map = map;
    }

    @Override
    public void upsert(final Job job) {
        this.map.put(job.getJobId(), job);
    }

    @Override
    public void remove(final String id) {
        this.map.remove(id);
    }

    @Override
    public Optional<Job> byId(final String id) {
        return Optional.ofNullable(this.map.get(id));
    }

    @Override
    public Iterator<Job> iterator() {
        return unmodifiableCollection(this.map.values()).iterator();
    }
}
