package com.pkb.sandbox.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.camel.Consume;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static com.pkb.sandbox.pubsub.Config.TEST_CONSUMER;
import static java.lang.invoke.MethodHandles.lookup;
import static java.time.Duration.ofSeconds;
import static org.slf4j.LoggerFactory.getLogger;

public class KickService {
    private static final Logger LOG = getLogger(lookup().lookupClass());
    public static final String RESPONSE = "response";

    private final ObjectMapper json;
    private final ProducerTemplate pubsubTopic;
    private final ReactiveRedisTemplate<String, String> redis;

    public KickService(ObjectMapper json, ProducerTemplate producer, ReactiveRedisTemplate<String, String> redis) {
        this.json = json;
        this.pubsubTopic = producer;
        this.redis = redis;
    }

    // Responses from Google PubSub phoenix-response subscription arrive here.
    @SneakyThrows
    @Consume(TEST_CONSUMER)
    public void process(String response) {
        // throwing an exception will cause a nack -> pubsub will attempt to re-deliver the message.
        var correlationId = json.readValue(response, Event.class).getCorrelationId();

        // Then those messages are sent to aan ephemeral redis stream
        // the stream name is the same as the task request id.
        redis.opsForStream()
                .add(correlationId, Map.of(RESPONSE, response))
                .subscribe(id -> LOG.debug("PubSub message [{}] is added to redis [{}]", correlationId, id.getValue()));
    }

    public Flux<String> kick(boolean streamProgress) {
        // First a correlation id is generated.
        var correlationId = UUID.randomUUID().toString();

        // We create a new flux service call.
        // The supplied lambda is called EACH time there is a new subscriber to the returned flux.
        // Since there should be only a single subscriber (spring framework that transforms the flux into http) we are good.
        return Flux.create(emitter -> {
            // Before we dispatch the task to PubSub, we need to prepare for receiving a response and correlating it back.
            // For this, we connect to a redis stream  -which for matter of fact, does not exist at the moment when subscribe to it -
            // and we wait for events to be streamed to redis (which will happen once a response is received from PubSub).

            //noinspection unchecked
            var d = redis.<String, String>opsForStream()
                    // we wait at most 10 seconds to receive an event.
                    // Offset should be kept from 0, latest() has a tendency to cause loss of messages.
                    // (It has something to do with the accuracy of the timestamps)
                    .read(StreamReadOptions.empty().block(ofSeconds(10)), StreamOffset.create(correlationId, ReadOffset.from("0")))
                    // Extract the response from redis record.
                    .map(record -> record.getValue().get(RESPONSE))
                    .subscribe(event -> {
                        // Send the payload to subscribers
                        emitter.next(event);
                        // And end the stream of events.
                        emitter.complete();
                        // Tidy up redis (the stream was custom to the request and shall be deleted).
                        redis.delete(correlationId).subscribe();
                        LOG.debug("Terminated response flux and redis stream {}.", correlationId);
                    });

            try {

                // Once we are ready to receive the response, let's fire the query
                pubsubTopic.sendBody(json.writer().writeValueAsString(buildEvent(correlationId)));

                // If asked, we can send multiple updates, out of which the first could be that request was placed to PubSub
                if (streamProgress) {
                    emitter.next(correlationId);
                }

            } catch (JsonProcessingException cause) {
                emitter.error(cause);
            }

            // Just be tidy...
            emitter.onDispose(d);
        });
    }

    private Event buildEvent(String requestId) {
        var now = LocalDateTime.now();
        return new Event(requestId, String.format("Hi. My time is: %s", now));
    }
}
