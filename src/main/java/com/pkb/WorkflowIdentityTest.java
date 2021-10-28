package com.pkb;

import com.google.api.core.ApiFutureCallback;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import io.grpc.LoadBalancerRegistry;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

import static com.google.api.core.ApiFutures.addCallback;
import static com.google.cloud.pubsub.v1.Subscriber.newBuilder;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.lang.System.getenv;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class WorkflowIdentityTest {
    private static final Logger LOG = getLogger(lookup().lookupClass());

    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());

        var projectId = checkNotNull(getenv("PROJECT_ID"), "PROJECT_ID must not be null");
        var topicName = checkNotNull(getenv("TOPIC"), "TOPIC must not be null");
        var subscriptionName = checkNotNull(getenv("SUBSCRIPTION"), "SUBSCRIPTION must not be null");

        var topic = ProjectTopicName.of(projectId, topicName);

        Publisher publisher = null;
        try {
            publisher = Publisher.newBuilder(topic).build();

            var data = ByteString.copyFromUtf8("my-message " + LocalDateTime.now());
            var pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
            var messageIdFuture = publisher.publish(pubsubMessage);

            addCallback(messageIdFuture, new ApiFutureCallback<>() {
                @Override
                public void onSuccess(String messageId) {
                    LOG.info("published with message id: {}", messageId);
                }

                @Override
                public void onFailure(Throwable t) {
                    LOG.error("failed to publish: ", t);
                }
            }, directExecutor());
        } finally {
            if (publisher != null) {
                publisher.shutdown();
                publisher.awaitTermination(1, MINUTES);
            }
        }

        var subscription = ProjectSubscriptionName.of(projectId, subscriptionName);

        MessageReceiver receiver = (message, consumer) -> {
            LOG.info("got message: {}", message.getData().toStringUtf8());
            consumer.ack();
        };

        Subscriber subscriber = null;
        try {
            subscriber = newBuilder(subscription, receiver)
                    .setCredentialsProvider(FixedCredentialsProvider.create(ComputeEngineCredentials.create()))
                    .build();
            subscriber.addListener(new Subscriber.Listener() {
                                       @Override
                                       public void failed(Subscriber.State from, Throwable failure) {
                                           LOG.error("Listener failed, ", failure);
                                       }
                                   },
                    directExecutor());
            subscriber.startAsync().awaitTerminated();
        } finally {
            if (subscriber != null) {
                subscriber.stopAsync();
            }
        }
    }
}
