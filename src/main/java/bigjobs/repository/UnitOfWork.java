package bigjobs.repository;

import bigjobs.Job;

/**
 * Group of committable changes
 */
public interface UnitOfWork {
    /**
     * Registers a {@link Job} upsert
     *
     * @param job The job
     */
    void registerUpsert(Job job);

    /**
     * Registers a {@link Job} removal
     *
     * @param id The job id
     */
    void registerRemove(String id);

    /**
     * Commits the changes
     */
    void commit();
}
