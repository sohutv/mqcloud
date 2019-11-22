## <span id="background">一、背景</span>

业务方反馈，日志中发现大量如下错误：

```
[2019-09-25T18:29:19,853][WARN ][RocketmqClient           ] get Topic [search-core_model_v2-topic] RouteInfoFromNameServer is not exist value
[2019-09-25T18:29:19,853][WARN ][RocketmqClient           ] updateTopicRouteInfoFromNameServer Exception
org.apache.rocketmq.client.exception.MQClientException: CODE: 17  DESC: No topic route info in name server for the topic: search-core_model_v2-topic
See http://rocketmq.apache.org/docs/faq/ for further details.
    at org.apache.rocketmq.client.impl.MQClientAPIImpl.getTopicRouteInfoFromNameServer(MQClientAPIImpl.java:1227) ~[rocketmq-client-4.2.0.jar:4.2.0]
    at org.apache.rocketmq.client.impl.MQClientAPIImpl.getTopicRouteInfoFromNameServer(MQClientAPIImpl.java:1197) ~[rocketmq-client-4.2.0.jar:4.2.0]
    at org.apache.rocketmq.client.impl.factory.MQClientInstance.updateTopicRouteInfoFromNameServer(MQClientInstance.java:605) [rocketmq-client-4.2.0.jar:4.2.0]
    at org.apache.rocketmq.client.impl.factory.MQClientInstance.updateTopicRouteInfoFromNameServer(MQClientInstance.java:492) [rocketmq-client-4.2.0.jar:4.2.0]
    at org.apache.rocketmq.client.impl.factory.MQClientInstance.updateTopicRouteInfoFromNameServer(MQClientInstance.java:361) [rocketmq-client-4.2.0.jar:4.2.0]
    at org.apache.rocketmq.client.impl.factory.MQClientInstance$3.run(MQClientInstance.java:278) [rocketmq-client-4.2.0.jar:4.2.0]
    at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) [?:1.8.0_111]
    at java.util.concurrent.FutureTask.runAndReset(FutureTask.java:308) [?:1.8.0_111]
    at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$301(ScheduledThreadPoolExecutor.java:180) [?:1.8.0_111]
    at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:294) [?:1.8.0_111]
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [?:1.8.0_111]
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [?:1.8.0_111]
    at java.lang.Thread.run(Thread.java:745) [?:1.8.0_111]
```

而最近应用没有重启或更新过。



## <span id="investigate">二、调查</span>

1. 错误的含义很明确，NameServer上不存在这个topic，而NameServer后端的代码很简单，简化如下：

   ```
   TopicRouteData topicRouteData = map.get(requestHeader.getTopic());
   if (topicRouteData == null) {
       response.setCode(ResponseCode.TOPIC_NOT_EXIST);
       response.setRemark("No topic route info in name server for the topic: " + requestHeader.getTopic()
           + FAQUrl.suggestTodo(FAQUrl.APPLY_TOPIC_URL));
   }
   ```

   即，确实从NameServer上查不到这个topic。

2. 立马本地测试，从NameServer上却可以获取到。

   NameServer上的topic路由是broker上报上去的，检查broker发现topic的配置都正常。

3. 检查异常发生的日期，发现出问题那天进行了两项变更：

   1. 两个集群的slave为了满足跨机房高可用的需求，进行了交换。
   2. 将A集群的NameServer两个机器下线，并分给了B集群，部署了两个新的NameServer实例。

   所以，极有可能是NameServer这个变更导致了问题的发生。

