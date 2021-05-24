package com.sohu.tv.mq.netty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.protocol.RequestCode;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.common.protocol.header.GetConsumerRunningInfoRequestHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.netty.NettyRequestProcessor;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.metric.ConsumeFailedStat;
import com.sohu.tv.mq.metric.ConsumeStatManager;
import com.sohu.tv.mq.metric.ConsumeThreadStat;
import com.sohu.tv.mq.metric.StackTraceMetric;

import io.netty.channel.ChannelHandlerContext;

/**
 * sohu 客户端远程处理器
 * 
 * @author yongfeigao
 * @date 2021年4月14日
 */
public class SohuClientRemotingProcessor implements NettyRequestProcessor {
    private final MQClientInstance mqClientFactory;

    public SohuClientRemotingProcessor(final MQClientInstance mqClientFactory) {
        this.mqClientFactory = mqClientFactory;
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
        switch (request.getCode()) {
            case RequestCode.GET_CONSUMER_RUNNING_INFO:
                return this.getConsumerRunningInfo(ctx, request);
            default:
                break;
        }
        return null;
    }

    public RemotingCommand getConsumerRunningInfo(ChannelHandlerContext ctx,
            RemotingCommand request) throws RemotingCommandException {
        HashMap<String, String> map = request.getExtFields();
        ConsumerRunningInfo consumerRunningInfo = null;
        GetConsumerRunningInfoRequestHeader requestHeader = (GetConsumerRunningInfoRequestHeader) request
                .decodeCommandCustomHeader(GetConsumerRunningInfoRequestHeader.class);
        if (map != null && "true".equals(map.get("_thread_metric"))) {
            consumerRunningInfo = getConsumerThreadMetric(requestHeader);
        } else if (map != null && "true".equals(map.get("_failed_metric"))) {
            consumerRunningInfo = getConsumerFailedMetric(requestHeader);
        } else {
            consumerRunningInfo = getConsumerRunningInfo(requestHeader);
        }
        RemotingCommand response = RemotingCommand.createResponseCommand(null);
        if (null != consumerRunningInfo) {
            response.setCode(ResponseCode.SUCCESS);
            response.setBody(consumerRunningInfo.encode());
        } else {
            response.setCode(ResponseCode.SYSTEM_ERROR);
            response.setRemark(String.format("The Consumer Group <%s> not exist",
                    requestHeader.getConsumerGroup()));
        }
        return response;
    }

    public ConsumerRunningInfo getConsumerRunningInfo(GetConsumerRunningInfoRequestHeader requestHeader) {
        ConsumerRunningInfo consumerRunningInfo = this.mqClientFactory
                .consumerRunningInfo(requestHeader.getConsumerGroup());
        if (null != consumerRunningInfo) {
            if (requestHeader.isJstackEnable()) {
                Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
                String jstack = UtilAll.jstack(map);
                consumerRunningInfo.setJstack(jstack);
            }
        }
        return consumerRunningInfo;
    }

    /**
     * 获取消费线程堆栈
     * @param requestHeader
     * @return
     */
    public ConsumerRunningInfo getConsumerThreadMetric(GetConsumerRunningInfoRequestHeader requestHeader) {
        // 获取线程统计
        ConsumeThreadStat consumeThreadMetrics = ConsumeStatManager.getInstance()
                .getConsumeThreadMetrics(requestHeader.getConsumerGroup());
        if (consumeThreadMetrics == null) {
            return null;
        }
        List<StackTraceMetric> threadMetricList = consumeThreadMetrics.getAll();
        ConsumerRunningInfo consumerRunningInfo = new ConsumerRunningInfo();
        consumerRunningInfo.getProperties().put("threadMetricList", JSON.toJSONString(threadMetricList));
        return consumerRunningInfo;
    }
    
    /**
     * 获取消费失败堆栈
     * @param requestHeader
     * @return
     */
    public ConsumerRunningInfo getConsumerFailedMetric(GetConsumerRunningInfoRequestHeader requestHeader) {
        // 获取线程统计
        ConsumeFailedStat consumeFailedMetrics = ConsumeStatManager.getInstance()
                .getConsumeFailedMetrics(requestHeader.getConsumerGroup());
        if (consumeFailedMetrics == null) {
            return null;
        }
        List<StackTraceMetric> metricList = consumeFailedMetrics.getAll();
        ConsumerRunningInfo consumerRunningInfo = new ConsumerRunningInfo();
        consumerRunningInfo.getProperties().put("failedMetricList", JSON.toJSONString(metricList));
        return consumerRunningInfo;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }

}
