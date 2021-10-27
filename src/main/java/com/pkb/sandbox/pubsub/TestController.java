package com.pkb.sandbox.pubsub;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@RestController
public class TestController {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PubSubEventProducer producer;
    private final PubSubEventConsumer consumer;

    @Autowired
    public TestController(PubSubEventProducer producer, PubSubEventConsumer consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    @GetMapping("/kick")
    String kick() {
        var requestId = UUID.randomUUID().toString();
        producer.kick(requestId);
        return requestId;
    }

    @GetMapping("/pull/{id}")
    Mono<String> pull(@PathVariable("id") String requestId) {
        LOG.info("Looking for response for {}", requestId);
        return consumer.response(requestId);
    }

    @GetMapping(value = "/jokes", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> getPersonStream() {
        return consumer.pubsubMessages().map(Event::getMessage);
    }
}
