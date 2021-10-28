package com.pkb.sandbox.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@Import(CamelAutoConfiguration.class)
public class Config {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    @Bean
    public ObjectMapper json() {
        return new ObjectMapper();
    }

    static final String TEST_CONSUMER = "direct:testconsumer";
    private static final String TEST_PRODUCER = "direct:testproducer";

    @Autowired
    @Produce(TEST_PRODUCER)
    private ProducerTemplate producer;


    @Bean
    public KickService consumer(ObjectMapper json, ReactiveRedisTemplate<String, String> redis) {
        return new KickService(json, producer, redis);
    }

    @Bean
    public RouteBuilder routes() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(TEST_PRODUCER)
                        .to("google-pubsub:emulator:phoenix-task-request");

                from("google-pubsub:emulator:phoenix-task-response")
                        .to(TEST_CONSUMER);

                LOG.info("Routes added.");
            }
        };
    }
}
