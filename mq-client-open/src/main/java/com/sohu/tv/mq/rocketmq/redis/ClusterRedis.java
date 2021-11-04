package com.sohu.tv.mq.rocketmq.redis;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.util.Pool;

/**
 * ClusterRedis
 * 
 * @author yongfeigao
 * @date 2021年8月25日
 */
public class ClusterRedis implements IRedis {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    protected JedisCluster jedisCluster;
    
    private RedisConfiguration redisConfiguration;

    public ClusterRedis() {
    }
    
    public ClusterRedis(JedisCluster jedisCluster, int timeout) {
        this.jedisCluster = jedisCluster;
        this.redisConfiguration = new RedisConfiguration();
        redisConfiguration.setConnectionTimeout(timeout);
        logger.info("new ClusterRedis timeout:{}", timeout);
    }

    @Override
    public void init(RedisConfiguration redisConfiguration) {
        this.redisConfiguration = redisConfiguration;
        String[] hostAndPortArray = redisConfiguration.getHost().split(",");
        Set<HostAndPort> jedisClusterNode = new HashSet<HostAndPort>();
        for (String hostAndPort : hostAndPortArray) {
            String[] tmpArray = hostAndPort.split(":");
            jedisClusterNode.add(new HostAndPort(tmpArray[0], Integer.parseInt(tmpArray[1])));
        }
        jedisCluster = new JedisCluster(jedisClusterNode, redisConfiguration.getConnectionTimeout(),
                redisConfiguration.getSoTimeout(), 5, redisConfiguration.getPassword(),
                redisConfiguration.getPoolConfig());
        logger.info("init ClusterRedis redisConfiguration:{}", redisConfiguration);
    }

    @Override
    public Pool<Jedis> getPool() {
        return null;
    }

    @Override
    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    @Override
    public String set(String key, String value, SetParams params) {
        return jedisCluster.set(key, value, params);
    }
    
    @Override
    public String get(String key) {
        return jedisCluster.get(key);
    }
    
    @Override
    public RedisConfiguration getRedisConfiguration() {
        return redisConfiguration;
    }
}
