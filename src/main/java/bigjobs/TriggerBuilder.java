package bigjobs;

import lombok.Getter;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;


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
