## <span id="background">一、背景</span>

目前rocketmq官方只针对集群消费模式实现了offset重置功能。为满足MQCloud使用方的个性化需求，为此开发实现广播消费模式下offset重置功能。

## <span id="analyse">二、rocketmq实现offset重置原理分析</span>

offset重置分为三个步骤：

1. 调用方发送offset重置指令给broker
2. broker处理offset重置指令并发送给consumer
3. consumer执行offset重置操作

#### 2.1 调用方发送offset重置指令给broker
目前rocketmq支持根据时间戳进行offset重置功能，调用方法为：`DefaultMQAdminExt#resetOffsetByTimestamp`

流程如下：

1. 根据topic获取路由信息，进而得到broker信息
2. 遍历每个broker，优先获取master地址，不存在则随机取一个slave地址，然后发送`RequestCode.INVOKE_BROKER_TO_RESET_OFFSET = 222`的指令

#### 2.2 broker处理offset重置指令并发送给consumer
broker收到`RequestCode.INVOKE_BROKER_TO_RESET_OFFSET=222`指令后，对应处理逻辑方法为：`AdminBrokerProcessor#resetOffset`，实际调用`Broker2Client#resetOffset`，处理流程如下：

1. 遍历broker的写队列:
   1. 获取消费者在当前队列的消费偏移量，记为`consumerOffset`
   2. 根据重置时间戳timeStamp，获取broker上该队列对应的offset值，记为`timeStampOffset`
   3. 比较consumerOffset和timeStampOffset，结合`isForce`(是否强制以timeStampOffset为准进行重置)，得到当前队列实际应该重置到的offset后存入到offsetTable中
2. 得到offsetTable后，遍历consumer列表，发送`RequestCode.RESET_CONSUMER_CLIENT_OFFSET = 220`的指令

总结：broker端收到重置offset命令后，根据timeStamp获取每个队列应该重置到的offset值后，然后构造`RequestCode.RESET_CONSUMER_CLIENT_OFFSET = 220`指令，依次发送给消费者client，实际执行offset操作的还是在消费者端。


#### 2.3 consumer端执行offset重置操作
消费者端收到`RequestCode.RESET_CONSUMER_CLIENT_OFFSET = 220`指令后，对应处理方法为：ClientRemotingProcessor#resetOffset，实际调用`MQClientInstance#resetOffset`，实际重置功能跟`OffsetStore`的部分方法关联很大。

1. 消费暂停
2. 设置消费快照为丢弃状态
3. 遍历messageQueue，根据重置offset值：
    1. 更新内存中offset值，调用`offsetStore#updateOffset`
    2. 持久化offset到文件，调用`offsetStore#persist`
    3. 移除内存中offset值，调用`offsetStore#removeOffset`
4. 恢复消费：执行`doRebalance()`

总结：consumer端实际执行offset重置工作，根据broker传来的需要重置到的offset，更新内存及文件中的offset值，进而达到重置目的。


## <span id="realize">三、广播消费模式实现offset重置功能</span>
上文所述offset重置流程对于集群消费模式和广播消费模式都是通用的，但是经实际测试，只有集群消费模式下能重置成功，广播消费模式下报错，信息如下：

```
2020-12-03 17:16:33.024 {ThreadPoolTaskScheduler-3} ERROR com.sohu.tv.mq.cloud.service.ConsumerService-527 - resetOffset topic=basic-apitest-topic,group=basic-apitest-broadcast-consumer err:CODE: 1  DESC: THe consumer group <basic-apitest-broadcast-consumer> not exist
For more information, please visit the url, http://rocketmq.apache.org/docs/faq/
```

根据报错信息，定位到`Broker2Client#resetOffset`，部分代码如下：

```
// 获取消费者在当前队列的消费偏移量offset
long consumerOffset = this.brokerController.getConsumerOffsetManager().queryOffset(group, topic, i);
// 获取不到就直接返回报错信息
 if (-1 == consumerOffset) {
    response.setCode(ResponseCode.SYSTEM_ERROR);
    response.setRemark(String.format("THe consumer group <%s> not exist", group));
    return response;
  }
```

