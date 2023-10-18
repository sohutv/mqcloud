## <span id="title">MQCloud-集客户端SDK，运维，监控预警等功能的[RocketMQ](https://github.com/apache/rocketmq)企业级一站式服务平台<span>
**它具备以下特性：**

* 跨集群：可以同时管理多个集群，对使用者透明。

* 预警功能：针对生产或消费堆积，失败，异常等情况预警。

* 简单明了：用户视图-拓扑、流量、消费状况等指标直接展示；管理员视图-集群运维、监控、流程审批等。

* 安全：用户隔离，操作审批，数据安全。

* 更多特性正在开发中。

* 下图简单描述了MQCloud大概的功能：

  <img src="img/mqcloud.png" class="img-wiki">

----------

## <span id="future">特性概览</span>
* 用户topic列表-不同用户看到不同的topic，管理员可以管理所有topic

  <img src="img/index.png" class="img-wiki">

* topic详情-分三块 基本信息，今日流程，拓扑

  <img src="img/topicDetail.png" class="img-wiki">

* 生产详情

  <img src="img/produceDetail2.png" class="img-wiki">

* 消费详情

  <img src="img/consumeDetail2.png" class="img-wiki">

* 某个消费者具体的消费详情-可以查询重试消息和死消息

  <img src="img/consumeRetry.png" class="img-wiki">

* 消息

  <img src="img/msgSearch.png" class="img-wiki">

* 消息消费情况

  <img src="img/msgTrack.png" class="img-wiki">

* 集群发现

  <img src="img/nameServer.png" class="img-wiki">

* 集群管理

  <img src="img/cluster.png" class="img-wiki">

* 集群流量

  <img src="img/clusterTraffic.png" class="img-wiki">

* 创建broker

  <img src="img/addBroker.png" class="img-wiki">

----------

## <span id="situation">目前运维的规模</span>
1. 服务器：40台+
2. 集群：5个+
3. topic：370个+
4. 生产消费消息量/日：10亿条+
5. 生产消费消息大小/日：1T+
----------

## <span id="contract">联系方式</span>

MQCloud QQ交流群：474960759

使用方式请参考[wiki](https://github.com/sohutv/sohu-tv-mq/wiki)。