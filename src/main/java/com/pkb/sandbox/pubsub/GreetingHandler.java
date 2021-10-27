package com.pkb.sandbox.pubsub;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class GreetingHandler {

    public Mono<ServerResponse> hello(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
                .body(BodyInserters.fromValue(new Greeting("Hello, Spring!")));
    }

    //public Flux<String> test() {
    //    return Flux.interval(Duration.ofSeconds(1)).
    //}
}