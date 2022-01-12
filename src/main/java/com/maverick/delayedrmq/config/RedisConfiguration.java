package com.maverick.delayedrmq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Configuration
@EnableCaching
public class RedisConfiguration {

    @Value("${spring.redis.host}")
    private String hostName;

    @Value("${spring.redis.port}")
    private Integer port;

    @Value("${redis.cluster.nodes}")
    private List<String> nodes;

    @Value("${farmrise.env}")
    private String environment;


    @Bean
    LettuceConnectionFactory lettuceConnectionFactory() {
        if (environment.equalsIgnoreCase("DEV")) {
            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
            redisStandaloneConfiguration.setHostName(hostName);
            redisStandaloneConfiguration.setPort(port);
            return new LettuceConnectionFactory(redisStandaloneConfiguration);
        }
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(nodes);
        return new LettuceConnectionFactory(clusterConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory());
        return template;
    }

}

