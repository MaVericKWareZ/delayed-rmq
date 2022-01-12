package com.maverick.delayedrmq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public abstract class RabbitMQConfiguration {

    private final Logger logger = LoggerFactory.getLogger(RabbitMQConfiguration.class);
    @Value("${messaging.host}")
    String host;
    @Value("${messaging.vhost}")
    String virtualHost;
    @Value("${messaging.usr}")
    String usr;
    @Value("${messaging.pwd}")
    String pwd;
    @Value("${messaging.topic}")
    String topic;
    @Value("${messaging.requestedHeartBeat}")
    int requestedHeartBeat;
    @Value("${messaging.ssl.enabled}")
    String isSSLenabled;
    @Value("${messaging.port}")
    int sslPort;
    @Value("${messaging.ssl.key.p12}")
    String sslKeyP12;
    @Value("${messaging.ssl.key.p12.path}")
    String sslKeyP12Path;
    @Value("${messaging.ssl.key.jks}")
    String sslKeyJks;
    @Value("${messaging.ssl.key.jks.path}")
    String sslKeyJksPath;
    @Value("${messaging.ssl.keyPassphrase:null}")
    String sslKeyPassphrase;
    @Value("${messaging.ssl.trustPassphrase:null}")
    String sslTrustPassphrase;
    @Value("${messaging.ssl.certificate.utility}")
    String sslCertificateUtility;
    @Value("${messaging.ssl.TLS.protocol.versions}")
    String tlsProtocolVersion;
    //todo: remove require false when all the service can inject ConnectionFactory
    @Autowired(required = false)
    private ConnectionFactory connectionFactoryFromSecretsManager;

    public static String base64Decode(String token) {
        byte[] decodedBytes = token.getBytes();
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    //todo: remove this method when all the service can inject ConnectionFactory
    public ConnectionFactory connectionFactory() {
        if (connectionFactoryFromSecretsManager != null) {
            return connectionFactoryFromSecretsManager;
        } else {
            logger.info("SSL Status [Enable={}]", isSSLenabled);
            CachingConnectionFactory cachingConnectionFactory = null;
            com.rabbitmq.client.ConnectionFactory connectionFactory = null;
            if (("true").equalsIgnoreCase(isSSLenabled)) {
                String base64DecodeKeyPassphrase = base64Decode(sslKeyPassphrase);
                String base64DecodeTrustPassphrase = base64Decode(sslTrustPassphrase);
                try {
                    char[] keyPassphrase = base64DecodeKeyPassphrase.toCharArray();
                    KeyStore ks = KeyStore.getInstance(sslKeyP12);
                    ks.load(new FileInputStream(sslKeyP12Path), keyPassphrase);
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(sslCertificateUtility);
                    kmf.init(ks, keyPassphrase);
                    char[] trustPassphrase = base64DecodeTrustPassphrase.toCharArray();
                    KeyStore tks = KeyStore.getInstance(sslKeyJks);
                    tks.load(new FileInputStream(sslKeyJksPath), trustPassphrase);
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(sslCertificateUtility);
                    tmf.init(tks);
                    SSLContext sslContext = SSLContext.getInstance(tlsProtocolVersion);
                    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                    connectionFactory = new com.rabbitmq.client.ConnectionFactory();
                    connectionFactory.useSslProtocol(sslContext);
                    cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
                    cachingConnectionFactory.setHost(host);
                    cachingConnectionFactory.setPort(sslPort);
                } catch (GeneralSecurityException | IOException generalSecurityException) {
                    logger.error("exception while converting in smsBO at Listener ", generalSecurityException);
                    throw new RuntimeException("Exception");
                }
            } else {
                cachingConnectionFactory = new CachingConnectionFactory(host);
            }
            cachingConnectionFactory.getRabbitConnectionFactory().setAutomaticRecoveryEnabled(true);
            cachingConnectionFactory.getRabbitConnectionFactory().setTopologyRecoveryEnabled(true);
            cachingConnectionFactory.setVirtualHost(virtualHost);
            cachingConnectionFactory.setUsername(usr);
            final String decodedPassword = base64Decode(pwd);
            cachingConnectionFactory.setPassword(decodedPassword);
            cachingConnectionFactory.setRequestedHeartBeat(requestedHeartBeat);
            return cachingConnectionFactory;
        }
    }

    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory, SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(10);
        factory.setMaxConcurrentConsumers(20);
        factory.setPrefetchCount(10);
        factory.setAutoStartup(true);
        return factory;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        logger.info("EventConfiguration.RabbitTemplate() called");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setExchange(topic);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Declarables declarablesDlq() {
        List<String> queueNames = getQueueNamesForDlq();
        List<Declarable> declarables = new ArrayList<>();
        for (String queueName : queueNames) {
            Queue dlq = QueueBuilder.durable(queueName)
                    // Set Dead Letter Switch
                    .withArgument(RabbitMQConstants.X_DEAD_LETTER_EXCHANGE, topic)
                    .withArgument(RabbitMQConstants.X_DEAD_LETTER_ROUTING_KEY,
                            RabbitMQConstants.ROUTING_KEY_PREFIX.concat(RabbitMQConstants.DEAD_LETTER_QUEUE))
                    .build();
            Binding dlqBinding = BindingBuilder.bind(dlq).to(exchange())
                    .with(RabbitMQConstants.ROUTING_KEY_PREFIX.concat(dlq.getName()));
            declarables.add(dlq);
            declarables.add(dlqBinding);
        }
        return new Declarables(declarables.toArray(new Declarable[0]));
    }

    protected List<String> getQueueNamesForDlq() {
        return new ArrayList<>();
    }

    @Bean
    public Declarables declarables() {
        List<String> queueNames = getQueueNames();
        List<Declarable> declarables = new ArrayList<>();
        DirectExchange exchange = exchange();
        Queue queue = null;
        Binding binding = null;
        for (String queueName : queueNames) {
            queue = new Queue(queueName);
            binding = BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.ROUTING_KEY_PREFIX.concat(queue.getName()));
            declarables.add(queue);
            declarables.add(binding);
        }
        return new Declarables(declarables.toArray(new Declarable[0]));
    }

    protected List<String> getQueueNames() {
        return new ArrayList<>();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(topic);
    }
}
