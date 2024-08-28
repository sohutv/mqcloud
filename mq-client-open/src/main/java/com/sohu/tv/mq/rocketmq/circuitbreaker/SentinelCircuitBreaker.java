package com.sohu.tv.mq.rocketmq.circuitbreaker;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DefaultCircuitBreakerRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.sohu.index.tv.mq.common.MQMessage;
import com.sohu.index.tv.mq.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * sentinel熔断器
 *
 * @author yongfeigao
 * @date 2024年07月31日
 */
public class SentinelCircuitBreaker {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // 资源名称
    private String resource;

    // 降级回调
    private Consumer fallbackConsumer;

    public SentinelCircuitBreaker(String resource, Consumer fallbackConsumer) {
        this.resource = resource;
        this.fallbackConsumer = fallbackConsumer;
        initDefaultRules();
    }

    /**
     * 进入资源
     */
    public void entry(MQMessage message) throws BlockException {
        message.setEntryResult(new Result(true, SphU.entry(resource, EntryType.OUT)));
    }

    /**
     * trace资源
     */
    public void trace(Throwable e, MQMessage message) {
        if (e instanceof BlockException) {
            fallback(message);
            return;
        }
        if (message.isEntryOK() && message.isRemoteError(e)) {
            Tracer.traceEntry(e, (Entry) message.getEntryResult().getResult());
        }
    }


    /**
     * 退出资源
     */
    public void exit(MQMessage message) {
        if (message.isEntryOK()) {
            ((Entry) message.getEntryResult().getResult()).exit();
        }
    }

    private void fallback(MQMessage message) {
        if (fallbackConsumer == null) {
            return;
        }
        try {
            fallbackConsumer.accept(message);
        } catch (Throwable ex) {
            // 降级失败不再往后传播异常
            logger.error("fallback error mqMessage:{}", message, ex);
        }
    }

    /**
     * 初始化默认规则
     */
    public void initDefaultRules() {
        Set<DegradeRule> degradeRules = new HashSet<>(DegradeRuleManager.getRules());
        Set<FlowRule> flowRules = new HashSet<>(FlowRuleManager.getRules());
        degradeRules.add(buildSlowRequestDegradeRule("*"));
        degradeRules.add(buildExceptionDegradeRule("*"));
        flowRules.add(buildFlowRule("*"));
        DegradeRuleManager.loadRules(new ArrayList<>(degradeRules));
        FlowRuleManager.loadRules(new ArrayList<>(flowRules));
        DefaultCircuitBreakerRuleManager.loadRules(new ArrayList<>(degradeRules));
    }

    /**
     * 慢请求比例
     *
     * @param resource
     * @return
     */
    public DegradeRule buildSlowRequestDegradeRule(String resource) {
        return new DegradeRule(resource)
                // Max allowed response time
                .setCount(1000)
                // Retry timeout (in second)
                .setTimeWindow(10)
                // Circuit breaker opens when slow request ratio > 60%
                .setSlowRatioThreshold(0.6)
                .setMinRequestAmount(20)
                .setStatIntervalMs(20000);
    }

    /**
     * 异常比例
     *
     * @param resource
     * @return
     */
    public DegradeRule buildExceptionDegradeRule(String resource) {
        return new DegradeRule(resource)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(0.8)
                .setTimeWindow(10)
                .setMinRequestAmount(5)
                .setStatIntervalMs(60000);
    }

    /**
     * 并发控制
     *
     * @return
     */
    public FlowRule buildFlowRule(String resource) {
        FlowRule flowRule = new FlowRule();
        flowRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        flowRule.setResource(resource);
        flowRule.setCount(100);
        return flowRule;
    }
}
