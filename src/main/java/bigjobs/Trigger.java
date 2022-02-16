package bigjobs;

import lombok.Builder;
import lombok.Getter;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