这里consumerOffset取值来自ConsumerOffsetManager.offsetTable，但是ConsumerOffsetManager管理的是集群消费模式下consumer的消费offset信息，无法获取广播消费模式下consumer的offset信息。所以广播消费模式下重置offset指令到这里就直接return了，这里需要针对广播消费模式修改限制条件，使得广播消费模式在获取不到consumerOffset时也能正常往下执行，具体修改如下：

1. **修改1：当查询不到consumerOffset但是是广播消费模式时，程序继续执行。**
2. **修改2：针对广播消费模式，设置单独的offset重置逻辑，避免影响集群消费模式。**

修改如下：

```
long consumerOffset = this.brokerController.getConsumerOffsetManager().queryOffset(group, topic, i);
// 广播消费模式下不return
if (-1 == consumerOffset && !messageModel.equals(MessageModel.BROADCASTING)) {
    response.setCode(ResponseCode.SYSTEM_ERROR);
    response.setRemark(String.format("THe consumer group <%s> not exist", group));
    return response;
}
// 广播消费模式单独处理，不改动集群消费模式逻辑 
if (messageModel.equals(MessageModel.BROADCASTING)) {
    offsetTable.put(mq, timeStampOffset);
} else {
    if (isForce || timeStampOffset < consumerOffset) {
        offsetTable.put(mq, timeStampOffset);
    } else {
        offsetTable.put(mq, consumerOffset);
    }
}
```

经过上述修改后，进行测试，没有报错，但是offset并没有实现重置，打开consumer端offset持久化文件，发现offset值没有改动。在上文原理篇中说道，offset重置实际执行都是在consumer端，并且与OffsetStore关系很大。这里先介绍下OffsetStore以便于后续理解。

OffsetStore是读取、更新、持久化offset的接口类，有两个实现类，分别对应着集群消费模式和广播消费模式：

1. RemoteBrokerOffsetStore：对应集群消费模式，该模式下消费者的offset存储在broker上
2.  LocalFileOffsetStore：对应广播消费模式，该模式下消费者的offset存储在消费者端机器上

在上文 **2.3 consumer端执行offset重置操作** 流程中，**第3步(遍历messageQueue，根据重置offset值)** 用到了OffsetStore的三个方法`updateOffset`、`persist`、`removeOffset`。集群消费模式下，这三个方法在RemoteBrokerOffsetStore类中均有实现，但是广播消费模式下，`LocalFileOffsetStore`只实现了方法`updateOffset`，其它两个方法并未实现。

但是，广播消费模式是以内存中的offset为准的，持久化并不是实时更新的，默认5s全量持久化一次，所以，持久化文件中的值与内存值不是实时相等的。这里没有实现持久化功能也可以认为是正常的。

继续分析上文 **2.3 consumer端执行offset重置操作** 流程中，**第4步(恢复消费：执行`doRebalance()`)**：

consumer经过一系列更新offset操作后，会恢复消费，恢复消费逻辑为执行`doRebalance()`，在这个步骤中，**获取每个队列消费offset时，是从持久化文件中获取的，而不是从内存中获取**。但是广播消费模式下没有实现offset持久化方法，导致rebalance阶段获取队列消费offset值不是期望的数据，从而offset重置失败。所以是否可以考虑实现LocalFileOffsetStore的persist方法，将内存中的值持久化到文件中?

实现LocalFileOffsetStore的persist方法，涉及到将内存值持久化到文件，该过程是线程不安全的，当多个队列同时调用persist时，会导致文件丢失等一系列并发带来的问题，当然可以考虑加锁，但这会导致性能下降，也可能会带来不可预知的风险。故而**该实现方式需慎重**。

可以从另外一个角度来分析，既然广播消费模式下offset以内存值为准，文件值为辅且不保证实时同步，那么doRebalance阶段何不修改为从内存中获取offset值？

1. **修改3：doRebalance阶段，广播消费模式下offset获取逻辑修改为：优先从内存获取，获取不到再从文件中查找**

修改如下：

```
long lastOffset = -1;
// 广播消费模式获取offset方式改为：MEMORY_FIRST_THEN_STORE
if (MessageModel.BROADCASTING == (this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getMessageModel())) {
   lastOffset = offsetStore.readOffset(mq, ReadOffsetType.MEMORY_FIRST_THEN_STORE);
} else {
   lastOffset = offsetStore.readOffset(mq, ReadOffsetType.READ_FROM_STORE);
}
```

