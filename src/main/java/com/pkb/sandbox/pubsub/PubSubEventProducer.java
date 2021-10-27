package com.pkb.sandbox.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;

import java.time.LocalDateTime;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

public class PubSubEventProducer {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ProducerTemplate producer;
    private final ObjectMapper json;

    public PubSubEventProducer(ProducerTemplate producer, ObjectMapper json) {
        this.producer = producer;
        this.json = json;
    }

    public void kick(String requestId) {
        try {
            LOG.info("About to send...");
            producer.sendBody(json.writer().writeValueAsString(new Event(requestId, String.format("Hi. My time is: %s", LocalDateTime.now()))));
            LOG.info("Sent");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
