## 一、<span id="queryWay">查询方式</span>

目前支持以下查询方式：

1. 按照偏移量查询
2. 按照key查询
3. 按照时间段查询
4. 按照消息id查询
5. 按照trace查询

下面进行一一介绍。

## 二、<span id="offset">按照偏移量查询</span>

*由于必须输入broker偏移量，适用于精确查询。*

![](img/4.0.png)

**起始偏移量**：参考生产详情里<a href="topic#produce">broker的最小偏移量</a>。

**结束偏移量**：参考生产详情里<a href="topic#produce">broker的最大偏移量</a>。

**关键字**：如果期望查到的消息包含某些字符，可以在此填入。

**筛选条件**：可以选择在某个broker查询，或者broker的某个队列。

**消息重发**：针对查出来的消息，可以单击选中（支持shfit或ctrl多选），点击重发按钮，即会把选中消息的重发请求发送到管理后台，管理员审核通过后进行重发。

**详情**：点击详情的眼睛图标，可以看到消息的生产消费轨迹（非trace）：

![](img/4.7.png)

## 三、<span id="key">按照key查询</span>

*适用于生产消息时发送了<a href="clientProducer#produceMessage">keys</a>字段的topic*

![](img/4.1.png)

**开始时间**：消息的最小时间。

**结束时间**：消息的最大时间。

**消息key**：发送时传的keys参数。

**建议消费发送方传递参数keys，因为RocketMQ会根据keys建立索引，这样根据keys查询时会很快。**

**例如，keys可以指定为订单号，那么在这里查询时，输入一个订单号，所有此订单相关的消息均会查到。**

## 四、<span id="time">按照时间段查询</span>

*适用于没有传递keys，但是想根据时间来检索消息*

![](img/4.2.png)

**开始时间**：消息的最小时间。

**结束时间**：消息的最大时间。

**关键字**：如果期望查到的消息包含某些字符，可以在此填入。

*与按照key查询方式不同，这样查询消息没有索引，耗时会比较长。*

## 五、<span id="msgId">按照消息id查询</span>

*适用于用户知道消息id的情况*

![](img/4.3.png)

**消息id**：此处的消息id是[SendResult](https://github.com/apache/rocketmq/blob/master/client/src/main/java/org/apache/rocketmq/client/producer/SendResult.java)里的offsetMsgId。

## 六、<span id="trace">按照trace查询</span>

*适用于申请新建topic时，开启了trace功能。*

![](img/4.4.png)

如果该topic开启了trace功能，可以从之前4种查询方式的消息结果页，通过点击消息前的序号跳到此trace页面。

点击详情的眼睛图标，可以看到trace的详细数据：

**生产者trace详情**：

![](img/4.5.png)



**消费者trace详情**：

![](img/4.6.png)