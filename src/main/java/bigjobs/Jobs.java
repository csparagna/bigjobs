package bigjobs;

import java.util.function.Predicate;

public class Jobs {

    public static Predicate<Job> withType(String type){
        return job -> {
            return job.getType().equals(type);
        };
    }

    public static Predicate<Job> withAttribute(String attributeName, String attributeValue){
        return job -> {
            String value = job.getAttributes().get(attributeName);
            if (value==null && attributeValue==null) return true;
            return  value!=null && attributeValue!=null && value.equals(attributeValue);
        };
    }


    public static Predicate<Job> or(Predicate<Job>... predicates){
        return job -> {
          for (Predicate<Job> predicate: predicates){
              if (predicate.test(job)) return true;
          }
          return false;
        };
    }

}
