package bigjobs;

import lombok.Builder;
import lombok.Getter;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

@Builder(setterPrefix = "with")
@Getter
public class Trigger {

    BigJobs jobs;
    boolean enabled = true;

    // TODO distinguere JobEvent da altri (MsgEvent, TimeEvent, ...)
    Predicate<Event> eventMatcher;
    String name;

    BooleanSupplier condition;
    Runnable action;
    boolean autoRemove;

    public boolean shouldFire(Event event){
        boolean result = enabled && eventMatcher.test(event) && (condition==null || condition.getAsBoolean());
        System.out.println("shouldFire "+result);
        return result;
    }

    public void fire(){
        if (autoRemove){ enabled = false; jobs.remove(this); }
        System.out.println("fire ");
        action.run();
    }

}
