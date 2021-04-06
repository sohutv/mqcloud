## 一、<span id="apply">前提条件</span>

请先参考[生产和消费](produceAndConsume)完成申请。

## 二、<span id="client">客户端依赖</span>

推荐使用rocketmq社区的[rocketmq-client-python](https://github.com/apache/rocketmq-client-python)。

**注意事项：**

由于rocketmq-client-python依赖rocketmq-client-cpp(rocketmq的c++客户端)，所以首先需要安装rocketmq-client-cpp，安装注意事项请参考[官方指南](https://github.com/apache/rocketmq-client-cpp)。

*如果采用自己build的方式，最终产生的librocketmq.so和librocketmq.a需要放到/usr/local/lib下。*

*并且将/usr/local/lib加入到/etc/ld.so.conf中，执行ldconfig，以便能搜到动态库。* 

## 三、<span id="produce">生产消息</span>

1. 引入rocketmq-client-python

   ```
   from rocketmq.client import Producer, Message, SendStatus
   ```

2. 初始化配置

   ```
   topic = 'index-test-python-topic'
   group_id = 'index-test-python-topic-producer'
   name_server_domain = 'http://${mqcloudDomain}/rocketmq/nsaddr-3'
   ```

   参数释义

   1. topic：topic名称
   2. group_id：producerGroup，可以参考[topic详情页](topic#detail)的producer group。
   3. name_server_domain：NameServer的域名，不同的集群对应不同的id。

3. 创建生产者实例

   ```
   try:
       producer = Producer(group_id)
       producer.set_name_server_domain(name_server_domain)
       producer.start()
   except Exception as e:
       print('create or start Producer failed, error:', e)
   ```

   注意事项

   1. 每个group_id对应的producer在应用整个生命周期里只应该创建一次。
   2. 之后可以用该producer实例多次发送消息，不用每次发送都创建。
   3. 在应用退出时应该调用<font color=red>producer.shutdown()</font>进行关闭。

4. 发送消息

   ```
   message = Message(topic)
   message.set_keys('9876')
   msg_body = '{v=9876, change_type=1}'
   message.set_body(msg_body)
   send_result = producer.send_sync(message)
   if send_result.status != SendStatus.OK:
           print('send msg failed')
           # retry or degrade
           # ....
   print ('send message ok, msg: %s, result: %s ' % (msg_body, send_result))
   ```

   1. 推荐发送消息时指定keys参数，keys为消息的标识，比如视频id为9876的消息，可以通过[消息查询](messageQuery#key)模块按照9876查询出所有这个视频变更的消息。
   2. 每条消息发送完毕应该检查返回值，不可丢失的消息在异常情况应该进行重试或降级处理。

## 四、<span id="consume">消费消息</span>

1. 引入rocketmq-client-python

   ```
   from rocketmq.client import PushConsumer, ConsumeStatus
   from rocketmq.ffi import MessageModel
   ```

2. 初始化配置

   ```
   topic = 'index-test-python-topic'
   group_id = 'index-test-python-topic-consumer01'
   name_server_domain = 'http://${mqcloudDomain}/rocketmq/nsaddr-3'
   model = MessageModel.CLUSTERING
   ```

   参数释义

   1. topic：topic名称
   2. group_id：consumerGroup，可以参考[消费详情](topic#consume)的consumer group。
   3. name_server_domain：NameServer的域名，不同的集群对应不同的id。
   5. model：消费方式
      1. CLUSTERING：所有的消费实例均分消息进行消费。
      2. BROADCASTING：每个消费实例会消费所有的消息。

3. 创建消费者实例

   ```
   try:
       consumer = PushConsumer(group_id)
       consumer.set_name_server_domain(name_server_domain)
       consumer.set_message_model(model)
   except Exception as e:
       print('start Consumer failed, error:', e)
   ```

   注意事项：

   每个group_id对应的消费者在应用整个生命周期里只应创建一次。

4. 订阅topic并启动

   ```
   # 回调函数
      def callback(msg):
          print('receive message: ', msg.body)
          # 具体的消费逻辑省略....
          # 消费成功时返回
          return ConsumeStatus.CONSUME_SUCCESS
          # 消费失败时返回
          return ConsumeStatus.RECONSUME_LATER
   # 启动消费者
   try:
          consumer.subscribe(topic, callback)
          consumer.start()
      except Exception as e:
          print('start Consumer failed, error:', e)
   ```

   注意事项

   1. 收到消息后请先打印到日志文件里，可以核对是否接到过该消息。
   2. 消费逻辑请务必捕获异常。
   3. 如果消费失败(比如数据库故障)务必返回ConsumeStatus.RECONSUME_LATER，这样此消息会发送回broker，并在此后一段时间内进行重试消费。
   4. 每个group_id对应的消费者在应用整个生命周期里只应启动一次。

5. 安全关闭

   应用退出时请调用<font color=red>consumer.shutdown()</font>进行安全关闭。

## 五、<span id="other">其余事项</span>

1. rocketmq日志默认在$HOME/logs/rocketmq-cpp下，可以参考定位问题。
2. 其余注意事项请参考[常见问题](faq)。
3. 完整用法示例可以参考[官方样例](https://github.com/apache/rocketmq-client-python/tree/master/samples)。

