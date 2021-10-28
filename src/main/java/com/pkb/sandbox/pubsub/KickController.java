package com.pkb.sandbox.pubsub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class KickController {
    private final KickService service;

    @Autowired
    public KickController(KickService consumer) {
        this.service = consumer;
    }


    @GetMapping(value = "/kick", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<String> kick() {
        return service.kick(false).next();
    }

    @GetMapping(value = "/kicks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> kicks() {
        return service.kick(true);
    }
}
