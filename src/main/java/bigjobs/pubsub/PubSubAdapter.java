package bigjobs.pubsub;

import java.util.List;
import java.util.function.Consumer;

/**
 * Minimal Pub/Sub abstraction to decouple BigJobs from a specific vendor SDK.
 *
 * Implementations should start consuming messages from the given subscriptions
 * and invoke the provided handler with the raw message payload (UTF-8 string).
 * Returned AutoCloseable must stop the consumption when closed.
 */
public interface PubSubAdapter {

    /**
     * Start consuming messages for the provided subscriptions.
     *
     * @param subscriptions list of subscription identifiers (format decided by implementation)
     * @param messageHandler consumer receiving the raw message payload as String
     * @return a handle that, when closed, stops the consumer
     */
    AutoCloseable start(List<String> subscriptions, Consumer<String> messageHandler);
}
