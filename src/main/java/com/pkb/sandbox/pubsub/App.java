package com.pkb.sandbox.pubsub;

import io.grpc.LoadBalancerRegistry;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@SpringBootApplication
@ComponentScan(value = {"com.pkb.sandbox.pubsub"})
public class App {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    public static void main(String[] args) {
        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());

        var context = SpringApplication.run(App.class, args);
        context.getBean(Producer.class).kick();
        LOG.info("Kicked...");
    }
}
