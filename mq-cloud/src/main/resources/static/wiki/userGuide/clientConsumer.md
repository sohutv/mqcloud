## 一、<span id="spring-boot">初始化之spring-boot方式</span>

**老用户请先通过“我是老用户”入口关联消费者**

```
@Configuration
public class MQConfiguration {

    @Value("${flushCache.consumerGroup}")
    private String flushCacheConsumer;

    @Value("${flushCache.topic}")
    private String flushCacheTopic;
    
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ${consumer} flushCacheConsumer(FlushCacheConsumerCallback consumerCallback) {
        ${consumer} consumer = new ${consumer}(flushCacheConsumer, flushCacheTopic);
        consumer.setConsumerCallback(consumerCallback);
        return consumer;
    }
}
```

## 二、<span id="spring-xml"> 初始化之spring xml方式</span>

```
<bean id="consumer" class="com.sohu.tv.mq.rocketmq.${consumer}"
    init-method="start" destroy-method="shutdown">
    <constructor-arg index="0" value="${请从topic详情查询消费者的consumer group}"/>
    <constructor-arg index="1" value="${topic名字}"/>
    <!-- 如果topic的消息格式是map，可以使用此属性 -->
    <property name="consumerExecutor" ref="consumerExecutorBean"/> 
    <!-- 如果topic的消息格式是map, byte[], String或其他格式，可以使用此属性(兼容所有类型) -->
    <property name="consumerCallback" ref="consumerCallbackBean"/> 
</bean>
```

##  三、<span id="java">初始化之java方式</span>

```
// 消费者初始化 注意：只用初始化一次
${consumer} consumer = new ${consumer}("xxx-consumer", "xxx-topic");
// 设置消费回调
consumer.setConsumerCallback(consumerCallback);
// 注意，只用启动一次
consumer.start();
// 应用退出时
consumer.shutdown();
```

 ## 四、<span id="consumerCallback">消费回调代码</span>

1. <span id="consumeJson">json消费回调代码</span>

   ```
   ConsumerCallback consumerCallback = new ConsumerCallback<String, MessageExt>() {
       public void call(String t, MessageExt k) {
               try {
                   // 打印日志
                   logger.info("msg:{}, msgExt:{}", t, k);
                   // 消费逻辑
               } catch (Exception e) {
                   logger.error("consume err, msgid:{}, msg:{}", k.getMsgId(), t, e);
                   // 如果需要重新消费，这里需要把异常抛出，消费失败的消息将发回rocketmq，重试消费
                   throw e;
               }
       }
   }
   ```

2. <span id="consumeObject">对象消费回调代码（假设对象为Video）</span>

   ```
   ConsumerCallback consumerCallback = new ConsumerCallback<Video, MessageExt>() {
       public void call(Video t, MessageExt k) {
               try {
                   // 打印日志
                   logger.info("msg:{}, msgExt:{}", t, k);
                   // 消费逻辑
               } catch (Exception e) {
                   logger.error("consume err, msgid:{}, msg:{}", k.getMsgId(), t, e);
                   // 如果需要重新消费，这里需要把异常抛出，消费失败的消息将发回rocketmq，重试消费
                   throw e;
               }
       }
   }
   ```

3. map消费回调代码

   ```
   ConsumerCallback consumerCallback = new ConsumerCallback<Map<String, Object>, MessageExt>() {
       public void call(Map<String, Object> t, MessageExt k) {
               try {
                   // 打印日志
                   logger.info("msg:{}, msgExt:{}", t, k);
                   // 消费逻辑
               } catch (Exception e) {
                   logger.error("consume err, msgid:{}, msg:{}", k.getMsgId(), t, e);
                   // 如果需要重新消费，这里需要把异常抛出，消费失败的消息将发回rocketmq，重试消费
                   throw e;
               }
       }
   }
   ```

   ​

## 五、<span id="offset">广播模式消费者需要注意</span>

广播模式offset默认存储在应用服务器~/.rocketmq_offsets文件夹下，如果应用部署在docker上，重新部署会导致offset文件丢失，丢失后默认会从broker上拉取最新的offset，那么可能会导致部分消息消费不到。可以通过单独指定offset存储的目录来防止这种情况：

```
-Drocketmq.client.localOffsetStoreDir=/data/logs/.rocketmq_offsets
```

## 六、<span id="explain">消费常见问题<span>：

1. 如何控制本地缓存的消息量？

   由于消息是先从broker拉取到本地，然后再进行消费的，那为了防止把本地内存打爆，可以通过如下参数控制：

   1.`pullThresholdForQueue`：每个本地队列缓存的消息数量，默认1000。

   消费者本地缓存的消息数量为：总队列数*`pullThresholdForQueue`

   例如，如下topic总队列数量为：48，那么默认缓存消息数量为48 * 1000 = 4.8万条

   ![](img/cc1.png)

   2.`pullThresholdForTopic`：针对整个topic限制消息数量，默认无限制。

   此参数优先级高于`pullThresholdForQueue`。

   3.对于以上两个参数，有对应的根据消息大小来设置的参数，分别为`pullThresholdSizeForQueue`(默认为100)和`pullThresholdSizeForTopic`(默认无限制)，其单位为M。

   **以上参数仅仅是设置消费者本地缓存的消息量，达到阈值时，会进行限流操作：即不再从broker拉取消息到本地缓存。**

   4.`pullBatchSize`：控制每个队列每次拉取多少条消息，默认最大32条(broker端有限制)。

   5.`pullInterval(默认为0)`：控制每个队列每隔多长时间从broker拉取一次消息，默认不停拉取。

2. 如何控制消费并发量？

   1.可以通过如下参数控制消费的线程数：

   `consumeThreadMin(默认为20)`和`consumeThreadMax(默认为64)`，默认至少有20个消费线程。

   例如，将`consumeThreadMin`和`consumeThreadMax`同时设置为1，这样就变成单线程消费了。

   2.可以通过如下参数控制多少条消息作为一批被某个线程消费：

   `consumeMessageBatchMaxSize(默认为1)`，默认表示每条消息需要一个线程来处理。

3. 每秒消费最多1000条消息，该如何实现？

   假设broker数量为2，每个broker上8个队列，总队列数为16。

   ```
   pullThresholdForQueue=1000/16≈63
   pullBatchSize=20
   pullInterval=300
   ```

   释义：这样每个队列每秒拉取20*(1000/300)=60条消息，总缓存消息1000条。

   如果不想计算队列数量怎么办？

   可以设置`pullThresholdForTopic=1000`，但是这个不是很准确，因为需要计算队列数，进行均分。

   ​

