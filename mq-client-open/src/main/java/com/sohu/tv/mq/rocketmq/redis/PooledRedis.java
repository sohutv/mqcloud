package com.sohu.tv.mq.rocketmq.redis;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.util.Pool;

/**
 * 池化的redis
 * 
 * @author yongfeigao
 * @date 2021年8月25日
 */
public class PooledRedis implements IRedis {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private RedisConfiguration redisConfiguration;
    
    private Pool<Jedis> pool;

    public PooledRedis() {
    }
    
    public PooledRedis(Pool<Jedis> pool, int timeout) {
        this.pool = new JedisPool();
        this.redisConfiguration = new RedisConfiguration();
        redisConfiguration.setConnectionTimeout(timeout);
        logger.info("new PooledRedis timeout:{}", timeout);
    }

    @Override
    public void init(RedisConfiguration redisConfiguration) {
        this.redisConfiguration = redisConfiguration;
        this.pool = new JedisPool(redisConfiguration.getPoolConfig(),
                redisConfiguration.getHost(),
                redisConfiguration.getPort(),
                redisConfiguration.getConnectionTimeout(),
                redisConfiguration.getSoTimeout(),
                redisConfiguration.getPassword(),
                Protocol.DEFAULT_DATABASE, null);
        logger.info("init PooledRedis redisConfiguration:{}", redisConfiguration);
    }

    @Override
    public Pool<Jedis> getPool() {
        return pool;
    }

    @Override
    public JedisCluster getJedisCluster() {
        return null;
    }

    @Override
    public String set(String key, String value, SetParams params) {
        return execute(jedis -> jedis.set(key, value, params));
    }

    @Override
    public String get(String key) {
        return execute(jedis -> jedis.get(key));
    }

    private <R> R execute(Function<Jedis, R> function) {
        try (Jedis jedis = pool.getResource()) {
            return function.apply(jedis);
        }
    }

    public void setPool(Pool<Jedis> pool) {
        this.pool = pool;
    }

    @Override
    public RedisConfiguration getRedisConfiguration() {
        return redisConfiguration;
    }
}
