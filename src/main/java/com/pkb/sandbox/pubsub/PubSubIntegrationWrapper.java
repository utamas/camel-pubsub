package com.pkb.sandbox.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

public class PubSubIntegrationWrapper {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ObjectMapper json;
    private final ProducerTemplate producer;
    private final PubSubFluxSource source;

    public PubSubIntegrationWrapper(ObjectMapper json, ProducerTemplate producer, PubSubFluxSource source) {
        this.json = json;
        this.producer = producer;
        this.source = source;
    }

    public Flux<String> kick() {
        var requestId = UUID.randomUUID().toString();

        return Flux.create(emitter -> {
            var d = source.events()
                    .filter(event -> {
                        LOG.info("Filtering: event id=[{}], looking for id=[{}]", event.getId(), requestId);
                        return requestId.equals(event.getId());
                    })
                    .subscribe(event -> {
                        LOG.info("We received a response for {}", requestId);
                        try {
                            emitter.next(json.writeValueAsString(event));
                            emitter.complete();
                            LOG.info("Terminated flux.");
                        } catch (JsonProcessingException e) {
                            LOG.error("Ups, something went wrong: ", e);
                            emitter.error(e);
                        }
                    });

            try {

                LOG.info("Sending request");
                producer.sendBody(json.writer().writeValueAsString(buildEvent(requestId)));
                LOG.info("Request sent");

                emitter.next(requestId);

            } catch (JsonProcessingException e) {
                emitter.error(e);
            }

            emitter.onDispose(d);
        });
    }

    private Event buildEvent(String requestId) {
        var now = LocalDateTime.now();
        return new Event(requestId, String.format("Hi. My time is: %s", now));
    }
}
