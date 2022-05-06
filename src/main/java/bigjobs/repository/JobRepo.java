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

import java.util.Optional;

/**
 * {@link Job} Repository
 */
public interface JobRepo extends Iterable<Job> {
    /**
     * Updates or inserts a job
     *
     * @param job The job
     */
    void upsert(Job job);

    /**
     * Removes a job given its id
     *
     * @param id The id
     */
    void remove(String id);

    /**
     * Retrieves a job given its id
     *
     * @param id The id
     * @return The job or empty
     */
    Optional<Job> byId(String id);

    /**
     * Builds a unit of work
     *
     * @return The unit of work
     */
    UnitOfWork unitOfWork();
}
