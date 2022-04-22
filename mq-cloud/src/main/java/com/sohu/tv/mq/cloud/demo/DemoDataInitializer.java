package com.sohu.tv.mq.cloud.demo;

import com.sohu.index.tv.mq.common.MQMessage;
import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.rocketmq.RocketMQConsumer;
import com.sohu.tv.mq.rocketmq.RocketMQProducer;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * demo环境数据初始化
 *
 * @author: yongfeigao
 * @date: 2022/4/19 10:43
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ServerDataService serverDataService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private NameServerService nameServerService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private UserConsumerService userConsumerService;

    @Autowired
    private CommonConfigService commonConfigService;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Override
    public void run(String... args) throws Exception {
        String env = System.getProperty("mqcloud.env");
        if (!"demo".equals(env)) {
            logger.info("mqcloud.env={} return", env);
            return;
        }
        String topicName = "mqcloud-demo-topic";
        String topicProducer = "mqcloud-demo-topic-producer";
        String topicConsumer = "mqcloud-demo-consumer";
        Result<Topic> topicResult = topicService.queryTopic(topicName);
        if (topicResult.isOK()) {
            logger.info("data has initialized!");
            // 启动生产消费
            produceAndConsume(topicName, topicProducer, topicConsumer);
            return;
        }

        int cid = 1;
        // 1.等待broker注册
        Cluster cluster = clusterService.getMQClusterById(cid);
        while (true) {
            ClusterInfo clusterInfo = mqAdminTemplate.execute(new DefaultCallback<ClusterInfo>() {
                public ClusterInfo callback(MQAdminExt mqAdmin) throws Exception {
                    return mqAdmin.examineBrokerClusterInfo();
                }
                public ClusterInfo exception(Exception e) {
                    logger.error("examineBrokerClusterInfo err", e);
                    return null;
                }
                public Cluster mqCluster() {
                    return cluster;
                }
            });
            if (clusterInfo != null && clusterInfo.getBrokerAddrTable() != null && clusterInfo.getBrokerAddrTable().size() > 0) {
                logger.info("brokerClusterInfo:{}", clusterInfo.getBrokerAddrTable());
                break;
            }
            logger.info("wait broker register to ns, sleep 3000");
            Thread.sleep(3000);
        }

        // 2.添加topic
        Cluster mqCluster = clusterService.getMQClusterById(cid);
        Audit audit = new Audit();
        audit.setInfo("demo topic");
        audit.setUid(1);
        AuditTopic auditTopic = new AuditTopic();
        auditTopic.setName(topicName);
        auditTopic.setQueueNum(8);
        auditTopic.setProducer(topicProducer);
        Result<?> createTopicResult = topicService.createTopic(mqCluster, audit, auditTopic);
        if (createTopicResult.isNotOK()) {
            if (createTopicResult.getException() != null) {
                logger.error("create topic failed", createTopicResult.getException());
            } else {
                logger.error("create topic failed:{}", createTopicResult.getMessage());
            }
            // broker直接创建topic
            return;
        } else {
            logger.info("create topic:{} OK!", topicName);
        }

        // 3.添加消费者
        topicResult = topicService.queryTopic(topicName);
        Topic topic = topicResult.getResult();
        UserConsumer userConsumer = new UserConsumer();
        userConsumer.setUid(audit.getUid());
        userConsumer.setTid(topic.getId());
        Consumer consumer = new Consumer();
        consumer.setInfo("demo 消费");
        consumer.setTid(topic.getId());
        consumer.setName(topicConsumer);
        Result<?> createUserConsumerResult = userConsumerService.saveUserConsumer(cluster, userConsumer, consumer);
        if (createUserConsumerResult.isNotOK()) {
            logger.error("create consumer failed", createUserConsumerResult.getException());
            return;
        } else {
            logger.info("create consumer:{} OK!", topicConsumer);
        }

        // 启动生产消费
        produceAndConsume(topicName, topicProducer, topicConsumer);
    }

    /**
     * 生产和消费
     *
     * @param topic
     * @param producerGroup
     * @param consumerGroup
     */
    public void produceAndConsume(String topic, String producerGroup, String consumerGroup) {
        new Thread() {
            public void run() {
                try {
                    Result<Topic> topicResult = topicService.queryTopic(topic);
                    while (true) {
                        TopicRouteData topicRouteData = topicService.route(topicResult.getResult());
                        if (topicRouteData != null && topicRouteData.getBrokerDatas() != null) {
                            break;
                        }
                        logger.info("waiting topic route...{}", topicRouteData);
                        Thread.sleep(10000);
                    }

                    // 1.启动消费者
                    RocketMQConsumer rocketMQConsumer = new RocketMQConsumer(consumerGroup, topic);
                    rocketMQConsumer.setMqCloudDomain("127.0.0.1:8080");
                    AtomicLong counter = new AtomicLong();
                    rocketMQConsumer.setConsumerCallback((msg, msgExt) -> {
                        if (counter.incrementAndGet() % 10 == 0) {
                            logger.info("receive msg count:{}", counter.get());
                        }
                    });
                    rocketMQConsumer.start();

                    // 2.启动生产者
                    RocketMQProducer producer = new RocketMQProducer(producerGroup, topic);
                    producer.setMqCloudDomain("127.0.0.1:8080");
                    producer.start();
                    for (int i = 0; i < Long.MAX_VALUE; ++i) {
                        try {
                            producer.send(MQMessage.build("demo message" + i));
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    logger.error("start produceAndConsume err", e);
                }
            }
        }.start();
    }
}