4. 调查日志

   1. 跟进业务日志，发现如下端倪：

      ```
      [2019-09-17T16:27:19,850][INFO ][RocketmqRemoting         ] new name server is chosen. OLD: A:9876 , NEW: B:9876. namesrvIndex = 790
      [2019-09-17T16:27:19,851][INFO ][RocketmqRemoting         ] createChannel: begin to connect remote host[B:9876] asynchronously
      [2019-09-17T16:27:19,851][INFO ][RocketmqRemoting         ] NETTY CLIENT PIPELINE: CONNECT  UNKNOWN => B:9876
      [2019-09-17T16:27:19,852][INFO ][RocketmqRemoting         ] createChannel: connect remote host[B:9876] success, DefaultChannelPromise@291c6e50(success)
      ```

      日志显示，在16:27:19秒，客户端连接的NameServer由A切换到了B（而这两台恰好是当时切过去的两台）。

      客户端在跟NameServer远程交互时，发生如下情况会进行NameServer的切换：

      1. 网络异常
      2. 超时

   2. 紧接着，发现如下日志：

      ```
      [2019-09-17T16:27:29,846][INFO ][RocketmqClient           ] name server address changed, old=A,B,C new=C,D,E
      [2019-09-17T16:27:29,846][INFO ][RocketmqRemoting         ] name server address updated. NEW : [C,D,E] , OLD: [A,B,C]
      ```

      此日志是客户端每2分钟轮询一次，查看NameServer地址是否发生变更，变更了的话，就会更新本地缓存的列表。

5. 可能的问题：

   1. 业务端持有的NameServer列表是：A,B, ...，并且目前使用的NameServer是A。
   2. 从集群下线B，挪到其他集群，启动
   3. 从集群下线A，挪到其他集群，启动
   4. 由于下线A，业务端与NameServer交互（心跳等）发生异常，则自动选择下一个，即：B，也就是4.1中的日志（注意：此时选择的节点已经属于其他集群了）。
   5. 客户端定时轮询，发现NameServer地址发生变更，更新本地缓存的列表，也就是4.2中的日志。

   **问题来了，客户端持有了其他集群的NameServer节点，而且此节点是正常启动的节点，通信正常，但是却没有topic信息。**

   **重点是，rocketmq客户端更新本地缓存的列表时，并不检查正在使用的NameServer是否在其中。类似如下代码：**

   ```
   private Channel getAndCreateNameserverChannel() throws InterruptedException {
       String addr = this.namesrvAddrChoosed.get();
       if (addr != null) {
           ChannelWrapper cw = this.channelTables.get(addr);
           if (cw != null && cw.isOK()) {
               return cw.getChannel();
           }
       }
       final List<String> addrList = this.namesrvAddrList.get();
       ... ...
   }
   ```

6. 什么情况会发生上面的情况？

   客户端的列表顺序恰好跟更新的顺序一致，且客户端发生NameServer选择恰好在NameServer更新时间内，且NameServer更新时间小于2分钟。（默认客户端列表是随机打乱的）

7. 发生上述情况带来的影响？

   由于topic路由找不到，那么生产和消费都将不可用。

## <span id="resolution">三、解决方案</span>

这个严格来说应该是一个bug，解决方案如下：

1. 获取链接时，只需要判断是否在NameServer列表即可。
2. 或者定时更新NameServer列表，发现发生了变更时，判断一下当前使用的是否在其中即可。

**1 升级rocketmq客户端，解决这个问题。**

**2 在各个业务升级到最新版本之前，更新NameServer要保障间隔至少大于3分钟。**



## <span id="test">四、测试步骤</span>

1 首先需要先复现出此种情况：

1. 部署一个rocketmq集群，其NameServer集群具有两个节点，这里称为A，B
2. 启动客户端消费进程，并打印其NameServer缓存的顺序，假设其缓存顺序A，B，目前连接着A
3. 观察NameServer更新时机，默认每2分钟更新一次，在其间隔内执行如下步骤：
   1. 将B从NameServer域名列表中移除
   2. 关闭B，并移至其他集群并启动
4. 关闭A，触发NameServer选择机制

此时，问题重现了，而且客户端一直在抛出如下异常（B即迁移走的节点）：