总结：经过上述三处修改，经测试可实现广播消费模式下offset重置功能。


## <span id="test">四、测试</span>

#### 4.1 测试广播消费模式下重置offset功能

在MQCloud控制台，申请跳过堆积，审核通过后，broker端日志：

```
2020-12-03 18:30:18 INFO AdminBrokerThread_4 - [reset-offset] reset offset started by x.x.x.x:57522. topic=broadcast-test-topic, group=broadcast-test-consumer, timestamp=1606991358536, isForce=true
2020-12-03 18:30:18 INFO AdminBrokerThread_4 - [reset-offset] reset offset success. topic=broadcast-test-topic, group=broadcast-test-consumer, clientId=x.x.x.x@DEFAULT@9
```

从日志中得知：broker收到指令，并且向consumer发送offset重置指令成功。

consumer端日志：

```
// 消费
2020-12-03 18:26:26.248 {ConsumeMessageThread_broadcast-test-consumer_1} INFO  c.e.consumer.test.consumer.TestConsumerCallback-23 - msg:772, msgExt:MessageExt [queueId=1, storeSize=197, queueOffset=48676, sysFlag=0, bornTimestamp=1605522449261，.........]
 
// 收到broker指令
2020-12-03 18:30:17.042 {NettyClientPublicExecutor_1} INFO  RocketmqClient-100 - invoke reset offset operation from broker. brokerAddr=x.x.x.x:10911, topic=broadcast-test-topic, group=broadcast-test-consumer, timestamp=1606991358536
// 暂停消费
2020-12-03 18:30:17.046 {NettyClientPublicExecutor_1} INFO  RocketmqClient-90 - suspend this consumer, broadcast-test-consumer
// 设置消费快照为dropped状态
2020-12-03 18:30:17.091 {PullMessageService} INFO  RocketmqClient-90 - the pull request[PullRequest [consumerGroup=broadcast-test-consumer, messageQueue=MessageQueue [topic=broadcast-test-topic, brokerName=broker-a, queueId=3], nextOffset=46589]] is dropped.
// dropped状态队列无法消费
2020-12-03 18:30:26.284 {ConsumeMessageThread_broadcast-test-consumer_5} INFO  RocketmqClient-95 - the message queue not be able to consume, because it's dropped. group=broadcast-test-consumer MessageQueue [topic=broadcast-test-topic, brokerName=broker-a, queueId=0]
.....
// doRebalance
2020-12-03 18:30:27.169 {NettyClientPublicExecutor_1} INFO  RocketmqClient-95 - doRebalance, broadcast-test-consumer, add a new mq, MessageQueue [topic=broadcast-test-topic, brokerName=broker-a, queueId=2]
2020-12-03 18:30:27.174 {NettyClientPublicExecutor_1} INFO  RocketmqClient-95 - doRebalance, broadcast-test-consumer, add a new pull request PullRequest [consumerGroup=broadcast-test-consumer, messageQueue=MessageQueue [topic=broadcast-test-topic, brokerName=broker-a, queueId=2], nextOffset=47042]
// 恢复消费
2020-12-03 18:30:27.189 {NettyClientPublicExecutor_1} INFO  RocketmqClient-90 - resume this consumer, broadcast-test-consumer
// 消费消息
2020-12-03 18:30:47.185 {ConsumeMessageThread_broadcast-test-consumer_8} INFO  c.e.consumer.test.consumer.TestConsumerCallback-23 - msg:2110, msgExt:MessageExt [queueId=2, storeSize=199, queueOffset=47044,...........}]
```

从日志中得知：consumer端重置offset后，确实是从较新的offset开始消费。


#### 4.2 广播消费模式下offset过大或过小问题

广播消费模式下，由于消费offset存在消费者本地，所以当用户在本地进行测试时，有可能过了很久才启动进行一次测试，此时消费者启动，会从本地加载offsets.json文件，但这时若broker端存储的消息已经过期被删除了，就会出现本地的offsets.json文件中offset值小于broker端存储的最小偏移量，那么从broker中就查不到对应的消息，加载到内存中的offset就是错的。
 
此类场景跟[广播消费模式的消费者OFFSET_MOVED_EVENT预警问题调查](broadcastingOffsetMovedEvent)很相似，可以理解为offset过大或过小。rocketmq有自动纠正错误offset的机制，但是有bug。


