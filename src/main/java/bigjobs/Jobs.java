package bigjobs;

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
