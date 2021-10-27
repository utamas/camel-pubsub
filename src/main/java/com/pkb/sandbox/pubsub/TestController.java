package com.pkb.sandbox.pubsub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class TestController {
    private final PubSubIntegrationWrapper consumer;

    @Autowired
    public TestController(PubSubIntegrationWrapper consumer) {
        this.consumer = consumer;
    }

    @GetMapping(value = "/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> test() {
        return consumer.kick();
    }
}
