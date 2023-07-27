package com.sohu.tv.mq.rocketmq.netty;

import com.sohu.tv.mq.metric.ConsumeFailedStat;
import com.sohu.tv.mq.metric.ConsumeStatManager;
import com.sohu.tv.mq.metric.ConsumeThreadStat;
import com.sohu.tv.mq.metric.StackTraceMetric;
import com.sohu.tv.mq.rocketmq.RocketMQConsumer;
import com.sohu.tv.mq.util.Constant;
import com.sohu.tv.mq.util.JSONUtil;
import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.header.GetConsumerRunningInfoRequestHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.netty.NettyRequestProcessor;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sohu 客户端远程处理器
 * 
 * @author yongfeigao
 * @date 2021年4月14日
 */
@SuppressWarnings("deprecation")
public class SohuClientRemotingProcessor implements NettyRequestProcessor {
    private RocketMQConsumer rocketMQConsumer;

    public SohuClientRemotingProcessor(RocketMQConsumer rocketMQConsumer) {
        this.rocketMQConsumer = rocketMQConsumer;
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

    /**
     * 获取运行时信息
     * 
     * @param ctx
     * @param request
     * @return
     * @throws RemotingCommandException
     */
    public RemotingCommand getConsumerRunningInfo(ChannelHandlerContext ctx,
            RemotingCommand request) throws RemotingCommandException {
        HashMap<String, String> map = request.getExtFields();
        ConsumerRunningInfo consumerRunningInfo = null;
        GetConsumerRunningInfoRequestHeader requestHeader = (GetConsumerRunningInfoRequestHeader) request
                .decodeCommandCustomHeader(GetConsumerRunningInfoRequestHeader.class);
        if (map != null && Constant.COMMAND_TRUE.equals(map.get(Constant.COMMAND_THREAD_METRIC))) {
            consumerRunningInfo = getConsumerThreadMetric(requestHeader);
        } else if (map != null && Constant.COMMAND_TRUE.equals(map.get(Constant.COMMAND_FAILED_METRIC))) {
            consumerRunningInfo = getConsumerFailedMetric(requestHeader);
        } else if (map != null && map.get(Constant.COMMAND_TIMESPAN_TOPIC) != null) {
            consumerRunningInfo = consumeTimespanMessage(requestHeader, map);
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

    /**
     * 原生的获取运行时信息
     * 
     * @param requestHeader
     * @return
     */
    public ConsumerRunningInfo getConsumerRunningInfo(GetConsumerRunningInfoRequestHeader requestHeader) {
        ConsumerRunningInfo consumerRunningInfo = rocketMQConsumer.getConsumer().getDefaultMQPushConsumerImpl()
                .getmQClientFactory().consumerRunningInfo(requestHeader.getConsumerGroup());
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
     * 
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
        consumerRunningInfo.getProperties().put(Constant.COMMAND_VALUE_THREAD_METRIC, JSONUtil.toJSONString(threadMetricList));
        return consumerRunningInfo;
    }

    /**
     * 获取消费失败堆栈
     * 
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
        consumerRunningInfo.getProperties().put(Constant.COMMAND_VALUE_FAILED_METRIC, JSONUtil.toJSONString(metricList));
        return consumerRunningInfo;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }

    /**
     * 消费时间段消息
     * 
     * @param ctx
     * @param request
     * @return
     * @throws RemotingCommandException
     */
    private ConsumerRunningInfo consumeTimespanMessage(GetConsumerRunningInfoRequestHeader requestHeader,
            HashMap<String, String> map) throws RemotingCommandException {
        String topic = map.get(Constant.COMMAND_TIMESPAN_TOPIC);
        long start = Long.parseLong(map.get(Constant.COMMAND_TIMESPAN_START));
        long end = Long.parseLong(map.get(Constant.COMMAND_TIMESPAN_END));
        rocketMQConsumer.consumeMessage(topic, requestHeader.getConsumerGroup(), start, end);
        return new ConsumerRunningInfo();
    }
}
