#### 1. <span id="topic">能否用一个topic发送多种消息？</span>

最好一个topic只负责一类消息，topic的数量对MQ集群几乎无影响。

#### 2. <span id="producer">能否用同样的producer名（即producer group）往多个topic发送消息？</span>

可以发送成功，但是不要这样做。

#### 3. <span id="consumer">能否用同样的consumer名（即consumer group）消费多个topic的消息？</span>

不可以，consumer group与topic的关系是，多对一。

#### 4. <span id="message">消息采用什么格式发送？</span>

可以使用json或者map。

#### 5. <span id="send">三种消息发送方式的应用场景？</span>

1. 如果是通知类型消息，即消息可以丢失，推荐采用oneway方式发送。
2. 如果需要知道消息是否发送成功，但是不能阻塞主流程，推荐采用asyn方式发送。
3. 如果消息必须发送成功，不在乎是否阻塞主流程，推荐采用普通方式发送。
4. 以上三种方式都有对应的hystrix隔离版，可以在MQ集群故障时保障客户端主流程不阻塞。

#### 6. <span id="produceNotice">生产者注意事项：</span>

检查发送消息后的返回值，针对失败的消息进行重试发送或降级处理。

#### 7. <span id="cluster">【集群模式】消费者注意事项：</span>

针对需要重试的消息，消费失败需要抛出异常，这样会将失败的消息发回重试队列。

#### 8. <span id="tags">能否使用tags？</span>

不建议使用tags，理由如下：

1 说起tags不得不说consumer group，其必须在整个集群中全局唯一，否则会在消费时导致部分消息丢失的问题：[参见测试](https://blog.csdn.net/a417930422/article/details/50663639)，而MQCloud在业务层面保证了这个唯一性。

2 那么跟tags有什么关系？关系就是同一个topic，同样的consumer group，使用不同的tags，会导致和consumer group一样的问题。

也就是说**topic<->consumer group<->tags需要一一对应！**

3 引起这两个问题的原因都跟rocketmq心跳机制有关，具体类可以参见[ConsumerManager](https://github.com/apache/rocketmq/blob/master/broker/src/main/java/org/apache/rocketmq/broker/client/ConsumerManager.java)，中的结构：

```
private final ConcurrentMap<String/* Consumer Group */, ConsumerGroupInfo> consumerTable = new ConcurrentHashMap<String, ConsumerGroupInfo>(1024);
```

4 如果自己能确保上述的一一对应关系，可以参考如下相关代码：

```
// 生产者：注意一条消息只支持设置一个tag
producer.publish(msg, tags, null, null);
// 消费者：在启动之前设置
consumer.setSubExpression("tagA || tagB");
```

5 tags替代方案：消息体增加type字段，各个消费者自己过滤。

#### 9. <span id="knownIssue">已知问题：</span>

1 org.apache.rocketmq.client.exception.MQBrokerException: CODE: 25 DESC: the consumer's subscription not latest。

该问题是rocketmq4.2版本的bug，拉取消息流程控制不严格导致，但是并不影响消息消费，在4.2版本出现，在4.3版本修复，[参见](https://github.com/apache/rocketmq/issues/370)。

2 org.apache.rocketmq.client.exception.MQBrokerException: CODE: 2 DESC: [TIMEOUT_CLEAN_QUEUE]broker busy, start flow control for a while

该问题是由于rocketmq4.1之后broker针对处理发送过来的请求增加了快速失败机制，对于响应超过200ms的请求移除队列。默认broker端采用单线程和spin lock来处理。引起的原因可能是SYN_FLUSH,SYN_MASTER,gc,iops过高等,[参考1](https://stackoverflow.com/questions/43154365/rocketmqmqbrokerexception-code-2-desc-timeout-clean-queue),[参考2](https://issues.apache.org/jira/browse/ROCKETMQ-311)。