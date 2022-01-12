package com.maverick.delayedrmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This is the class producer uses for raising event to RabbitMQ.
 */
@Component
@Slf4j
public class QueueGateway {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${messaging.topic}")
    private String exchange;

    /*
     * (non-Javadoc)
     *
     * @see
     * com.climate.event.gateway.QueueGatewayInterface#pushEventToQueue(java.
     * lang.Object, java.lang.String)
     */
    public boolean pushEventToQueue(final Object payLoad, String queueName) {
        log.info("QueueGateway.pushEventToQueue :: Called with payLoad = {} and queueName = {}", payLoad, queueName);
        String routingKey = RabbitMQConstants.ROUTING_KEY_PREFIX.concat(queueName);
        rabbitTemplate.convertAndSend(exchange, routingKey, payLoad);
        log.info("Successfully Sent queueEvent to {}", queueName);
        return true;
    }
}
