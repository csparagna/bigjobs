package bigjobs;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;


public class TriggerBuilder {
    BigJobs bigJobs;
    Predicate<Event> event;
    String name;
    BooleanSupplier condition;
    Runnable action;
    boolean autoRemove;


    public TriggerBuilder on(Predicate<Event> eventMatcher){
        this.event = eventMatcher;
        return this;
    }

    public TriggerBuilder withName(String name){
        this.name = name;
        return this;
    }

    public TriggerBuilder ifCondition(BooleanSupplier condition){
        this.condition = condition;
        return this;
    }
    public TriggerBuilder then(Runnable action){
        this.action = action;
        return this;
    }

    public TriggerBuilder autoRemove(boolean autoremove){
        autoremove = autoRemove;
        return this;
    }

    public Trigger build(){

        Trigger trigger = Trigger.builder()
                .withName(name)
                .withEnabled(true)
                .withAutoRemove(autoRemove)
                .withAction(action)
                .withEventMatcher(event)
                .withCondition(condition)
                .build();
        bigJobs.add(trigger);
        return trigger;
    }

    public TriggerBuilder withBigJobs(BigJobs bigJobs) {
        this.bigJobs = bigJobs;
        return this;
    }
}
