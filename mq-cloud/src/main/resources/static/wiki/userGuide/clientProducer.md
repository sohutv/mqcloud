## 一、<span id="spring-boot">初始化之spring-boot方式</span>

**老用户请先通过“我是老用户”入口关联生产者**

```
@Configuration
public class MQConfiguration {
    @Value("${flushCache.producer}")
    private String flushCacheProducer;

    @Value("${flushCache.topic}")
    private String flushCacheTopic;
    
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ${producer} flushCacheProducer() {
        return new ${producer}(flushCacheProducer, flushCacheTopic);
    }
}
```

## 二、<span id="spring-xml">初始化之spring xml方式</span>

```
<!-- 采用spring xml方式 -->			
<bean id="xxxProducer" class="com.sohu.tv.mq.rocketmq.${producer}" init-method="start" destroy-method="shutdown">
    <constructor-arg index="0" value="${请从topic详情查询生产者的producer group}"></constructor-arg>
    <constructor-arg index="1" value="${topic名字}"></constructor-arg>
</bean>
```

## 三、<span id="java">初始化之java方式</span>

```
// 生产者初始化 注意：只用初始化一次
${producer} producer = new ${producer}("xxx-producer", "xxx-topic");
// 注意，只用启动一次
producer.start();
// 应用退出时
producer.shutdown();
```

## 四、<span id="produceMessage">发送普通消息示例</span>：

```
Map<String, Object> message = new HashMap<String, Object>();
message.put("vid", "123456");
message.put("aid", "789172");
//这个例子message使用map，当然也可以使用json
//建议设置keys(多个key用空格分隔)参数(也可以忽略该参数)，比如keys指定为vid，那么就可以根据vid查询消息
Result<SendResult> sendResult = producer.publish(message);
if(!sendResult.isSuccess){
    //失败消息处理
}
```

## 五、<span id="produceOrderMessage">发送有序消息示例</span>

```
/**
 * 相同的id发送到同一个队列
 * hash方法：id % 队列数
 */
class IDHashMessageQueueSelector implements MessageQueueSelector {
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object idObject) {
        long id = (Long) idObject;
        int size = mqs.size();
        int index = (int) (id % size);
        return mqs.get(index);
    }
}
// 设置到producer
producer.setMessageQueueSelector(new IDHashMessageQueueSelector());
// 消息发送
long id = 123L;
Map<String, Object> map = new HashMap<String, Object>();
map.put("id", id);
Result<SendResult> sendResult = producer.publishOrder(map, String.valueOf(id), id);
```

## <span id="produceTransMessage">六、 发送事务消息示例</span>

```
// 1.定义实现事务回调接口
TransactionListener transactionListener = new TransactionListener() {
    /**
     * 在此方法执行本地事务
     */
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        // arg可以传业务id
        int id = (Integer) arg;
        // 确定事务状态，未知返回：UNKNOW，回滚返回：ROLLBACK_MESSAGE，成功返回：COMMIT_MESSAGE，抛出异常默认为：UNKNOW
        return LocalTransactionState.COMMIT_MESSAGE;
    }

    /**
     * 如果executeLocalTransaction返回UNKNOW，rocketmq会回调此方法查询事务状态，默认每分钟查一次，最多查询15次，状态还是UNKNOW的话，丢弃消息
     */
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        String key = msg.getKeys();
        int id = Integer.valueOf(key);
        return LocalTransactionState.COMMIT_MESSAGE;
    }
};

// 2.发送事务消息
// 初始化
${producer} producer = new ${producer}(producerGroup, topic, transactionListener);
// 组装消息
int id = 123;
Map<String, Object> map = new HashMap<String, Object>();
map.put("id", id);
map.put("msg", "msg" + id);
// 发送
Result<SendResult> sendResult = producer.publishTransaction(JSON.toJSONString(map), String.valueOf(id), id);
if(!sendResult.isSuccess){
    //失败消息处理
}
```

##  七、 <span id="hystrix">隔离发送消息示例</span>【hystrix版：MQ集群如果出现故障，将会拖慢发送方，故提供了hystrix版，以保证即使MQ集群整体不可用，也不会拖死发送方】

```
Map<String, String> map = new HashMap<String, String>();
map.put("aid", "123456");
map.put("vid", "765432");
// 1.oneway方式 - 此种方式发送效率最高，但是无法获取返回的结果
new PublishOnewayCommand(producer, map).execute();
// 2.async方式 - 此种方式发送效率高于普通方式，可以通过异步回调的方式校验返回结果
SendCallback sendCallback = new SendCallback() {
            public void onSuccess(SendResult sendResult) {
                // 成功回调
            }
            public void onException(Throwable e) {
                // 失败回调
            }
        };
new PublishAsyncCommand(producer, map, sendCallback).execute();
// 3.普通方式 - 此种方式即为普通方式的hystrix封装，与普通发送方式无异
Result<SendResult> result = new PublishCommand(producer, map).execute();
```

注意：hystrix配置默认采用线程池隔离，容量为30，超时时间为rocketmq客户端默认超时3s，如果使用hystrix版，还需要显示依赖hystrix，如下：

```
<dependency>
    <groupId>com.netflix.hystrix</groupId>
    <artifactId>hystrix-core</artifactId>
    <version>1.3.20</version>
</dependency>
```
