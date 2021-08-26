package com.pkb.sandbox.pubsub;

import org.apache.camel.Consume;
import org.slf4j.Logger;

import java.util.Objects;

import static com.pkb.sandbox.pubsub.Config.TEST_CONSUMER;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

public class Consumer {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    @Consume(TEST_CONSUMER)
    public void process(String message) {
        LOG.info(Objects.toString(message));
    }
}
