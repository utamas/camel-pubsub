package com.pkb.sandbox.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Consumer;

import static com.pkb.sandbox.pubsub.Config.TEST_CONSUMER;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

public class PubSubEventConsumer {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    private Consumer<Event> next;
    private final Flux<Event> flux;

    private final ObjectMapper json;

    public PubSubEventConsumer(ObjectMapper json) {
        this.json = json;
        flux = Flux.<Event>create(sink -> next = sink::next)
                .cache(Duration.ofSeconds(10));
    }


    @Consume(TEST_CONSUMER)
    public void process(String event) {
        try {
            next.accept(json.readValue(event, Event.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public Mono<String> response(String requestId) {
        return pubsubMessages()
                .filter(event -> {
                    LOG.info("{}, {}", event.getId(), requestId);
                    return event.getId().equals(requestId);
                })
                .map(event -> {
                    try {
                        return json.writeValueAsString(event);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .next();
    }

    public Flux<Event> pubsubMessages() {
        return this.flux;
    }
}
