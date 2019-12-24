## <span id="background">一、背景</span>

master broker.log中出现如下日志：

```
2019-12-03 17:42:06 INFO BrokerControllerScheduledThread1 - Slave fall behind master: -6868909625984868736 bytes
```

slave同步master的数据落后的是负数，难以理解！

## <span id="analyse">二、分析</span>

1 上面的日志对应[DefaultMessageStore](https://github.com/apache/rocketmq/blob/master/store/src/main/java/org/apache/rocketmq/store/DefaultMessageStore.java#L941)如下代码：

```
public long slaveFallBehindMuch() {
    return this.commitLog.getMaxOffset() - this.haService.getPush2SlaveMaxOffset().get();
}
```

其中`this.commitLog.getMaxOffset()`这个是master的commit log的最大offset，这个不太可能出错，如果出错，那消息存储麻烦就大了，commit log设计可以[参考](https://blog.csdn.net/a417930422/article/details/52585180)。

那么就是`this.haService.getPush2SlaveMaxOffset().get()`这个有问题了。

2 push2SlaveMaxOffset来自于[HAService](https://github.com/apache/rocketmq/blob/master/store/src/main/java/org/apache/rocketmq/store/ha/HAService.java#L55)中的属性，顾名思义：推送给slave的最大偏移量，初始值为0。

那么它是什么时候更新的，参考如下代码：

```
public void notifyTransferSome(final long offset) {
    for (long value = this.push2SlaveMaxOffset.get(); offset > value; ) {
        boolean ok = this.push2SlaveMaxOffset.compareAndSet(value, offset);
        if (ok) {
            this.groupTransferService.notifyTransferSome();
            break;
        } else {
            value = this.push2SlaveMaxOffset.get();
        }
    }
}
```

push2SlaveMaxOffset来自于offset的值，offset是什么？

1. 如果slave未同步过master的数据，offset将使用master的commit文件组中最新的一个文件的最小offset，即：同步最新的一个commit文件给slave。
2. 如果slave已经同步过master数据，那么offset将使用slave commit文件组的最大的offset，其通过心跳包的方式上报给master。

很明显，对于第一种情况(slave未同步过master的数据)，offset值不可能大于master的最大offset。

对于第二种情况，是有可能的。

3 实验

模拟slave上报给master的offset过大，很容易联想到如下步骤：

1. 启动一个新的master。
2. 增加一个新的slave。
3. 发送一些消息。
4. 关闭master，并重新部署一个新的同样brokerName的master。

此时，slave自动变成新master的slave了，但是其commit log的offset是之前master的，肯定大于新master的。

**结论：如果重部master，一定要部署新的slave，老的slave重启没用。**

另外，如果已经发生这样的情况，移除旧的slave，再部署新的slave并不起作用，因为master已经持有旧的slave上报上来的offset，`notifyTransferSome(final long offset)`中的`for (... offset > value; ) `会阻止push2SlaveMaxOffset更新。

4 影响

1. 上面的情况影响的是同步双写模式的master，因为此模式的master会通过push2SlaveMaxOffset检测是否写入到slave了。
2. 其次，发送错误偏移量的slave将无法正确同步数据。
3. storeerror.log出现大量错误日志，broker.log和store.log出现诡异的日志，影响主从数据同步情况的判断。

## <span id="resolution">三、解决方案</span>

1. broker迁移

   由于存储文件巨大，一般broker迁移时，采取的方案是下掉旧的broker，部署新的broker的[方案](https://github.com/sohutv/mqcloud/wiki/broker%E8%BF%81%E7%A7%BB)。那么slave一定也要部署新的，即使slave的机器不用迁移，也不能用旧的slave。

2. 已经存在的broker

   1. 执行停写。
   2. 下掉slave。
   3. 停止master。
   4. 启动master。
   5. 部署新的slave。

   注意以上顺序，master必须重启，否则此问题依然存在。

## <span id="other">四、其余情况</span>

导致这样的情况的还有一种可能，就是非rocketmq slave程序和master的slave端口进行了通信，进行如下测试（假设master在127.0.0.1的10916监听slave请求，在127.0.0.2进行测试）：

1. telnet 127.0.0.1 10916

2. 发送999999999

3. 观察master日志：

   ```
   2019-12-04 11:35:12 INFO AcceptSocketService - HAService receive new connection, /127.0.0.2:38824
   2019-12-04 11:35:12 INFO ReadSocketService - ReadSocketService service started
   2019-12-04 11:35:12 INFO WriteSocketService - WriteSocketService service started
   2019-12-04 11:35:16 INFO ReadSocketService - slave[/127.0.0.2:38824] request offset 4123389851770370361
   2019-12-04 11:35:16 INFO WriteSocketService - master transfer data from 4123389851770370361 to slave[/127.0.0.2:38824], and slave request 4123389851770370361
   2019-12-04 11:35:36 WARN ReadSocketService - ha housekeeping, found this connection[/127.0.0.2:38824] expired, 20020
   2019-12-04 11:35:36 INFO ReadSocketService - makestop thread ReadSocketService
   2019-12-04 11:35:36 INFO ReadSocketService - makestop thread WriteSocketService
   2019-12-04 11:35:36 INFO ReadSocketService - ReadSocketService service end
   2019-12-04 11:35:36 INFO WriteSocketService - makestop thread WriteSocketService
   2019-12-04 11:35:36 INFO WriteSocketService - makestop thread ReadSocketService
   2019-12-04 11:35:36 INFO WriteSocketService - WriteSocketService service end
   2019-12-04 11:35:53 INFO BrokerControllerScheduledThread1 - slave fall behind master: 27539373 - 4123389851770370361 = -4123389851742830988 bytes
   ```

由于rocketmq的同步机制并未进行安全校验，所以任何程序都有可能链接此端口进行通信，导致问题发生。

另附rocketmq解析通信数据的代码：

```
if ((this.byteBufferRead.position() - this.processPostion) >= 8) {
    int pos = this.byteBufferRead.position() - (this.byteBufferRead.position() % 8);
    long readOffset = this.byteBufferRead.getLong(pos - 8);
    this.processPostion = pos;

    HAConnection.this.slaveAckOffset = readOffset;
    if (HAConnection.this.slaveRequestOffset < 0) {
        HAConnection.this.slaveRequestOffset = readOffset;
        log.info("slave[" + HAConnection.this.clientAddr + "] request offset " + readOffset);
    }

    HAConnection.this.haService.notifyTransferSome(HAConnection.this.slaveAckOffset);
}
```

即，只要连接master监听的slave通信端口，发送数据大于8个字节，就可能导致该问题的产生。