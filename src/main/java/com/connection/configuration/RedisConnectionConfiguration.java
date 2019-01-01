package com.connection.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@PropertySource("classpath:redis.properties")
public class RedisConnectionConfiguration {

    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;


    private static final Logger LOG = LoggerFactory.getLogger(RedisConnectionConfiguration.class);

    private static final String DEFAULT_CONNECTION = "redis";

    @Bean
    @Primary
    public JedisConnectionFactory jedisConnectionFactory() {
        LOG.debug("Connecting to redis...");

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().usePooling().build();
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(configuration, jedisClientConfiguration);
        jedisConnectionFactory.afterPropertiesSet();

        LOG.debug("Established connection to redis {}", DEFAULT_CONNECTION);
        logRedisProperties();

        return jedisConnectionFactory;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private void logRedisProperties() {
        LOG.info("=============Redis Connection================");
        LOG.info("========== Redis host: {}", redisHost);
        LOG.info("========== Redis port: {}", redisPort);
        LOG.info("=============================================");
    }

    @Bean
    @Primary
    CacheManager cacheManager() {
        return RedisCacheManager.builder(jedisConnectionFactory()).build();
    }
}
