package bigjobs;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
public class JobEvent extends Event {

    @Getter
    EventType type;
    @Getter
    Job jobOldValue;
    @Getter
    Job jobActualValue;

}
