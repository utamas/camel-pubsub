package com.pkb.sandbox.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.Duration.ofSeconds;
import static org.slf4j.LoggerFactory.getLogger;

public class PubSubIntegrationWrapper {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ObjectMapper json;
    private final ProducerTemplate producer;
    private final ReactiveRedisTemplate<String, String> redis;

    public PubSubIntegrationWrapper(ObjectMapper json, ProducerTemplate producer, ReactiveRedisTemplate<String, String> redis) {
        this.json = json;
        this.producer = producer;
        this.redis = redis;
    }

    public Flux<String> kick(boolean streamProgress) {
        var requestId = UUID.randomUUID().toString();

        return Flux.create(emitter -> {
            //noinspection unchecked
            var d = redis.<String, String>opsForStream()
                    .read(StreamReadOptions.empty().block(ofSeconds(10)), StreamOffset.create(requestId, ReadOffset.from("0")))
                    .map(record -> record.getValue().get("response"))
                    .subscribe(event -> {
                        emitter.next(event);
                        emitter.complete();
                        redis.delete(requestId).subscribe();
//                        LOG.info("Terminated response flux and redis stream.");
                    });

            try {

                producer.sendBody(json.writer().writeValueAsString(buildEvent(requestId)));
//                LOG.info("PubSub Request is sent {}.", requestId);

                if (streamProgress) {
                    emitter.next(requestId);
                }

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
