package bigjobs;

import lombok.Getter;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
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
@Getter
public class Trigger<EVT extends Event> {

    public static <EVT extends Event> TriggerBuilder<EVT> builder(){
        return new TriggerBuilder<EVT>();
    }

    protected Trigger(TriggerBuilder<EVT> builder){
        this.jobs = builder.getBigJobs();
        this.action = builder.getAction();
        this.condition = builder.getCondition();
        this.name = builder.getName();
        this.eventMatcher = builder.getEvent();
        this.autoRemove = builder.isAutoRemove();
        this.enabled = builder.isEnabled();
    }

    BigJobs jobs;
    boolean enabled = true;

    // TODO distinguere JobEvent da altri (MsgEvent, TimeEvent, ...)
    Predicate<EVT> eventMatcher;
    String name;

    BooleanSupplier condition;
    Consumer<EVT> action;

    boolean autoRemove;


    public boolean shouldFire(EVT event){

        boolean result = enabled && eventMatcher.test(event) && (condition==null || condition.getAsBoolean());
        System.out.println("shouldFire "+result);
        return result;
    }

    public void fire(EVT event){
        if (autoRemove){ enabled = false; jobs.remove(this); }
        System.out.println("fire ");
        action.accept(event);
    }

}
