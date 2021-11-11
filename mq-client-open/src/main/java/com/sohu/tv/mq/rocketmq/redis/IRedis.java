package com.sohu.tv.mq.rocketmq.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.util.Pool;

/**
 * redis统一接口，屏蔽底层实现，所有方法都来自于jedis
 * 
 * @author yongfeigao
 * @date 2021年8月25日
 */
public interface IRedis {
    /**
     * 初始化
     * 
     * @param redisConfiguration
     */
    void init(RedisConfiguration redisConfiguration);
    
    RedisConfiguration getRedisConfiguration();

    Pool<Jedis> getPool();

    JedisCluster getJedisCluster();

    String set(String key, String value, SetParams params);
    
    String get(String key);
}
