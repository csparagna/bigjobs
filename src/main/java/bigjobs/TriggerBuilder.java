package bigjobs;

import lombok.Getter;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;


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
public class TriggerBuilder<EVT extends Event> {
    @Getter
    BigJobs bigJobs;
    @Getter
    Predicate<EVT> event;
    @Getter
    String name;
    @Getter
    BooleanSupplier condition;
    @Getter
    Consumer<EVT> action;
    @Getter
    boolean autoRemove;
    @Getter
    boolean enabled = true;


    public TriggerBuilder<EVT> on(Predicate<EVT> eventMatcher){
        this.event = eventMatcher;
        return this;
    }

    public TriggerBuilder<EVT> withName(String name){
        this.name = name;
        return this;
    }

    public TriggerBuilder<EVT> ifCondition(BooleanSupplier condition){
        this.condition = condition;
        return this;
    }
    public TriggerBuilder<EVT> then(Consumer<EVT> action){
        this.action = action;
        return this;
    }

    public TriggerBuilder autoRemove(boolean autoremove){
        autoremove = autoRemove;
        return this;
    }

    public Trigger<EVT> build(){
        Trigger<EVT> trigger = new Trigger<> (this);
        bigJobs.add(trigger);
        return trigger;
    }


    private TriggerBuilder<EVT> withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public TriggerBuilder<EVT> withBigJobs(BigJobs bigJobs) {
        this.bigJobs = bigJobs;
        return this;
    }
}
