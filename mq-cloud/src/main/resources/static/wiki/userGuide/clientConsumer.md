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

1. json消费回调代码

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

2. 对象消费回调代码（假设对象为Video）

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

## 六、<span id="explain">Consumer部分参数释义<span>【如非有特殊需求不必修改】：

```
/**
 * 消费线程数，默认20
 * 
 * @param num
 */
public void setConsumeThreadMin(int num) {
    if (num <= 0) {
        return;
    }
    consumer.setConsumeThreadMin(num);
}
/**
 * 消费线程数，默认64
 * 
 * @param num
 */
public void setConsumeThreadMax(int num) {
    if (num <= 0) {
        return;
    }
    consumer.setConsumeThreadMax(num);
}
/**
 * queue中缓存多少个消息时进行流控 ，默认1000
 * 
 * @param size
 */
public void setPullThresholdForQueue(int size) {
    if (size < 0) {
        return;
    }
    consumer.setPullThresholdForQueue(size);
}
```

