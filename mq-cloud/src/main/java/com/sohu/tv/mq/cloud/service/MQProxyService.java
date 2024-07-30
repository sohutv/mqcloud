package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.HttpConsumerConfig;
import com.sohu.tv.mq.cloud.bo.QueueOffset;
import com.sohu.tv.mq.cloud.common.util.CipherHelper;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import com.sohu.tv.mq.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MQ代理服务
 *
 * @author: yongfeigao
 * @date: 2022/6/7 9:18
 */
@Component
public class MQProxyService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CipherHelper cipherHelper;

    @Autowired
    private MQProxyServerChooser mqProxyServerChooser;

    /**
     * 获取集群队列偏移量
     *
     * @param consumer
     * @return
     */
    public Result<List<QueueOffset>> clusteringQueueOffset(String consumer) {
        String server = mqProxyServerChooser.choose();
        if (server == null) {
            return Result.getResult(Status.NO_RESULT);
        }
        String uriTemplate = "http://" + server + ":8081/mq/clustering/queue/offset?consumer={consumer}";
        URI url = restTemplate.getUriTemplateHandler().expand(uriTemplate, consumer);
        try {
            ResponseEntity<Result<List<QueueOffset>>> response = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<Result<List<QueueOffset>>>() {
                    });
            Result<List<QueueOffset>> result = response.getBody();
            if (logger.isDebugEnabled()) {
                logger.debug("url:{} result:{}", url, result);
            }
            return result;
        } catch (Exception e) {
            logger.error("queueOffset err, url:{}", url, e);
            return Result.getErrorResult(Status.WEB_ERROR, e);
        }
    }

    /**
     * 获取广播队列偏移量
     *
     * @param consumer
     * @return
     */
    public Result<Map<String, List<QueueOffset>>> broadcastQueueOffset(String consumer) {
        String server = mqProxyServerChooser.choose();
        if (server == null) {
            return null;
        }
        String uriTemplate = "http://" + server + ":8081/mq/broadcast/queue/offset?consumer={consumer}";
        URI url = restTemplate.getUriTemplateHandler().expand(uriTemplate, consumer);
        try {
            ResponseEntity<Result<Map<String, List<QueueOffset>>>> response = restTemplate.exchange(url,
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<Result<Map<String, List<QueueOffset>>>>() {
                    });
            Result<Map<String, List<QueueOffset>>> result = response.getBody();
            if (logger.isDebugEnabled()) {
                logger.debug("url:{} result:{}", url, result);
            }
            return result;
        } catch (Exception e) {
            logger.error("queueOffset err, url:{}", url, e);
        }
        return null;
    }

    /**
     * 消费者配置
     * @param userInfo
     * @param consumerConfigParam
     * @return
     */
    public Result<?> consumerConfig(UserInfo userInfo, ConsumerConfigParam consumerConfigParam) {
        String server = mqProxyServerChooser.choose();
        if (server == null) {
            logger.error("mq proxy server is null:{}", consumerConfigParam);
            throw new IllegalArgumentException("no proxy server");
        }
        // 设置header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("MU", cipherHelper.encrypt(userInfo.getUser().getEmail()));
        // 设置参数
        HttpEntity<String> request = new HttpEntity<String>(JSONUtil.toJSONString(consumerConfigParam), headers);
        String uriTemplate = "http://" + server + ":8081/mq/consumer/config";
        URI url = restTemplate.getUriTemplateHandler().expand(uriTemplate);
        try {
            ResponseEntity<Result<?>> response = restTemplate.exchange(url, HttpMethod.POST, request,
                    new ParameterizedTypeReference<Result<?>>(){});
            return response.getBody();
        } catch (Exception e) {
            logger.error("consumerConfig err, {}", consumerConfigParam, e);
            return Result.getWebErrorResult(e);
        }
    }

    /**
     * 解注册
     *
     * @param topic
     * @param consumer
     * @return
     */
    public Result<?> unregister(String topic, String consumer) {
        List<String> mqProxyServerList = mqProxyServerChooser.getAll();
        if (mqProxyServerList == null) {
            logger.error("mqProxyServerList is null");
            return null;
        }
        for (String server : mqProxyServerList) {
            for (int i = 0; i < 10; ++i) {
                String uriTemplate = "http://" + server + ":8080/mq/unregister?topic={topic}&consumer={consumer}";
                URI url = restTemplate.getUriTemplateHandler().expand(uriTemplate, topic, consumer);
                try {
                    Result<?> result = restTemplate.getForObject(url, Result.class);
                    if (result.isOK()) {
                        logger.info("unregister topic:{} consumer:{} ok", topic, consumer);
                    } else {
                        logger.warn("unregister topic:{} consumer:{} failed:{}", topic, consumer, result);
                    }
                    break;
                } catch (Exception e) {
                    logger.error("unregister err times{}, url:{}", i, url, e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
        return Result.getOKResult();
    }

    /**
     * 消费者配置
     *
     * @param consumer
     * @return
     */
    public Result<HttpConsumerConfig> getConsumerConfig(String consumer) {
        String server = mqProxyServerChooser.choose();
        if (server == null) {
            return null;
        }
        String uriTemplate = "http://" + server + ":8081/mq/config/{consumer}";
        URI url = restTemplate.getUriTemplateHandler().expand(uriTemplate, consumer);
        try {
            ResponseEntity<Result<HttpConsumerConfig>> response = restTemplate.exchange(url,
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<Result<HttpConsumerConfig>>() {
                    });
            Result<HttpConsumerConfig> result = response.getBody();
            if (logger.isDebugEnabled()) {
                logger.debug("url:{} result:{}", url, result);
            }
            return result;
        } catch (Exception e) {
            logger.error("getConsumerConfig err, url:{}", url, e);
            return Result.getWebErrorResult(e);
        }
    }

    /**
     * mq代理服务器选择器
     */
    @Component
    public static class MQProxyServerChooser {

        @Autowired
        private MQCloudConfigHelper mqCloudConfigHelper;

        private AtomicLong counter = new AtomicLong();

        private ConcurrentMap<String, List<String>> mqProxyServerListMap = new ConcurrentHashMap<>();

        /**
         * 选择一个server
         *
         * @return
         */
        public String choose() {
            String proxyServer = mqCloudConfigHelper.getMqProxyServerString();
            if (proxyServer == null) {
                return null;
            }
            List<String> mqProxyServerList = mqProxyServerListMap.computeIfAbsent(proxyServer,
                    k -> Arrays.asList(k.split(",")));
            long index = counter.incrementAndGet();
            if (index < 0) {
                index = 0;
                counter.set(index);
            }
            return mqProxyServerList.get((int) (index % mqProxyServerList.size()));
        }

        public List<String> getAll() {
            return mqProxyServerListMap.get(mqCloudConfigHelper.getMqProxyServerString());
        }
    }

    /**
     * 消费配置
     */
    public static class ConsumerConfigParam {
        private String consumer;
        private Integer maxPullSize;
        private Long consumeTimeoutInMillis;
        private Long pullTimeoutInMillis;
        private Integer pause;
        private String clientId;
        private Long resetOffsetTimestamp;
        private Integer rateLimitEnabled;
        private Double limitRate;
        // 重试的消息id
        private String retryMsgId;

        public String getConsumer() {
            return consumer;
        }

        public void setConsumer(String consumer) {
            this.consumer = consumer;
        }

        public Integer getMaxPullSize() {
            return maxPullSize;
        }

        public void setMaxPullSize(Integer maxPullSize) {
            this.maxPullSize = maxPullSize;
        }

        public Long getConsumeTimeoutInMillis() {
            return consumeTimeoutInMillis;
        }

        public void setConsumeTimeoutInMillis(Long consumeTimeoutInMillis) {
            this.consumeTimeoutInMillis = consumeTimeoutInMillis;
        }

        public Long getPullTimeoutInMillis() {
            return pullTimeoutInMillis;
        }

        public void setPullTimeoutInMillis(Long pullTimeoutInMillis) {
            this.pullTimeoutInMillis = pullTimeoutInMillis;
        }

        public Integer getPause() {
            return pause;
        }

        public void setPause(Integer pause) {
            this.pause = pause;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public Long getResetOffsetTimestamp() {
            return resetOffsetTimestamp;
        }

        public void setResetOffsetTimestamp(Long resetOffsetTimestamp) {
            this.resetOffsetTimestamp = resetOffsetTimestamp;
        }

        public Integer getRateLimitEnabled() {
            return rateLimitEnabled;
        }

        public void setRateLimitEnabled(Integer rateLimitEnabled) {
            this.rateLimitEnabled = rateLimitEnabled;
        }

        public Double getLimitRate() {
            return limitRate;
        }

        public void setLimitRate(Double limitRate) {
            this.limitRate = limitRate;
        }

        public String getRetryMsgId() {
            return retryMsgId;
        }

        public void setRetryMsgId(String retryMsgId) {
            this.retryMsgId = retryMsgId;
        }

        @Override
        public String toString() {
            return "ConsumerConfigParam{" +
                    "consumer='" + consumer + '\'' +
                    ", maxPullSize=" + maxPullSize +
                    ", consumeTimeoutInMillis=" + consumeTimeoutInMillis +
                    ", pullTimeoutInMillis=" + pullTimeoutInMillis +
                    ", pause=" + pause +
                    ", clientId='" + clientId + '\'' +
                    ", resetOffsetTimestamp=" + resetOffsetTimestamp +
                    ", rateLimitEnabled=" + rateLimitEnabled +
                    ", limitRate=" + limitRate +
                    ", retryMsgId='" + retryMsgId + '\'' +
                    '}';
        }
    }
}
