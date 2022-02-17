package bigjobs;

import java.util.Map;

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
public interface Job {
    JobStatus getStatus();
    String getJobId();
    String getType();
    Map<String,String> getAttributes();
    String getTechnology();

    void remove() throws BJException;

    void cancel() throws BJException;

    }

//PENDING: The job is scheduled and waiting to be run.
//RUNNING: The job is in progress.
//DONE
