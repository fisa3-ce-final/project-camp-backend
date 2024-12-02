package com.rental.camp.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Long> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();

        // Redis 연결 팩토리 설정
        redisTemplate.setConnectionFactory(connectionFactory);

        // 키 직렬화 - 문자열로 처리
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // 값 직렬화 - Long 타입으로 처리
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Long.class));

        return redisTemplate;
    }
}