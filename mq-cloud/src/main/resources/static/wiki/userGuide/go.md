## 一、<span id="apply">前提条件</span>

请先参考[生产和消费](produceAndConsume)完成申请。

## 二、<span id="client">客户端依赖</span>

推荐使用rocketmq社区的[rocketmq-client-go](https://github.com/apache/rocketmq-client-go)。

**注意事项：**

由于rocketmq-client-go依赖rocketmq-client-cpp(rocketmq的c++客户端)，所以首先需要安装rocketmq-client-cpp，安装注意事项请参考[官方指南](https://github.com/apache/rocketmq-client-cpp)。

*如果采用自己build的方式，最终产生的librocketmq.so和librocketmq.a需要放到/usr/local/lib下。*

*并且将/usr/local/lib加入到/etc/ld.so.conf中，执行ldconfig，以便能搜到动态库。* 

## 三、<span id="produce">生产消息</span>

1. 引入rocketmq-client-go

   ```
   import (
   	"github.com/apache/rocketmq-client-go/core"
   )
   ```

2. 初始化配置

   ```
   config := &rocketmq.ProducerConfig{ClientConfig: rocketmq.ClientConfig{
   	GroupID: "mqcloud-json-test-topic-producer",
   	NameServerDomain: "http://${mqcloudDomain}/rocketmq/nsaddr-3",
   }}
   ```

   参数释义

   1. GroupID：producerGroup，可以参考[topic详情页](topic#detail)的producer group。
   2. NameServerDomain：NameServer的域名，不同的集群对应不同的id。

3. 创建生产者实例

   ```
   producer, err := rocketmq.NewProducer(config)
   if err != nil {
   	fmt.Println("create Producer failed, error:", err.Error())
   	return
   }
   err = producer.Start()
   if err != nil {
   	fmt.Println("start producer error", err.Error())
   	return
   }
   ```

   注意事项

   1. 每个GroupID对应的producer在应用整个生命周期里只应该创建一次。
   2. 之后可以用该producer实例多次发送消息，不用每次发送都创建。
   3. 在应用退出时应该调用producer.shutdown()进行关闭。

4. 发送消息

   ```
   msg := "video 123 changed";
   result, err := producer.SendMessageSync(&rocketmq.Message{Topic: "mqcloud-json-test-topic", Body: msg, Keys: "123"})
   if err != nil {
   	fmt.Println("Error:", err)
   	// retry or degrade
   }
   if result.Status != rocketmq.SendOK {
     	// retry or degrade
   }
   fmt.Printf("send message ok: %s result: %s\n", msg, result)
   ```

   1. 推荐发送消息时指定keys参数，keys为消息的标识，比如视频id为123的消息，可以通过[消息查询](messageQuery#key)模块按照123查询出所有这个视频变更的消息。
   2. 每条消息发送完毕应该检查返回值，不可丢失的消息在异常情况应该进行重试或降级处理。

## 四、<span id="consume">消费消息</span>

1. 引入rocketmq-client-go

   ```
   import (
   	"github.com/apache/rocketmq-client-go/core"
   )
   ```

2. 初始化配置

   ```
   config := &rocketmq.PushConsumerConfig{
       ClientConfig: rocketmq.ClientConfig{
           GroupID: "mqcloud-json-test-consumer",
           NameServerDomain: "http://${mqcloudDomain}/rocketmq/nsaddr-3",
       },
       Model: rocketmq.Clustering,
       ConsumerModel: rocketmq.CoCurrently,
   }
   ```

   参数释义

   1. GroupID：consumerGroup，可以参考[消费详情](topic#consume)的consumer group。
   2. NameServerDomain：NameServer的域名，不同的集群对应不同的id。
   3. Model：消费方式
      1. Clustering：所有的消费者均分消息进行消费。
      2. BroadCasting：每个消费者会消费所有的消息。
   4. ConsumerModel：消费顺序
      1. CoCurrently：并发消费
      2. Orderly：顺序消费，具体参考[释义](clientConsumer#orderConsumer)。

3. 创建消费者实例

   ```
   consumer, err := rocketmq.NewPushConsumer(config)
   if err != nil {
       println("create Consumer failed, error:", err.Error())
       return
   }
   ```

   注意事项：

   每个GroupID对应的消费者在应用整个生命周期里只应创建一次。

4. 订阅topic并启动

   ```
   consumer.Subscribe("mqcloud-json-test-topic", "*", func(msg *rocketmq.MessageExt) rocketmq.ConsumeStatus {
       fmt.Printf("receive message: \"%s\" \n", msg.Body)
       // 具体的消费逻辑省略...
       // 如果消费成功返回如下
       return rocketmq.ConsumeSuccess
       // 如果消费失败返回如下
       return rocketmq.ReConsumeLater
   })
   err = consumer.Start()
   if err != nil {
       println("consumer start failed,", err.Error())
       return
   }
   ```

   注意事项

   1. 收到消息后请先打印到日志文件里，可以核对是否接到过该消息。
   2. 消费逻辑请务必捕获异常。
   3. 如果消费失败(比如数据库故障)务必返回rocketmq.ReConsumeLater，这样此消息会发送回broker，并在此后一段时间内进行重试消费。
   4. 每个GroupID对应的消费者在应用整个生命周期里只应启动一次。

5. 安全关闭

   应用退出时请调用consumer.Shutdown()进行安全关闭。

## 五、<span id="other">其余事项</span>

1. rocketmq日志默认在$HOME/logs/rocketmq-cpp下，可以参考定位问题。
2. 其余注意事项请参考[常见问题](faq)。
3. 完整用法示例可以参考[官方样例](https://github.com/apache/rocketmq-client-go/tree/master/demos)。

