package com.maverick.delayedrmq.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class RedisUtil<T> {

    private final RedisTemplate<String, T> redisTemplate;

    private final HashOperations<String, Object, T> hashOperation;

    private final ListOperations<String, T> listOperation;

    private final ValueOperations<String, T> valueOperations;

    @Autowired
    RedisUtil(RedisTemplate<String, T> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperation = redisTemplate.opsForHash();
        this.listOperation = redisTemplate.opsForList();
        this.valueOperations = redisTemplate.opsForValue();
    }

    /**
     * @param redisKey
     * @param key
     * @param data
     */
    public void putMap(String redisKey, Object key, T data) {
        hashOperation.put(redisKey, key, data);
    }

    /**
     * @param redisKey
     * @param key
     * @return
     */
    public T getMapAsSingleEntry(String redisKey, Object key) {
        return hashOperation.get(redisKey, key);
    }

    /**
     * @param redisKey
     * @return
     */
    public Map<Object, T> getMapAsAll(String redisKey) {
        return hashOperation.entries(redisKey);
    }

    /**
     * @param key
     * @param value
     */
    public void putValue(String key, T value) {
        valueOperations.set(key, value);
    }

    /**
     * @param key
     * @param value
     * @param timeout
     * @param unit
     */
    public void putValueWithExpireTime(String key, T value, long timeout, TimeUnit unit) {
        valueOperations.set(key, value, timeout, unit);
    }

    /**
     * @param key
     * @return
     */
    public T getValue(String key) {
        return valueOperations.get(key);
    }

    /**
     * @param key
     * @param timeout
     * @param unit
     */
    public void setExpire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    /**
     * @param redisMapKey
     * @param expiryDate
     */
    public void setExpireAt(String key, Date expiryDate) {
        redisTemplate.expireAt(key, expiryDate);
    }

    /**
     * @param redisMapKey
     * @param *keys
     */
    public void delete(String redisMapKey, Object... key) {
        hashOperation.delete(redisMapKey, key);
    }
}