##### 4.2.1 rocketmq自动纠正错误offset机制流程

1. 消费者启动，从offset持久化文件中加载offset

2. 根据加载到的offset从broker端获取消息，源码入口：`PullMessageProcessor#processRequest`。
在这一步中，会对offset进行容错处理，大致容错机制为：当请求的offset比最小的minOffset还要小时，纠正offset为最小minOffset；比最大的maxOffset还要大时，纠正offset为最大maxOffset。并且返回错误码：OFFSET_TOO_SMALL或OFFSET_OVERFLOW_BADLY，broker端会将这些错误码统一转成：`PULL_OFFSET_MOVED`错误码，然后发送给消费者端。

3. 消费者端收到PULL_OFFSET_MOVED后，转成`OFFSET_ILLEGAL`错误码接收，进行offset纠正：
    1. 更新容错后的offset内存值，`offsetStore#updateOffset`
    2. 持久化offset到文件，`offsetStore.persist`
    3. 设置消费队列为dropped=true，并移除当前消费快照

容错机制执行后，会随着5s一次的全量offset持久化定时任务，将内存中的offset值持久化到文件，从而达到既更新内存值又更新文件值的目的。

这里的`offsetStore#updateOffset`和`offsetStore.persist`看着很眼熟，跟上文中consumer端重置offset的处理逻辑很相似。

经过测试，发现确实可以自动纠正错误的offset，但是有一种场景是无法纠正的：当本地offsets.json文件中**所有队列的offset全部是错误**的时候(offset过大/过小)，此时消费者端会卡住不停地重复offset纠正这个过程。例如下面文件中，队列6和队列7的offset全是偏大，此时是无法纠正的。

```
{
    "offsetTable":{{
            "brokerName":"broker-a",
            "queueId":7,
            "topic":"broadcast-test-topic"
        }:999999999,{
            "brokerName":"broker-a",
            "queueId":6,
            "topic":"broadcast-test-topic"
        }:999999999
    }
}
```

##### 4.2.2 原因分析及解决方案

假设持久化文件中offset全部是错误的(偏大/偏小)。

先引入两个定时任务便于后续理解：

1. 每隔5s全量持久化消费者offset到文件中。即：`MQClientInstance.persistAllConsumerOffset()`
2. 每隔20s一次的`doRebalance()`

在**4.2.1**中，consumer端执行offset容错处理共有三步，其中：

1. 持久化offset到文件，`offsetStore.persist`。广播消费模式没有实现该方法，即：`LocalFileOffsetStore.persist`
2. 设置消费队列为dropped=true，并移除当前消费快照。**此时若所有的offset都是错误的，将移除所有的消费快照，导致现有的消费快照列表(Set&lt;MessageQueue&gt;)是空的。**

当容错机制执行完毕后，随着5s一次的全量持久化offset操作，将内存中的offset持久化到文件，部分代码如下：

```
public void persistAll(Set<MessageQueue> mqs) {
    if (null == mqs || mqs.isEmpty())
        return;
    // 以下略去
}
```

**这里判断当前消费队列是否为空，若为空则不进行下面的持久化操作；但是若offset全是错误的情况下，此时消费快照列表(Set&lt;MessageQueue&gt;)是空的，即不会更新offset文件，此时本地offsets.json文件中的offset并没有被纠正，依然是错误的。**

随后，在20s一次的doRebalance()任务中，获取offset时，**是从持久化文件中获取的**，这时文件中的offset全部是错误的，从而得到错误的offset，触发了offset容错机制，然而容错机制并没有将更新后的offset持久化到文件，导致文件中的offset
一直都是错误的并且得不到纠正，从而**循环出现`the pull request offset illegal`错误，消费端卡住无法消费**。

解决方案：

在第三章中实现了广播消费模式下offset重置功能，其中一项修改：**修改3：doRebalance阶段，广播模式下offset获取逻辑修改为：优先从内存获取，获取不到再从文件中查找。**

这样修改后，在doRebalance()任务中，能够从内存中获取到正确的offset值，从而不会触发offset容错处理机制，consumer端能够正常消费不会卡住。至于持久化文件中的错误offset，会随着5s一次的持久化操作而得到更正。

总结：实现广播消费模式下offset重置功能后，顺带解决了offset全部错误导致卡住不能消费的问题。