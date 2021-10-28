package com.pkb.sandbox.pubsub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class TestController {
    private final PubSubIntegrationWrapper consumer;

    @Autowired
    public TestController(PubSubIntegrationWrapper consumer) {
        this.consumer = consumer;
    }


    @GetMapping(value = "/kick", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<String> kick() {
        return consumer.kick(false).next();
    }

    @GetMapping(value = "/kicks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> kicks() {
        return consumer.kick(true);
    }
}
