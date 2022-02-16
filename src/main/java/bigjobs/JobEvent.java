package bigjobs;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
public class JobEvent extends Event {

    @Getter
    JobEventType jobEventType;
    @Getter
    Job jobOldValue;
    @Getter
    Job jobActualValue;

}
