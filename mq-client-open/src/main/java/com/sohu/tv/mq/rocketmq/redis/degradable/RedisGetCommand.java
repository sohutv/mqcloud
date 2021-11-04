package com.sohu.tv.mq.rocketmq.redis.degradable;

import org.apache.rocketmq.common.Pair;

import com.sohu.tv.mq.common.AbstractCommand;
import com.sohu.tv.mq.common.Alerter;
import com.sohu.tv.mq.common.DefaultAlerter;
import com.sohu.tv.mq.rocketmq.redis.IRedis;

/**
 * redis get 命令
 * 
 * @author yongfeigao
 * @date 2021年10月14日
 */
public class RedisGetCommand extends AbstractCommand<Pair<String, String>> {

    private IRedis redis;

    private String key;

    public RedisGetCommand(IRedis redis, String key) {
        this("redis-" + redis.hashCode(), "get", redis.getRedisConfiguration().getMaxTimeout() + 1000,
                DefaultAlerter.getInstance());
        this.redis = redis;
        this.key = key;
    }

    public RedisGetCommand(String groupKey, String commandKey, int timeout, Alerter alerter) {
        super(groupKey, commandKey, timeout, alerter);
    }

    @Override
    protected Pair<String, String> invoke() throws Exception {
        String result = redis.get(key);
        if (result != null) {
            String[] array = result.split(":");
            return new Pair<>(array[0], array[1]);
        }
        return null;
    }

    @Override
    protected Object invokeErrorInfo() {
        return key;
    }

    @Override
    public Pair<String, String> fallback() {
        return null;
    }
}