````
11:12:00.999 [MQClientFactoryScheduledThread] INFO  RocketmqRemoting - new name server is chosen. OLD: A , NEW: B. namesrvIndex = 627
11:12:01.000 [MQClientFactoryScheduledThread] INFO  RocketmqRemoting - createChannel: begin to connect remote host[B] asynchronously
11:12:01.001 [NettyClientWorkerThread_4] INFO  RocketmqRemoting - NETTY CLIENT PIPELINE: CONNECT  UNKNOWN => B
11:12:01.009 [MQClientFactoryScheduledThread] INFO  RocketmqRemoting - createChannel: connect remote host[B] success, DefaultChannelPromise@5a9281bb(success)
11:12:01.022 [MQClientFactoryScheduledThread] INFO  RocketmqClient - updateTopicRouteInfoFromNameServer:basic-apitest-topic
11:12:01.041 [MQClientFactoryScheduledThread] WARN  RocketmqClient - get Topic [basic-apitest-topic] RouteInfoFromNameServer is not exist value
11:12:01.053 [MQClientFactoryScheduledThread] WARN  RocketmqClient - updateTopicRouteInfoFromNameServer Exception
org.apache.rocketmq.client.exception.MQClientException: CODE: 17  DESC: No topic route info in name server for the topic: basic-apitest-topic
See http://rocketmq.apache.org/docs/faq/ for further details.
    at org.apache.rocketmq.client.impl.MQClientAPIImpl.getTopicRouteInfoFromNameServer(MQClientAPIImpl.java:1233) ~[classes/:na]
    at org.apache.rocketmq.client.impl.MQClientAPIImpl.getTopicRouteInfoFromNameServer(MQClientAPIImpl.java:1203) ~[classes/:na]
    at org.apache.rocketmq.client.impl.factory.MQClientInstance.updateTopicRouteInfoFromNameServer(MQClientInstance.java:606) [classes/:na]
    at org.apache.rocketmq.client.impl.factory.MQClientInstance.updateTopicRouteInfoFromNameServer(MQClientInstance.java:493) [classes/:na]
    at org.apache.rocketmq.client.impl.factory.MQClientInstance.updateTopicRouteInfoFromNameServer(MQClientInstance.java:362) [classes/:na]
    at org.apache.rocketmq.client.impl.factory.MQClientInstance$3.run(MQClientInstance.java:278) [classes/:na]
    at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:471) [na:1.7.0_80]
    at java.util.concurrent.FutureTask.runAndReset(FutureTask.java:304) [na:1.7.0_80]
    at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$301(ScheduledThreadPoolExecutor.java:178) [na:1.7.0_80]
    at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:293) [na:1.7.0_80]
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145) [na:1.7.0_80]
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615) [na:1.7.0_80]
    at java.lang.Thread.run(Thread.java:745) [na:1.7.0_80]
````

2 使用修复后的客户端，重复执行上述步骤进行测试：

```
11:31:13.676 [MQClientFactoryScheduledThread] INFO  RocketmqClient - fetchNameServerAddr:A
11:31:13.676 [MQClientFactoryScheduledThread] INFO  RocketmqClient - name server address changed, old=B;A, new=A
11:31:13.676 [MQClientFactoryScheduledThread] INFO  RocketmqRemoting - name server address updated. NEW : [A] , OLD: [A, B]
11:31:33.668 [MQClientFactoryScheduledThread] INFO  RocketmqClient - updateTopicRouteInfoFromNameServer:TBW102
11:31:33.668 [MQClientFactoryScheduledThread] INFO  RocketmqRemoting - name server address is invalid: B
11:31:33.668 [MQClientFactoryScheduledThread] INFO  RocketmqRemoting - new name server is chosen. OLD: null , NEW: A. namesrvIndex = 614
11:31:33.669 [MQClientFactoryScheduledThread] INFO  RocketmqRemoting - createChannel: begin to connect remote host[A] asynchronously
```

日志：name server address is invalid: B 表示，能够及时剔除还在连着的，但是不在NameServer列表中的地址。

