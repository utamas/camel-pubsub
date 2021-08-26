package com.pkb.sandbox.pubsub;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@Import(CamelAutoConfiguration.class)
public class Config {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    static final String TEST_CONSUMER = "direct:testconsumer";
    private static final String TEST_PRODUCER = "direct:testproducer";

    @Autowired
    @EndpointInject(TEST_PRODUCER)
    @Produce(TEST_PRODUCER)
    private ProducerTemplate producer;

    @Bean
    public Consumer consumer() {
        return new Consumer();
    }

    @Bean
    public Producer producer() {
        LOG.info("What is this kerek? {}", producer);
        return new Producer(producer);
    }

    @Bean
    public RouteBuilder routes() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(TEST_PRODUCER)
                        .to("google-pubsub:fhir-experiments-20210712:kms-key-available-05");

                from("google-pubsub:fhir-experiments-20210712:kms-key-available-05-kms")
                        .to(TEST_CONSUMER);

                LOG.info("Routes added.");
            }
        };
    }
}
