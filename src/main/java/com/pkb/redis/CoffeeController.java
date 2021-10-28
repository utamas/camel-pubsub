package com.pkb.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.Duration.ofSeconds;
import static org.slf4j.LoggerFactory.getLogger;

@RestController
public class CoffeeController {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ReactiveRedisTemplate<String, String> redis;
    private final ObjectMapper json;

    CoffeeController(ReactiveRedisTemplate<String, String> redis, ObjectMapper json) {
        this.redis = redis;
        this.json = json;
    }
//
//    @GetMapping("/coffees")
//    public Flux<Coffee> all() {
//        return redis.keys("*")
//                .flatMap(redis.opsForValue()::get);
//    }

    @PostMapping("/coffees")
    Mono<String> addCoffee(@RequestBody Coffee coffee) {
        return redis.opsForStream()
                .add(coffee.getId(), Map.of("coffee", asJson(coffee)))
                // we want to emmit our ID instead of the redis one.
                .map(RecordId::getValue);
    }

    @GetMapping(path = "/coffees/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamCoffees(@PathVariable("id") String stream) {
        //noinspection unchecked
        return redis.<String, String>opsForStream()
                .read(StreamReadOptions.empty().block(ofSeconds(30)), StreamOffset.create(stream, ReadOffset.from("0")))
                .map(record -> record.getValue().get("coffee"))
                .doAfterTerminate(() -> {
                    LOG.info("deleting stream");
                    redis.delete(stream).subscribe();
                });
    }

    @SneakyThrows
    private String asJson(Coffee coffee) {
        return json.writeValueAsString(coffee);
    }
}