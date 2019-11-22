## <span id="background">一、背景</span>

当broker处于半死不活的状态时（gc，os内存不足等等原因），此时表现无法及时响应客户端的请求。

针对客户端同步发送消息，默认是带有重试策略的。可是此种情况下，客户端可能并没有重试的机会，详见下面的分析。



## <span id="analyse">二、分析</span>

参考核心消息发送代码，简化如下：

```
channel.writeAndFlush(request).addListener(new ChannelFutureListener() {//listener在数据返回后执行，不会阻塞当前线程
    public void operationComplete(ChannelFuture f) throws Exception {
       //处理返回的结果
       countDownLatch.countDown();
    }
});
countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
```

其中timeoutMillis可以近似理解为DefaultMQProducer.sendMsgTimeout，默认为3秒。

**也就是说它会一次性等到最大超时为止。**

为什么这么设计？个人理解：

作者可能认为如果netty的writeAndFlush没有抛出异常（隐含的意思是发送成功了，因为发送前和发送中如果发生网络问题，客户端立马会收到异常，就不会等着了），则客户端就等着就行了。

1 如果设置的超时时间较短，而broker比较忙，此时进行重试，这样会产生重复的消息。

2 超时后抛出异常给业务方就行了，不用rocketmq自己处理，这样消息发重了业务方是知道的。

 

但是，我们期望的是broker不响应时能够进行重试。



## <span id="resolution">三、解决方案</span>

在超时设置上做文章，超时参数优化如下：

```
// 如果还有下次重试机会，那么本次最大发送耗时修改为最大sendMsgMaxTimeoutPerRequest。
// 防止broker端长时间无响应导致无法进行下次重试。
if(defaultMQProducer.getSendMsgMaxTimeoutPerRequest() > -1 && times + 1 < timesTotal 
        && curTimeout > defaultMQProducer.getSendMsgMaxTimeoutPerRequest()) {
    curTimeout = defaultMQProducer.getSendMsgMaxTimeoutPerRequest();
}
```

即提供一个新的参数sendMsgMaxTimeoutPerRequest来标识每次真实发送请求最大的超时时间，默认为-1。

这样，客户端可以修改sendMsgMaxTimeoutPerRequest为3秒，sendMsgTimeout为4秒，这样跟rocketmq默认的行为是一致的，只有broker无响应时，再给1秒的重试机会。



## <span id="test">四、测试</span>

1. broker端

   为了模拟broker暂停的情况，broker接到客户端发送的消息后，根据消息中的属性sleep一段时间再继续执行：

   ```
   private RemotingCommand sendMessage(final ChannelHandlerContext ctx,
                                           final RemotingCommand request,
                                           final SendMessageContext sendMessageContext,
                                           final SendMessageRequestHeader requestHeader) throws RemotingCommandException {
           Map<String, String> map = MessageDecoder.string2messageProperties(requestHeader.getProperties());
           String sleepString = map.get("sleep");
           if(sleepString != null) {
               int sleep = Integer.parseInt(sleepString);
               log.info("sleep:{}ms", sleep);
               try {
                   Thread.sleep(sleep);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
   }
   ```

2. client端

   采用新的rocketmq-client进行如下测试:

   | 测试情况                             | broker sleep时间 | 客户端超时设置 | 单次最大耗时 | 期望结果 | 测试结果 |
   | -------------------------------- | -------------- | ------- | ------ | ---- | ---- |
   | broker响应正常，客户端采用默认设置             | 0              | 默认（3秒）  | 默认（-1） | 无重试  | 通过   |
   | broker响应正常，客户端配置最大超时时间           | 0              | 4秒      | 3秒     | 无重试  | 通过   |
   | broker在3秒范围内无响应，客户端采用默认设置        | 3秒             | 默认（3秒）  | 默认（-1） | 无重试  | 通过   |
   | broker在3秒内无响应，客户端加大最大超时时间        | 3秒             | 4秒      | 默认（-1） | 发生重试 | 通过   |
   | broker在客户端最大超时时间内无响应             | >4秒            | 4秒      | 默认（-1） | 无重试  | 通过   |
   | broker在客户端最大超时时间内无响应，客户端设置单次最大耗时 | >3秒            | 4秒      | 3秒     | 发生重试 | 通过   |

   由此可知，新加的参数带来的影响：**在broker无响应时，由于发生重试，可能会产生重复消息**。