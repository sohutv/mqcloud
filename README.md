## MQCloud - [RocketMQ](https://github.com/apache/rocketmq)的企业级运维平台
**它具备以下特性：**

* 跨集群：可以同时管理多个集群，对使用者透明。

* 预警功能：针对生产或消费堆积，失败，异常等情况预警，处理。

* 简单明了：用户视图-拓扑、流量、消费状况等指标直接展示；管理员视图-集群运维、监控、流程审批等。

* 安全：用户隔离，操作审批，数据安全。

* 更多特性正在开发中。

* 下图简单描述了MQCloud大概的功能：

  ![mqcloud](mq-cloud/src/main/resources/static/img/intro/mqcloud.png)


----------

## 特性概览
* 用户topic列表-不同用户看到不同的topic，管理员可以管理所有topic

  ![用户topic列表](mq-cloud/src/main/resources/static/img/intro/index.png)

* topic详情-分三块 基本信息，今日流程，拓扑

  ![topic详情](mq-cloud/src/main/resources/static/img/intro/topicDetail.png)

* 生产详情

  ![生产详情](mq-cloud/src/main/resources/static/img/intro/produceDetail2.png)

* 消费详情

  ![消费详情](mq-cloud/src/main/resources/static/img/intro/consumeDetail2.png)

* 某个消费者具体的消费详情-可以查询重试消息和死消息

  ![消费详情](mq-cloud/src/main/resources/static/img/intro/consumeRetry.png)

* 消息

  ![消息](mq-cloud/src/main/resources/static/img/intro/msgSearch.png)

* 消息消费情况

  ![msgconsume](mq-cloud/src/main/resources/static/img/intro/msgTrack.png)

* 集群发现

  ![admin](mq-cloud/src/main/resources/static/img/intro/nameServer.png)

* 集群管理

  ![admin](mq-cloud/src/main/resources/static/img/intro/cluster.png)

* 集群流量

  ![admin](mq-cloud/src/main/resources/static/img/intro/clusterTraffic.png)

* 创建broker

  ![addBroker](mq-cloud/src/main/resources/static/img/intro/addBroker.png)

----------

## 目前运维的规模
1. 服务器：40台+
2. 集群：5个+
3. topic：370个+
4. 生产消费消息量/日：10亿条+
5. 生产消费消息大小/日：1T+
----------

## 联系方式

MQCloud QQ交流群：474960759

使用方式请参考[wiki](https://github.com/sohutv/sohu-tv-mq/wiki)。