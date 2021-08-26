package com.pkb.sandbox.pubsub;

import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;

import java.time.LocalDateTime;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.slf4j.LoggerFactory.getLogger;

public class Producer {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ProducerTemplate producer;

    public Producer(ProducerTemplate producer) {
        this.producer = producer;
    }

    public void kick() {
        newFixedThreadPool(1).submit(() -> {
            try {
                Thread.sleep(5000);
                LOG.info("About to send...");
                producer.sendBody(String.format("Hi. My time is: %s", LocalDateTime.now()));
                LOG.info("Sent");
            } catch (InterruptedException cause) {
                LOG.error("Ups, ", cause);
                throw new IllegalStateException(cause);
            }
        });
    }
}
