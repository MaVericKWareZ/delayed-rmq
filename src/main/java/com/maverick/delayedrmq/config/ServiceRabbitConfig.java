package com.maverick.delayedrmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableRabbit
@Slf4j
public class ServiceRabbitConfig extends RabbitMQConfiguration {

    @Override
    protected List<String> getQueueNames() {
        List<String> queueNames = new ArrayList<>();
        queueNames.add(QueueNames.USER_VIEWS_QUEUE);
        queueNames.add(QueueNames.USER_VIEWS_UPDATE_QUEUE);
        return queueNames;
    }

    @Override
    protected List<String> getQueueNamesForDlq() {
        List<String> dlqQueueNames = new ArrayList<>();
        dlqQueueNames.add(QueueNames.USER_VIEWS_UPDATE_DLQ);
        return dlqQueueNames;
    }

    @Bean
    Queue backoffQueue() {
        return QueueBuilder.durable(QueueNames.USER_VIEWS_UPDATE_BACKOFF_QUEUE)
                .withArgument(RabbitMQConstants.X_DEAD_LETTER_EXCHANGE, exchange())
                .withArgument(RabbitMQConstants.X_MESSAGE_TTL, getDelayInMillis())
                .withArgument(RabbitMQConstants.X_DEAD_LETTER_ROUTING_KEY, RabbitMQConstants.ROUTING_KEY_PREFIX.concat(QueueNames.USER_VIEWS_UPDATE_QUEUE))
                .build();
    }

    @Bean
    @DependsOn("backoffQueue")
    Binding backoffQueueBinding() {
        Queue queue = backoffQueue();
        log.info("Bound backoff queue with name={}", queue.getName());
        return BindingBuilder.bind(queue).to(exchange()).with(RabbitMQConstants.ROUTING_KEY_PREFIX.concat(QueueNames.USER_VIEWS_UPDATE_BACKOFF_QUEUE));
    }

    @Bean
    @DependsOn("backoffQueueBinding")
    public Declarables declarablesForBackoffQueue() {
        List<Declarable> declarables = new ArrayList<>();
        Queue queue = backoffQueue();
        Binding binding = backoffQueueBinding();
        declarables.add(queue);
        declarables.add(binding);
        log.info("Bound 1 queue with name={}", queue.getName());
        return new Declarables(declarables.toArray(new Declarable[0]));
    }
}
