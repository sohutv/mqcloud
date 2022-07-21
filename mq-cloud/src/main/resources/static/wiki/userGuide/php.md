## 一、<span id="apply">前提条件</span>

请先参考[生产和消费](produceAndConsume)完成申请。

## 二、<span id="client">客户端依赖</span>

推荐使用[rocketmq-client-php](https://github.com/lpflpf/rocketmq-client-php)。

**注意事项：**

由于rocketmq-client-php依赖rocketmq-client-cpp(rocketmq的c++客户端)，所以首先需要安装[rocketmq-client-cpp](https://github.com/apache/rocketmq-client-cpp)。

**1. 安装rocketmq-client-cpp** 请参考如下脚本：

```
yum install -y gcc-c++ cmake automake autoconf libtool bzip2-devel wget tar unzip make zlib-devel \
    && mkdir -p /root/rocketmq-cpp/ \
    && wget -O rocketmq.tar.gz 'https://github.com/apache/rocketmq-client-cpp/archive/1.2.2.tar.gz' \
    && tar xvf rocketmq.tar.gz -C /root/rocketmq-cpp/ --strip-components=1 \
    && cd /root/rocketmq-cpp/ \
    && sh build.sh \
    && yum clean all \
    && mkdir -p /usr/include/rocketmq \
    && cp /root/rocketmq-cpp/bin/librocketmq.* /usr/lib64/ -rf \
    && cp /root/rocketmq-cpp/include/* /usr/include/rocketmq/ -rf \
    && cp /root/rocketmq-cpp/bin/include/* /usr/include/ -rf \
    && cp /root/rocketmq-cpp/bin/lib/* /usr/lib64/ -rf \
    && rm -rf /root/rocketmq*
```

默认rocketmq-client-cpp下的build.sh会自动下载依赖并构建，如果服务器无法访问github或外网，可以将如下四个包下载下来：

[rocketmq-client-cpp-1.2.2.tar.gz](https://github.com/apache/rocketmq-client-cpp/archive/1.2.2.tar.gz)

[boost_1_58_0.tar.gz](https://nchc.dl.sourceforge.net/project/boost/boost/1.58.0/boost_1_58_0.tar.gz)

[jsoncpp-0.10.7.zip](https://codeload.github.com/open-source-parsers/jsoncpp/zip/0.10.7)

[libevent-release-2.0.22-stable.zip](https://codeload.github.com/libevent/libevent/zip/release-2.0.22-stable)

之后将rocketmq-client-cpp解压，将boost_1_58_0.tar.gz, jsoncpp-0.10.7.zip, libevent-release-2.0.22-stable.zip放置到rocketmq-client-cpp目录，再执行build.sh即可。

**2. 安装PHP-CPP**

php要想调用rocketmq-client-cpp，需要安装[PHP-CPP](https://github.com/CopernicaMarketingSoftware/PHP-CPP)，下载[PHP-CPP-2.1.4.tar.gz](https://github.com/CopernicaMarketingSoftware/PHP-CPP/archive/v2.1.4.tar.gz)解压后执行make && make install即可（注意，先安装好php环境）。

**3. 安装rocketmq-client-php**，参考如下步骤：

```
yum install -y libxml2-devel libcurl-devel
wget -O /root/rocketmq-client-php.tar.gz 'https://github.com/lpflpf/rocketmq-client-php/archive/v0.1-beta.tar.gz' \
    && tar xvf /root/rocketmq-client-php.tar.gz -C /root/rocketmq-client-php --strip-components=1 \
    && cd /root/rocketmq-client-php/ \
    && make && make install \
```

**4. 关联php**

```
echo "extension_dir=`/usr/local/php/bin/php-config --extension-dir`" >> /usr/local/php/lib/php.ini
echo 'extension=rocketmq.so' >> /usr/local/php/lib/php.ini
```

另外，如果需要构建rocketmq-client-cpp的docker版请参考[Dockerfile](https://github.com/lpflpf/rocketmq-client-php/blob/master/dist/Dockerfile)。

如果需要构建rocketmq-client-php的docker版请参考[Dockerfile](https://github.com/lpflpf/rocketmq-client-php/blob/master/Dockerfile)

## 三、<span id="produce">生产消息</span>

1. 创建生产者并启动

   ```
   $producer = new Producer("mqcloud-json-test-topic-producer");
   $producer->setNamesrvDomain("http://${mqcloudDomain}/rocketmq/nsaddr-集群id");
   $producer->start();
   ```

   参数释义

   1. Producer构造函数参数：producerGroup，可以点击复制[topic详情页](topic#detail)的producer group。
   2. NameServerDomain：NameServer的域名，可以点击复制[topic详情页](topic#detail)的集群路由。
   3. 每个Producer在应用整个生命周期里只应该创建和启动一次，之后可以用该producer实例多次发送消息。
   4. 在应用退出时应该调用$producer.shutdown()进行关闭。

2. 发送消息

   ```
   $message = new Message("mqcloud-json-test-topic", "*", "hello world");
   $sendResult = $producer->send($message);
   echo $sendResult->getSendStatus() . "\n";
   ```

   1. 每条消息发送完毕应该检查返回值，不可丢失的消息在异常情况应该进行重试或降级处理。

## 四、<span id="consume">消费消息</span>

1. 创建消费者，并初始化配置

   ```
   $consumer = new PushConsumer("mqcloud-json-test-consumer");
   $consumer->setNamesrvDomain("http://${mqcloudDomain}/rocketmq/nsaddr-集群id");
   $consumer->setMessageModel(MessageModel::CLUSTERING);
   $consumer->setThreadCount(1);
   ```

   参数释义

   1. PushConsumer构造函数参数：consumerGroup，可以参考[消费详情](topic#consume)的consumer group。
   2. NameServerDomain：NameServer的域名，可以点击复制[topic详情页](topic#detail)的集群路由。
   3. 集群id：topic所在集群的id，可到[topic详情页](topic#detail)查看配置。
   4. MessageModel：消费方式
      1. CLUSTERING：所有的消费实例均分消息进行消费。
      2. BROADCASTING ：每个消费实例会消费所有的消息。
   5. ThreadCount：消费线程，注意，如果设置大于1，调用echo会崩溃。

   注意事项：

   每个consumerGroup对应的消费者在应用整个生命周期里只应创建一次。

2. 订阅topic并启动

   ```
   $consumer->setCallback(function ($msg) {
       echo $msg->getMessage()->getBody() . "\n";
   });
   $consumer->subscribe("mqcloud-json-test-topic", "*");
   $consumer->start();
   ```

   注意事项

   1. 收到消息后请先打印到日志文件里，可以核对是否接到过该消息。

3. 安全关闭

   应用退出时请调用$consumer->shutdown()进行安全关闭。

## 五、<span id="other">其余事项</span>

1. rocketmq日志默认在$HOME/logs/rocketmq-cpp下，可以参考定位问题。
2. 其余注意事项请参考[常见问题](faq)。
3. 完整用法示例可以参考[官方样例](https://github.com/lpflpf/rocketmq-client-php/tree/master/example)。


