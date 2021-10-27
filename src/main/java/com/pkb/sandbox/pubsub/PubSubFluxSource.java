package com.pkb.sandbox.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.pkb.sandbox.pubsub.Config.TEST_CONSUMER;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

public class PubSubFluxSource {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final Map<String, Consumer<Event>> emitters = new ConcurrentHashMap<>();

    private final ObjectMapper json;
    private final Flux<Event> flux;

    public PubSubFluxSource(ObjectMapper json) {
        this.json = json;

        flux = Flux
                .create(emitter -> {
                    LOG.info("Flux is getting initialized");
                    Consumer<Event> next = emitter::next;

                    String key = String.valueOf(next.hashCode());
                    emitters.put(key, next);

                    LOG.info("Pushed {}", key);

                    emitter.onDispose(() -> {
                        LOG.info("disposed {}", key);
                       emitters.remove(key);
                    });

                    emitter.onCancel(() -> {
                        LOG.info("cancelled {}/{}", key, next.hashCode());
                        emitters.remove(key);
                    });

                    LOG.info("collection size: {}", emitters.size());
                });
    }

    @Consume(TEST_CONSUMER)
    public void process(String event) {
            emitters.forEach((hash, emitter) -> {
                try {
                    emitter.accept(json.readValue(event, Event.class));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException(e);
                }
            });
    }

    public Flux<Event> events() {
        return this.flux;
    }
}
