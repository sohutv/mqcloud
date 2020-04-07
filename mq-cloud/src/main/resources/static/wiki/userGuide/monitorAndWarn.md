## 一、<span id="consume">消费堆积预警</span>

![](img/5.0.png)

*只针对集群消费方式进行预警，默认每5分钟消息堆积量达到10000条，预警一次，一小时最多预警一次。*

## 二、<span id="clientBlock">客户端阻塞</span>

![](img/5.1.png)

*只针对push方式局部有序的消费者，默认每5分钟阻塞达到10秒，预警一次，一小时最多预警一次。*

## 三、<span id="clientException">客户端异常</span>

![](img/5.2.png)

*使用MQCloud提供的客户端，生产失败的消息会每5分钟预警一次。*

## 四、<span id="consumeError">消费失败</span>

![](img/5.3.png)

*针对集群消费方式的消费者，每小时消费失败量达到10次，预警一次。*

## 五、<span id="offset">偏移量错误</span>

![](img/5.4.png)

*消费者消费的消息在broker上不存在时，一般是偏移量错误，此时会进行预警，预警频率：实时。*

## 六、<span id="subError">订阅错误</span>

![](img/5.5.png)

*一个消费者订阅了多个topic时，进行预警。*

## 七、<span id="statMonitorWarning">统计，监控，预警</span>

关于这块的内容，感兴趣的可以参考开发指南的[统计监控预警](../developerGuide/statMonitorWarning)部分。