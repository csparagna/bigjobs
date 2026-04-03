package bigjobs;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.unmodifiableList;

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
import java.util.ArrayList;
import java.util.List;

public class BigJobs {
    private final BigJobsJobsManager bigJobsJobsManager = new BigJobsJobsManager();
    private final List<BigJobsPlugin> plugins = new ArrayList<>();

    public synchronized void addPlugin(BigJobsPlugin plugin){
        plugin.register(bigJobsJobsManager);
        plugins.add(plugin);
        plugin.start();
    }

    public synchronized void stop(){
        // Try to gracefully stop/close all plugins
        for (BigJobsPlugin plugin : plugins){
            try {
                if (plugin instanceof AutoCloseable){
                    ((AutoCloseable) plugin).close();
                } else {
                    // best-effort: reflectively call stop() if present
                    try {
                        plugin.getClass().getMethod("stop").invoke(plugin);
                    } catch (NoSuchMethodException ignored) {
                        // no-op
                    }
                }
            } catch (Throwable t){
                // swallow to continue stopping others
            }
        }
    }


    public List<Job> getJobs() {
        return bigJobsJobsManager.getJobs();
    }

    public void add(Trigger trigger) { bigJobsJobsManager.add(trigger); }
    public void remove(Trigger trigger) { bigJobsJobsManager.remove(trigger); }


    /** Migliorare: voglio poter gestire differenti tipologie di evento senza ciclare su tutti i trigger */
    public TriggerBuilder on(Predicate<Event> event){
        TriggerBuilder triggerBuilder = new TriggerBuilder();
        triggerBuilder.withBigJobs(this);
        triggerBuilder.on(event);
        return triggerBuilder;
    }

}



