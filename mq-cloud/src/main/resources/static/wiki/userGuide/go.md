## 一、<span id="apply">前提条件</span>

请先参考[生产和消费](produceAndConsume)完成申请。

## 二、<span id="client">客户端依赖</span>

使用rocketmq社区的[rocketmq-client-go](https://github.com/apache/rocketmq-client-go)，此版本为纯go语言实现，请使用最新版本2.2.1-rc2，之前的版本有某些bug，会导致生产或消费问题。

## 三、<span id="common">公共配置</span>

1. log配置

   ```
   rlog.SetLogger(Logger)
   ```

2. GroupName

   生产组或消费组名，对应到MQCloud中如下：

   * 生产组名：[topic详情页](topic#detail)的生产者名字。
   * 消费组名：消费详情里的消费者名字。

3. 集群发现-HttpResolver

   ```
   primitive.NewHttpResolver("test-cluster", "http://${mqcloudDomain}/rocketmq/nsaddr-集群id")
   ```

   HttpResolver共两个参数：

   1. 集群名
   2. NameServer路由地址，可以点击复制[topic详情页](topic#detail)的集群路由。

4. 集群id

   ```
   consumer或者producer.WithInstance("pid or port"+"@集群id")
   ```

   集群id就是NameServer路由地址最后的数字。注意，必须追加集群id，否则跨集群时，使用一个通道导致异常，可依据[集群配置](cgo#appendix)进行配置。

## 四、<span id="produce">生产消息</span>

1. 初始化代码如下，其余请参考[官方实例](https://github.com/apache/rocketmq-client-go/blob/master/docs/Introduction.md)：

   ```
   p, _ := rocketmq.NewProducer(
       producer.WithGroupName("mqcloud-json-test-producer"),
       producer.WithNsResovler(primitive.NewHttpResolver("test-cluster", "http://${mqcloudDomain}/rocketmq/nsaddr-集群id")),
       producer.WithRetry(2),
   )
   ```

2. 启动

   ```
   err := p.Start()
   if err != nil {
       fmt.Printf("start producer error: %s", err.Error())
       os.Exit(1)
   }
   ```

   *注意，整个应用生命周期内，只用启动一次即可。*

3. 消息发送

   ```
   msg := &primitive.Message{
       Topic: topic,
       Body:  []byte("video 123 title changed"),
   }
   msg.WithKeys([]string{"123"})
   res, err := p.SendSync(context.Background(), msg)

   if err != nil {
       fmt.Printf("send message error: %s\n", err)
   }
   if res.Status != primitive.SendOK {
       // retry or degrade
   }
   fmt.Printf("send message success: result=%s\n", res.String())
   ```

   1. 推荐发送消息时指定WithKeys，keys为消息的标识，比如视频id为123的消息，可以通过[消息查询](/wiki/userGuide/messageQuery#key)模块按照123查询出所有这个视频变更的消息。
   2. 每条消息发送完毕应该检查返回值，不可丢失的消息在异常情况应该进行重试或降级处理。

4. 关闭

   在应用退出的时候进行关闭：

   ```
   err = p.Shutdown()
   if err != nil {
       fmt.Printf("shutdown producer error: %s", err.Error())
   }
   ```

## 五、<span id="consume">消费消息</span>

1. 初始化代码如下，其余请参考[官方实例](https://github.com/apache/rocketmq-client-go/blob/master/docs/Introduction.md)：

   ```
   c, _ := rocketmq.NewPushConsumer(
       consumer.WithGroupName("mqcloud-json-test-consumer"),
       consumer.WithNsResovler(primitive.NewHttpResolver("test-cluster", "http://${mqcloudDomain}/rocketmq/nsaddr-集群id")),
       consumer.WithConsumerModel(consumer.Clustering),
   )
   ```
   这里需要说明一下参数含义：

   * ConsumerModel分为消费方式：
     1. Clustering：所有的消费实例均分消息进行消费。
     2. BroadCasting：每个消费实例会消费所有的消息。

2. 订阅和消费逻辑

   ```
   err := c.Subscribe("mqcloud-json-test-topic", consumer.MessageSelector{}, func(ctx context.Context, msgs ...*primitive.MessageExt) (consumer.ConsumeResult, error) {
       for i := range msgs {
           fmt.Printf("subscribe callback: %v \n", msgs[i])
       }
       return consumer.ConsumeSuccess, nil
   })
   if err != nil {
       fmt.Println(err.Error())
   }
   ```

   注意事项

   1. 收到消息后请先打印到日志文件里，可以核对是否接到过该消息。

3. 启动

   ```
   err = c.Start()
   if err != nil {
       fmt.Println(err.Error())
       os.Exit(-1)
   }
   ```

   *注意，整个应用生命周期内，只用启动一次即可。*

4. 关闭

   在应用退出的时候进行关闭：

   ```
   err = c.Shutdown()
   if err != nil {
       fmt.Printf("shutdown Consumer error: %s", err.Error())
   }
   ```
## 六、<span id="appendix">集群配置</span>
生产环境集群配置列表：

| 集群id   | 集群名称            | 集群路由地址                                |
|:------:| :------:             | :------:                                      |
|   1    |index-cluster        | http://${mqcloudDomain}/rocketmq/nsaddr-1|
|   2    | core-cluster        | http://${mqcloudDomain}/rocketmq/nsaddr-2|
|   3    | test-cluster        | http://${mqcloudDomain}/rocketmq/nsaddr-3|
|   5    | transaction-cluster | http://${mqcloudDomain}/rocketmq/nsaddr-5|

   ​
