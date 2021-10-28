package com.pkb.sandbox.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.util.Map;

import static com.pkb.sandbox.pubsub.Config.TEST_CONSUMER;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

public class PubSubResponseHandler {
//    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ObjectMapper json;
    private final ReactiveRedisTemplate<String, String> redis;

    public PubSubResponseHandler(ObjectMapper json, ReactiveRedisTemplate<String, String> redis) {
        this.json = json;
        this.redis = redis;
    }

    @SneakyThrows
    @Consume(TEST_CONSUMER)
    public void process(String eventRawString) {
        var event = json.readValue(eventRawString, Event.class);

//        LOG.info("PubSub response for request with ID=[{}] is received.", event.getId());

        redis.opsForStream()
                .add(event.getId(), Map.of("response", eventRawString))
                .subscribe();
//                .subscribe(id -> LOG.info("Added to redis {} - {}", event.getId(), id.getValue()));
    }
}
