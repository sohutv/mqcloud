package com.sohu.tv.mq.rocketmq.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.util.Pool;

/**
 * redis客户端构建器
 * 
 * @author yongfeigao
 * @date 2021年10月14日
 */
public class RedisBuilder {

    /**
     * 构建redis客户端
     * 
     * @param host
     * @param port
     * @return
     */
    public static IRedis build(String host, int port) {
        RedisConfiguration redisConfiguration = new RedisConfiguration();
        redisConfiguration.setHost(host);
        redisConfiguration.setPort(port);
        redisConfiguration.setConnectionTimeout(1000);
        redisConfiguration.setSoTimeout(1000);
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxWaitMillis(1000);
        redisConfiguration.setPoolConfig(poolConfig);
        return build(redisConfiguration);
    }

    /**
     * 构建redis客户端
     * 
     * @param hostPortString
     * @return
     */
    public static IRedis build(String hostPortString) {
        RedisConfiguration redisConfiguration = new RedisConfiguration();
        redisConfiguration.setHost(hostPortString);
        redisConfiguration.setConnectionTimeout(1000);
        redisConfiguration.setSoTimeout(1000);
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxWaitMillis(1000);
        redisConfiguration.setPoolConfig(poolConfig);
        redisConfiguration.setCluster(true);
        return build(redisConfiguration);
    }

    /**
     * 构建redis客户端
     * 
     * @param redisConfiguration
     * @return
     */
    public static IRedis build(RedisConfiguration redisConfiguration) {
        if (redisConfiguration.isCluster()) {
            ClusterRedis clusterRedis = new ClusterRedis();
            clusterRedis.init(redisConfiguration);
            return clusterRedis;
        }
        PooledRedis pooledRedis = new PooledRedis();
        pooledRedis.init(redisConfiguration);
        return pooledRedis;
    }

    /**
     * 构建redis客户端
     * 
     * @param jedisCluster
     * @param maxTimeoutInMillis 最大超时时间，包括链接池获取链接时间
     * @return
     */
    public static IRedis build(JedisCluster jedisCluster, int maxTimeoutInMillis) {
        return new ClusterRedis(jedisCluster, maxTimeoutInMillis);
    }

    /**
     * 构建redis客户端
     * 
     * @param jedisPool
     * @param maxTimeoutInMillis 最大超时时间，包括链接池获取链接时间
     * @return
     */
    public static IRedis build(Pool<Jedis> jedisPool, int maxTimeoutInMillis) {
        return new PooledRedis(jedisPool, maxTimeoutInMillis);
    }
}
