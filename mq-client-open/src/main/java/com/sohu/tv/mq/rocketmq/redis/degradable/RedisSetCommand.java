package com.sohu.tv.mq.rocketmq.redis.degradable;

import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.common.AbstractCommand;
import com.sohu.tv.mq.common.Alerter;
import com.sohu.tv.mq.common.DefaultAlerter;
import com.sohu.tv.mq.rocketmq.redis.IRedis;

import redis.clients.jedis.params.SetParams;

/**
 * redis set 命令
 * 
 * @author yongfeigao
 * @date 2021年10月14日
 */
public class RedisSetCommand extends AbstractCommand<Result<String>> {

    private IRedis redis;

    private String key;

    private String value;

    private SetParams params;

    public RedisSetCommand(IRedis redis, String key, String value, SetParams params) {
        this("redis-" + redis.hashCode(), "set", redis.getRedisConfiguration().getMaxTimeout() + 1000,
                DefaultAlerter.getInstance());
        this.redis = redis;
        this.key = key;
        this.value = value;
        this.params = params;
    }

    public RedisSetCommand(String groupKey, String commandKey, int timeout, Alerter alerter) {
        super(groupKey, commandKey, timeout, alerter);
    }

    @Override
    protected Result<String> invoke() throws Exception {
        return new Result<>(true, redis.set(key, value, params));
    }

    @Override
    protected Object invokeErrorInfo() {
        return key;
    }

    @Override
    public Result<String> fallback() {
        if (isFailedExecution()) {
            return new Result<>(false, getExecutionException());
        }
        return new Result<>(false);
    }
}
