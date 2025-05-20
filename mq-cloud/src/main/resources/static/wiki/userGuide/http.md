## 一、<span id="apply">前提条件</span>

请先参考[生产和消费](produceAndConsume)完成申请。

由于RocketMQ为java语言实现的，其他语言的客户端实现并不完备，往往出现某些问题很难定位，故建议非java语言使用http协议进行消息的生产和消费。

## 二、<span id="producer">生产接入</span>

1. 接口：

   ```
   POST http://${httpProducerUriPrefix}/mq/produce
   ```

2. 参数：

   * 【必选】producer: 在MQCloud申请的生产者名称
   * 【必选】message: 消息
   * 【可选】keys:  key
   * 【可选】orderId：发送顺序消息id，将用该参数定位消息发往的队列，定位方法采用orderId.hash%队列数
     例如，同一个订单的消息如果想保障顺序，orderId可以传订单id。
   * 【可选】delayLevel：延时消息的延迟级别，取值为1~18，对应的含义请参考[延迟消息](clientProducer#delay)
   * 【可选】deliveryTimestamp：定时消息的递交时间戳（需要保留到毫秒），对应的含义请参考[定时消息](clientProducer#timerWheel)

3. 响应说明：

   ```
   {
       "status": 200,
       "result": {
           "sendStatus": "SEND_OK",
           "msgId": "7F0000010008323659F89380B26C3B73",
           "messageQueue": {
               "topic": "mqcloud-http-test-topic",
               "brokerName": "broker-c",
               "queueId": 3
           },
           "queueOffset": 3153,
           "offsetMsgId": "0A121DC100002A9F0000000020EBAC2D",
           "regionId": "DefaultRegion",
           "traceOn": true
       }
   }
   ```

   1. status：标识本次响应的状态码，包括但不限于如下值（如需100%发送成功，非200时要重试发送）：
      - 200：生产成功
      - 300：生产未知，如需保障100%成功需要重试
      - 400：参数错误
      - 500：服务内部异常
   2. message：当响应状态码非200时的提示信息。
   3. result：响应结果
      1. sendStatus：本次发送消息的状态，包括但不限于如下值：
         - SEND_OK：发送成功
         - FLUSH_DISK_TIMEOUT：消息接收成功，但是服务器刷盘超时，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失
         - FLUSH_SLAVE_TIMEOUT：消息接收成功，但是服务器同步到 Slave 时超时，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失
         - SLAVE_NOT_AVAILABLE：消息接送成功，但是此时 slave 不可用，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失
      2. msgId：消息id，客户端生成的
      3. offsetMsgId：消息id，broker生成的
      4. messageQueue：消息发送到的消息队列对象
      5. queueOffset：消息发送到队列的偏移量

4. 生产举例：

   ```
   <script>
       var param = {
           producer: "mqcloud-http-test-topic-producer"
       };

       function httpProduce() {
           param.message = "http生产测试";
           $.ajax({
               type: "POST",
               url: "http://${httpProducerUriPrefix}/mq/produce",
               data: param,
               success: function (data) {
                   console.log(data)
                   if (data.status == 200) {
                       console.log("send ok")
                   } else {
                       // 重试发送
                       httpProduce()
                   }
               }, error: function (XmlHttpRequest, textStatus, errorThrown) {
                   console.error(textStatus)
                   // 重试发送
                   setTimeout("httpProduce()", 1000);
               }
           });
       }

       httpProduce()
   </script>
   ```

## 三、<span id="batchProduce">批量生产接入</span>
1. 接口：
   ```
   POST http://${httpProducerUriPrefix}/mq/produce/batch
   ```
2. 参数：
   * 【必选】producer: 在MQCloud申请的生产者名称
   * 【必选】messages: json格式的数组消息，例如：
     ```
     [{"id": "123","title": "video"},"only text"]
     ```
     如上所示，json数组内为一条一条的消息，消息可以为json或字符串等格式。
3. 响应说明：
   当全部发送成功时，返回如下响应：
   ```
   {
       "status": 200,
   }
   ```
   当发送出现失败时，返回类似如下响应：
   ```
   {
       "status": 500,
       "message": "some messages failed to send",
       "result": [
           {
               "status": 400,
               "message": "message body too long:4194304"
           },
           {
               "status": 200
           }
       ]
   }
   ```
   只需要检测`status`字段的值，如果不是200，就表明发送失败了。

   另外，响应中如果没有`result`字段，表示消息全部发送失败了。

   如果存在`result`字段，可以解析该数组，其中每个结果的下标与发送的消息下标相对应，根据每个结果的`status`字段，可以知道哪条消息发送失败了，以便进行重发，例如发送的消息和响应如下：

   ```
   假如messages参数如下：
   [
     {
       "id": "123",
       "title": "video"
     },
     "only text"
   ]
   响应如下：
   {
       "status": 500,
       "message": "some messages failed to send",
       "result": [
           {
               "status": 400,
               "message": "java.util.concurrent.TimeoutException"
           },
           {
               "status": 200
           }
       ]
   }
   总体的status为非200，表示整体发送失败，但是可能部分发送成功，需要解析result数组。
   result数组第一个结果status为400，代表第一条消息:{"id": "123","title": "video"}发送失败了。
   第二个结果的status为200，代表第二条消息:"only text"发送成功了。
   ```
   另外，单条消息最大为4M，超过后将发送失败。

## 四、<span id="consumer">消费接入</span>

1. 消息消费接口：

   ```
   GET http://${httpConsumerUriPrefix}/mq/message
   ```

2. 参数：

   * 【必选】topic：打算消费的主题
   * 【必选】consumer: 在MQCloud申请的消费者名称
   * 【必选】requestId：请求id，用于确认上次消费完成
   * 【广播消费必选】clientId：广播模式消费时须携带，不同的clientId消费全量消息，可以设置为ip
   * 【可选】clientIp:  用于记录客户端与消费队列的关系，默认会自动解析clientIp，只有自动解析不正确时才需要传递该参数

3. 响应说明：

   ```
   {
   	"status": 200,
   	"result": {
   		"status": "FOUND",
   		"requestId": "YnJva2VyLWMsNCwyMTU2OSwyMTYwMSwxNjU1ODgyNjM0MDU0",
   		"msgList": [{
   			"messageId": "7F00000122AC18B4AAC26F3E531D000B",
   			"message": "{\"id\":11,\"name\":\"搜狐tv11\"}",
   			"timestamp": 1655879164144
   		}, {
   			"messageId": "7F00000122AC18B4AAC26F3E550A001B",
   			"message": "{\"id\":27,\"name\":\"搜狐tv27\"}",
   			"timestamp": 1655879164640
   		}],
   		"msgListSize": 2,
   		"retryMsgList": [{
   			"messageId": "7F0000010008323659F82698564A13A3",
   			"message": "{\"id\":13,\"name\":\"搜狐tv13\"}",
   			"timestamp": 1657252317780
   		}, {
   			"messageId": "7F0000010008323659F8269857B513A9",
   			"message": "{\"id\":14,\"name\":\"搜狐tv14\"}",
   			"timestamp": 1657252318144
   		}],
   		"retryMsgListSize": 2
   	}
   }
   ```

   1. status：标识本次响应的状态码，包括但不限于如下值：
      * 200：响应正常
      * 400：参数错误
      * 500：服务内部异常
   2. message：当响应状态码非200时的提示信息
   3. result：响应结果
      1. status：本次消息拉取状态，包括但不限于如下值：
         * FOUND：正常拉取到消息
         * NO_NEW_MSG：没有新消息
         * NO_MATCHED_MSG：没有匹配的消息
         * OFFSET_ILLEGAL：偏移量非法
         * PAUSED：消费暂停
         * RATE_LIMITED：消费被限速
      2. requestId：请求id，**下次请求时需要携带此参数** 
      3. msgList：消息列表
         * messageId：消息id
         * message：消息体
         * timestamp：消息时间戳，可以理解为消息发送时间戳
      4. msgListSize：消息量
      5. retryMsgList：重试消息列表，可以在消息搜索模块发送重试消息。
      6. retryMsgListSize：重试消息量

   **注意：如果消费者从来没有消费过某个队列，第一次消费会从队列最小偏移量开始消费，如果想从最大偏移量开始消费，请手动跳过堆积。**

4. 消息消费成功确认接口：

   ```
   GET http://${httpConsumerUriPrefix}/mq/ack
   ```

   参数与`消息消费接口`相同，用于当消费业务下线时ACK使用。

   *默认情况下，消费消息时，传递的requestId参数用于确认上一批消息消费成功。但是当服务下线时，不再拉取消息，那么最后一次消费的消息就无法确认，所以需要在停止消息拉取后再单独调用一次ack接口。*

5. ACK机制说明

   为了确保消息消费成功，需要消息确认机制，即ACK机制，具体如下：

   * 当调用`http://${httpConsumerUriPrefix}/mq/message`接口时，会返回字段`requestId`， 它用于标识本次请求返回的消息。
   * 当下次调用`http://${httpConsumerUriPrefix}/mq/message`，需要把上次返回的`requestId`当做请求参数，这样后台会确认上次的消息消费成功了。

   如果某次拉取的消息未进行ACK，那么会认为这批消息没有消费成功，在锁定一段时间后（默认为5分钟），会被重新拉取到。

   另外，在拉取消息时传递`requestId`势必对编程或用浏览器测试造成些困难，这里提供了自动化ACK功能，即在拉取消息时增加一个参数即可：`useCookie=true`。

   这样，requestId会自动存到cookie中，当发送请求时，自动携带cookie，不用在单独传递`requestId`了。

   **注意：** 浏览器或客户端需要支持cookie功能，否则加上参数`useCookie=true`，也无法自动ACK。

6. 主动解锁队列接口：

   ```
   GET http://${httpConsumerUriPrefix}/mq/unlock
   ```

   参数与`消息消费接口`相同，用于当消费业务下线时，解锁最后一次消费的队列。

   *主要用于服务下线时，此时消费任务还没有完成，但是想直接重启程序后再消费的情况。如果直接退出的话，由于最后一次消费的队列没有ACK，队列会被锁定，至到锁定超时后才会解锁。如果程序重启后想立马消费该队列，就需要主动调用/mq/unlock接口，解锁该队列。注意：解锁后，最后一次消费的消息会重新被拉取到。*

7. 集群消费举例：

   下面是jquery的一个demo，便于理解如何使用：

   ```
   <script>
       var param = {
           topic: "mqcloud-json-test-topic",
           consumer: "http-clustering-consumer"
       };

       function httpConsume() {
           $.ajax({
               type: "GET",
               url: "http://${httpConsumerUriPrefix}/mq/message",
               data: param,
               success: function (data) {
                   if (data.status == 200) {
                       param.requestId = data.result.requestId;
                       if (data.result.msgListSize > 0) {
                           console.log(data.result.status + "," + data.result.msgListSize);
                       }
                       if (data.result.retryMsgListSize > 0) {
                       	console.log(data.result.retryMsgListSize);
                       }
                   } else {
                       console.log(data.message);
                   }
                   interval = setTimeout("httpConsume()", 1000);
               }, error: function (XmlHttpRequest, textStatus, errorThrown) {
                   console.error(textStatus)
                   interval = setTimeout("httpConsume()", 1000);
               }
           });
       }
       httpConsume();
   </script>
   ```

   **当返回结果的status字段为200时，务必把result.requestId当做参数，传递给下一次请求，否则，本次消息会认为没有消费成功。**

8. 广播消费举例：

   ```
   <script>
       var param = {
           topic: "mqcloud-json-test-topic",
           consumer: "http-clustering-consumer",
           clientId: "127.0.0.1"
       };

       function httpConsume() {
           $.ajax({
               type: "GET",
               url: "http://${httpConsumerUriPrefix}/mq/message",
               data: param,
               success: function (data) {
                   if (data.status == 200) {
                       param.requestId = data.result.requestId;
                       if (data.result.msgListSize > 0) {
                           console.log(data.result.status + "," + data.result.msgListSize);
                       }
                       if (data.result.retryMsgListSize > 0) {
                       	console.log(data.result.retryMsgListSize);
                       }
                   } else {
                       console.log(data.message);
                   }
                   interval = setTimeout("httpConsume()", 1000);
               }, error: function (XmlHttpRequest, textStatus, errorThrown) {
                   console.error(textStatus)
                   interval = setTimeout("httpConsume()", 1000);
               }
           });
       }
       httpConsume();
   </script>
   ```

   广播模式消费与集群模式唯一的区别是需要额外传递一个参数`clientId`，不同的clientId将消费全量消息。