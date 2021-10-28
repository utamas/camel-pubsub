package com.pkb.redis;

import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

@RestController
public class CoffeeController {
    private final ReactiveRedisOperations<String, Coffee> coffeeOps;

    CoffeeController(ReactiveRedisOperations<String, Coffee> coffeeOps) {
        this.coffeeOps = coffeeOps;
    }

    @GetMapping("/coffees")
    public Flux<Coffee> all() {
        return coffeeOps.keys("*")
                .flatMap(coffeeOps.opsForValue()::get);
    }

    @GetMapping(path = "/coffee-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Coffee> stream() {
        var opts = StreamReadOptions.empty()
                .block(ofSeconds(100))
                .count(1);

        coffeeOps.opsForStream()
                .read(opts, StreamOffset.create("foo2", ReadOffset.from("0")))
                .subscribe(record -> {
                    System.out.println(record);
                });
        
        return Flux.empty();
    }
}