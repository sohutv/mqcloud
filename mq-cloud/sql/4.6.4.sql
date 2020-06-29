CREATE TABLE `audit_consumer_config` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `consumer_id` int(11) DEFAULT NULL COMMENT 'consumer id',
  `permits_per_second` float DEFAULT NULL COMMENT 'qps',
  `enable_rate_limit` tinyint(4) DEFAULT NULL COMMENT '0:不限速,1:限速',
  `pause` tinyint(4) DEFAULT NULL COMMENT '0:不暂停,1:暂停',
  `pause_client_id` varchar(255) DEFAULT NULL COMMENT '暂停的客户端Id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核消费者配置相关表';

drop table message_reset;
CREATE TABLE `consumer_config` (
  `consumer` varchar(64) NOT NULL COMMENT 'consumer名',
  `retry_message_reset_to` bigint(20) DEFAULT NULL COMMENT '重置至时间戳，小于此时间的都将不再消息',
  `permits_per_second` float DEFAULT NULL COMMENT 'qps',
  `enable_rate_limit` tinyint(4) DEFAULT NULL COMMENT '0:不限速,1:限速',
  `pause` tinyint(4) DEFAULT NULL COMMENT '0:不暂停,1:暂停',
  `pause_client_id` varchar(255) DEFAULT NULL COMMENT '暂停的客户端Id',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `consumer` (`consumer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='客户端配置表';

CREATE TABLE `cluster_config` (
  `cid` int(11) NOT NULL COMMENT '集群id',
  `bid` int(11) NOT NULL COMMENT 'broker config id',
  `online_value` varchar(256) COMMENT '线上值',
  UNIQUE KEY `cid_key` (`cid`,`bid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `broker_config_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group` varchar(255) NOT NULL COMMENT '配置组',
  `order` int(11) NOT NULL COMMENT '序号小排前',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_key`  (`group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `broker_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gid` int(11) NOT NULL COMMENT '组id',
  `key` varchar(255) NOT NULL COMMENT '配置key',
  `value` varchar(256) COMMENT '配置值',
  `desc` varchar(256) COMMENT '描述',
  `tip` varchar(256) COMMENT '提示',
  `order` int(11) NOT NULL COMMENT '序号小排前',
  `dynamic_modify` tinyint(4) NOT NULL COMMENT '0:修改后需要重启,1:修改后实时生效',
  `option` varchar(256) COMMENT '选项',
  `required` tinyint(4) DEFAULT 0 COMMENT '0:可选配置,1:必选配置',
  PRIMARY KEY (`id`),
  UNIQUE KEY `bkey`  (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into broker_config_group(`id`, `group`, `order`) values(1, '常见配置', 1);
insert into broker_config_group(`id`, `group`, `order`) values(2, '发现机制', 2);
insert into broker_config_group(`id`, `group`, `order`) values(3, 'topic&订阅', 3);
insert into broker_config_group(`id`, `group`, `order`) values(4, '写入限流机制', 4);
insert into broker_config_group(`id`, `group`, `order`) values(5, '请求处理线程池', 5);
insert into broker_config_group(`id`, `group`, `order`) values(6, '内存预热', 6);
insert into broker_config_group(`id`, `group`, `order`) values(7, 'CommitLog相关', 7);
insert into broker_config_group(`id`, `group`, `order`) values(8, 'ConsumeQueue相关', 8);
insert into broker_config_group(`id`, `group`, `order`) values(9, '堆外内存相关', 9);
insert into broker_config_group(`id`, `group`, `order`) values(10, 'HA机制', 10);
insert into broker_config_group(`id`, `group`, `order`) values(11, '数据文件保留机制', 11);
insert into broker_config_group(`id`, `group`, `order`) values(12, '消息相关', 12);
insert into broker_config_group(`id`, `group`, `order`) values(13, '消费优化', 13);
insert into broker_config_group(`id`, `group`, `order`) values(14, '拉取消息', 14);
insert into broker_config_group(`id`, `group`, `order`) values(15, '快速失败机制', 15);
insert into broker_config_group(`id`, `group`, `order`) values(16, 'broker保护机制', 16);
insert into broker_config_group(`id`, `group`, `order`) values(17, '注册相关', 17);
insert into broker_config_group(`id`, `group`, `order`) values(18, '事务相关', 18);
insert into broker_config_group(`id`, `group`, `order`) values(19, 'salve相关', 19);
insert into broker_config_group(`id`, `group`, `order`) values(20, 'filter相关', 20);
insert into broker_config_group(`id`, `group`, `order`) values(21, 'netty server相关', 21);
insert into broker_config_group(`id`, `group`, `order`) values(22, 'netty client相关', 22);
insert into broker_config_group(`id`, `group`, `order`) values(23, 'rpc消息', 23);
insert into broker_config_group(`id`, `group`, `order`) values(24, '其他配置', 24);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerName', null, 'broker名', null, 1, 0, null, 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'rocketmqHome', null, 'broker安装目录', '启动脚本中已经设置,无需再设置', 2, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerRole', 'ASYNC_MASTER', 'broker角色', null, 3, 0, 'ASYNC_MASTER:异步复制master;SYNC_MASTER:同步双写master;SLAVE:slave;', 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'flushDiskType', 'ASYNC_FLUSH', '刷盘机制', null, 4, 0, 'ASYNC_FLUSH:异步刷盘;SYNC_FLUSH:同步刷盘;', 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerIP1', null, 'broker的ip', '服务器ip,尤其对于多网卡情况', 5, 0, null, 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerIP2', null, 'master HA ip', '与brokerIP1一致', 6, 0, null, 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'listenPort', '10911', 'broker监听端口', null, 7, 0, null, 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerClusterName', null, '集群名', null, 8, 0, null, 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerId', '0', '0:master,非0:slave', null, 9, 0, null, 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerPermission', '6', 'broker权限', 'broker下线可以设置为只读', 10, 1, '2:只写;4:只读;6:读写', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'storePathRootDir', null, '数据文件存储根目录', '务必设置', 11, 0, null, 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'storePathCommitLog', null, 'CommitLog文件存储根目录', '务必设置', 12, 0, null, 1);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(2, 'namesrvAddr', null, 'namesrv地址', '若采用域名寻址模式无需设置', 1, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(2, 'fetchNamesrvAddrByAddressServer', 'true', '域名方式获取NameServer地址', '若配置namesrvAddr无需设置此项', 2, 0, 'true:是;false:否;', 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(2, 'rmqAddressServerDomain', null, 'NameServer地址域名', null, 3, 0, null, 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(2, 'rmqAddressServerSubGroup', null, 'NameServer地址域名子路径', null, 4, 0, null, 1);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'autoCreateTopicEnable', 'true', '发送消息时没有topic自动创建', '线上建议设置为false', 1, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'autoCreateSubscriptionGroup', 'true', '自动创建订阅组', '线上建议设置为false', 2, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'defaultTopicQueueNums', '8', 'autoCreateTopicEnable为true时,创建topic的队列数', '无需修改', 3, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'clusterTopicEnable', 'true', '自动创建以cluster为名字的topic', null, 4, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'brokerTopicEnable', 'true', '自动创建以broker为名字的topic', null, 5, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'traceTopicEnable', 'false', '是否启用trace topic', null, 6, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'msgTraceTopicName', 'RMQ_SYS_TRACE_TOPIC', '默认的trace topic名', null, 7, 0, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(4, 'useReentrantLockWhenPutMessage', 'false', '写消息是否使用重入锁', '若不使用建议配合transientStorePool使用；若使用要加大sendMessageThreadPoolNums', 1, 0, 'true:是;false:否;', 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(4, 'sendMessageThreadPoolNums', '1', '处理发消息的线程池大小', '默认使用spin锁', 2, 0, null, 1);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(4, 'waitTimeMillsInSendQueue', '200', '消息发送请求超过阈值没有处理则返回失败', '若出现broker busy建议调大', 3, 1, null, 1);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'sendThreadPoolQueueCapacity', '10000', '处理发消息的线程池队列大小', null, 2, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'pullMessageThreadPoolNums', '16+核数*2', '处理拉消息的线程池大小', null, 3, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'pullThreadPoolQueueCapacity', '100000', '处理拉消息的线程池队列大小', null, 4, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'queryMessageThreadPoolNums', '8+核数', '处理消息查询的线程池大小', null, 5, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'queryThreadPoolQueueCapacity', '20000', '处理消息查询的线程池队列大小', null, 6, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'adminBrokerThreadPoolNums', '16', '处理admin请求的线程池大小', null, 7, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'clientManageThreadPoolNums', '32', '处理客户端(解注册等)的线程池大小', null, 8, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'clientManagerThreadPoolQueueCapacity', '1000000', '处理客户端(解注册等)的线程池队列大小', null, 9, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'consumerManageThreadPoolNums', '32', '处理消费者请求的线程池大小', null, 10, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'consumerManagerThreadPoolQueueCapacity', '1000000', '处理消费者请求的线程池队列大小', null, 11, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'heartbeatThreadPoolNums', 'min(32, 核数)', '处理客户端心跳的线程池大小', null, 12, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'heartbeatThreadPoolQueueCapacity', '50000', '处理客户端心跳的线程池队列大小', null, 13, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'endTransactionThreadPoolNums', '8+核数*2', '处理结束事务请求的线程池大小', null, 14, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'endTransactionPoolQueueCapacity', '100000', '处理结束事务请求的线程池队列大小', null, 15, 0, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(6, 'warmMapedFileEnable', 'false', 'mmap时进行是否进行内存预热，避免缺页异常', null, 1, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(6, 'flushLeastPagesWhenWarmMapedFile', '4096', '预热时同时刷多少页内存(同步刷盘时)', null, 2, 1, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'mappedFileSizeCommitLog', '1073741824', 'CommitLog文件大小', '默认大小1G，如非必要请勿修改', 1, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'flushIntervalCommitLog', '500', '异步刷盘时间间隔', '单位ms', 2, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'flushCommitLogTimed', 'false', '是否定时刷CommitLog，若否会实时刷', null, 3, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'flushCommitLogLeastPages', '4', '最少凑够多少页内存才刷CommitLog', null, 4, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'flushCommitLogThoroughInterval', '10000', '两次刷CommitLog最大间隔，若超过，不校验页数直接刷', '单位ms,默认10秒', 5, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'syncFlushTimeout', '5000', '同步刷CommitLog超时时间', '单位ms,默认5秒', 6, 1, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(8, 'mappedFileSizeConsumeQueue', '6000000', 'ConsumeQueue文件存储的条目', '默认为30万，如非必要请勿修改', 1, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(8, 'flushIntervalConsumeQueue', '1000', '异步刷ConsumeQueue时间间隔', '单位ms,默认1秒', 2, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(8, 'flushConsumeQueueLeastPages', '2', '最少凑够多少页内存才刷ConsumeQueue', null, 3, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(8, 'flushConsumeQueueThoroughInterval', '60000', '两次刷ConsumeQueue最大间隔，若超过，不校验页数直接刷', '单位ms,默认1分钟', 4, 1, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'transientStorePoolEnable', 'false', '是否启动堆外内存池加速写', null, 1, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'transientStorePoolSize', '5', '堆外内存池大小', null, 2, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'fastFailIfNoBufferInStorePool', 'false', '是否启用快速失败', null, 3, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'commitIntervalCommitLog', '200', '异步刷堆外内存时间间隔', '单位ms', 4, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'commitCommitLogLeastPages', '4', '最少凑够多少页内存才刷堆外内存', null, 5, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'commitCommitLogThoroughInterval', '200', '两次刷堆外内存最大间隔，若超过，不校验页数直接刷', '单位ms', 6, 1, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(10, 'haListenPort', '10912', 'master监听的HA端口', null, 1, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(10, 'haHousekeepingInterval', '20000', 'master与slave链接超时间隔', '单位ms,默认20秒', 2, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(10, 'haTransferBatchSize', '32768', 'master批量传输给slave数据大小', '单位字节,默认32K', 3, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(10, 'haSlaveFallbehindMax', '268435456', '同步双写master，判断slave落后大于多少为不可用', '单位字节,默认256M', 4, 1, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'fileReservedTime', '72', 'CommitLog保留的时间', '单位小时,默认3天', 1, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'deleteWhen', '04', 'CommitLog删除时间点，多个用;分隔', null, 2, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'destroyMapedFileIntervalForcibly', '120000', '删除文件时，若文件被占用，等待多久后强制删除', '单位ms,默认2分钟', 3, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'cleanFileForciblyEnable', 'true', '磁盘超过阈值、且无过期文件情况下, 是否强制删除文件', null, 4, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'diskMaxUsedSpaceRatio', '75', '磁盘最大使用阈值', null, 5, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'deleteCommitLogFilesInterval', '100', '删除CommitLog间隔，中间将sleep', '单位ms', 6, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'deleteConsumeQueueFilesInterval', '100', '删除ConsumeQueue间隔，中间将sleep', '单位ms', 7, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'redeleteHangedFileInterval', '120000', '重新删除已经执行过删除却未删掉的文件间隔', '单位ms,默认2分钟', 8, 1, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'maxMessageSize', '4194304', '单条消息最大大小', '单位字节,默认4M', 1, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'messageIndexEnable', 'true', '消息是否开启索引', null, 2, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'messageIndexSafe', 'false', '消息索引恢复时是否安全校验', null, 3, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'maxHashSlotNum', '5000000', '单个消息文件hash槽个数', '默认5百万', 4, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'maxIndexNum', '20000000', '单个消息文件hash槽个数', '默认2千万', 5, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'messageDelayLevel', '1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h', '延迟消息队列', null, 6, 0, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(13, 'slaveReadEnable', 'false', '拉取消息在硬盘时是否可以从slave拉取', '设置为true分担master压力', 1, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(13, 'longPollingEnable', 'true', '针对消费拉消息是否开启长轮询', null, 2, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(13, 'shortPollingTimeMills', '1000', '针对消费拉消息短轮询时间', '单位ms,默认1秒', 3, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(13, 'notifyConsumerIdsChangedEnable', 'true', '消费者上下线时是否通知客户端,以便再平衡', null, 4, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(13, 'transferMsgByHeap', 'true', '传输数据时是否使用零拷贝', '若消息量不大,基本都在pagecache,建议为false.否则消息在硬盘使用零拷贝会卡netty线程', 5, 1, 'true:是;false:否;', 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'accessMessageInMemoryMaxRatio', '40', '判断消息是否在内存的依据，此值仅为预估值，不准确', null, 1, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'maxTransferBytesOnMessageInMemory', '262144', '单次拉取内存消息传输的最大字节', '单位字节,默认256K', 2, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'maxTransferCountOnMessageInMemory', '32', '单次拉取内存消息传输的最大数量', null, 3, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'maxTransferBytesOnMessageInDisk', '65536', '单次拉取硬盘消息传输的最大字节', '单位字节,默认64K', 4, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'maxTransferCountOnMessageInDisk', '8', '单次拉取硬盘消息传输的最大数量', null, 5, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'maxMsgsNumBatch', '64', '按照key查询一次返回多少条消息（主要用于admin工具查询）', null, 6, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'defaultQueryMaxNum', '32', '按照msgId查询一次返回多少条消息（主要用于admin工具查询）', null, 7, 1, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(15, 'osPageCacheBusyTimeOutMills', '1000', '消息存储超过此时间，则将丢弃所有的写入请求', '单位ms,默认1秒', 1, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(15, 'brokerFastFailureEnable', 'true', '是否启用快速失败机制', null, 2, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(15, 'waitTimeMillsInPullQueue', '5000', '消息拉取请求超过阈值没有处理则返回失败', '单位ms,默认5秒', 4, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(15, 'waitTimeMillsInHeartbeatQueue', '31000', 'client心跳请求超过阈值没有处理则返回失败', '单位ms,默认31秒', 5, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(15, 'waitTimeMillsInTransactionQueue', '3000', '事务结束请求超过阈值没有处理则返回失败', '单位ms,默认3秒', 6, 1, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(16, 'startAcceptSendRequestTimeStamp', '0', 'broker启动多久后可以接受请求', '单位ms,默认0ms', 1, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(16, 'disableConsumeIfConsumerReadSlowly', 'false', '是否禁用消费慢的消费者', '启用slaveReadEnable代替此功能', 2, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(16, 'consumerFallbehindThreshold', '17179869184', '消费者拉取消息大小超过此值认为消费慢', '单位字节,默认16G', 3, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(16, 'diskFallRecorded', 'true', '记录消费者拉取消息大小', '不启用disableConsumeIfConsumerReadSlowly可以选否', 4, 1, 'true:是;false:否;', 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(17, 'compressedRegister', 'false', '向NameServer注册时数据是否压缩', 'topic过多可以开启', 1, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(17, 'forceRegister', 'true', '向NameServer注册时是否强制每次发送数据', 'topic过多可以关闭', 2, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(17, 'registerNameServerPeriod', '30000', '向NameServer注册周期', '单位ms,默认30秒', 3, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(17, 'registerBrokerTimeoutMills', '6000', '向NameServer注册时超时时间', '单位ms,默认6秒', 4, 1, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(18, 'rejectTransactionMessage', 'false', '是否拒绝发送事务消息', '非事务集群设置为true', 1, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(18, 'transactionTimeOut', '6000', '事务消息超过多久后首次检查', '单位ms,默认6秒', 2, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(18, 'transactionCheckMax', '15', '事务消息最大检查次数', null, 3, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(18, 'transactionCheckInterval', '60000', '事务消息检查间隔', '单位ms,默认60秒', 4, 1, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(19, 'haSendHeartbeatInterval', '5000', 'slave与master心跳间隔', '单位ms,默认5秒', 1, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(19, 'haMasterAddress', null, 'slave的HA master', '无需设置,向NameServer注册会返回master地址作为HA地址', 2, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(19, 'offsetCheckInSlave', 'false', '消费者从slave拉取消息offset不正确时，slave是否检查更正', null, 2, 1, 'true:是;false:否;', 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'filterServerNums', '0', '过滤服务数', null, 1, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'enableCalcFilterBitMap', 'false', '是否启用BitMap过滤计算', null, 2, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'bitMapLengthConsumeQueueExt', '112', 'BitMap大小', null, 3, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'expectConsumerNumUseFilter', '32', '预估的订阅同一topic的消费者数', null, 4, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'maxErrorRateOfBloomFilter', '20', 'bloom filter错误率', '单位%,默认20%', 5, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'filterDataCleanTimeSpan', '86400000', '清理n小时之前的filter数据', '单位ms,默认24小时', 6, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'enableConsumeQueueExt', 'false', '是否生成额外的consume queue文件', null, 7, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'mappedFileSizeConsumeQueueExt', '50331648', '额外的consume queue文件大小', '单位字节,默认48M', 8, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'filterSupportRetry', 'false', '是否支持过滤retry消费者', null, 9, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'enablePropertyFilter', 'false', '是否支持过滤SQL92', null, 10, 1, 'true:是;false:否;', 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverWorkerThreads', '8', 'worker线程', null, 1, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverCallbackExecutorThreads', '0', '默认公共线程', null, 2, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverSelectorThreads', '3', 'selector线程', null, 3, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverOnewaySemaphoreValue', '256', 'oneway信号量', null, 4, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverAsyncSemaphoreValue', '64', 'aysnc信号量', null, 5, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverChannelMaxIdleTimeSeconds', '120', 'idle最大时间', null, 6, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverSocketSndBufSize', '131072', 'SO_SNDBUF', null, 7, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverSocketRcvBufSize', '131072', 'SO_RCVBUF', null, 8, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverPooledByteBufAllocatorEnable', 'true', '是否开启bytebuffer池', null, 9, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'useEpollNativeSelector', 'false', '是否使用epoll', null, 10, 0, 'true:是;false:否;', 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientWorkerThreads', '4', 'worker线程', null, 1, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientCallbackExecutorThreads', '核数', '默认公共线程', null, 2, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientOnewaySemaphoreValue', '65535', 'oneway信号量', null, 3, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientAsyncSemaphoreValue', '65535', 'aysnc信号量', null, 4, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'channelNotActiveInterval', '60000', '此项作废', null, 5, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientChannelMaxIdleTimeSeconds', '120', '链接最大idle时间', null, 6, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'connectTimeoutMillis', '3000', '连接超时时间', null, 7, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientSocketSndBufSize', '131072', 'SO_SNDBUF', null, 8, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientSocketRcvBufSize', '131072', 'SO_RCVBUF', null, 9, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientPooledByteBufAllocatorEnable', 'false', '是否开启bytebuffer池', null, 10, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientCloseSocketIfTimeout', 'false', '超时是否关闭连接', null, 11, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'useTLS', 'false', '是否使用ssl', null, 12, 0, 'true:是;false:否;', 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(23, 'storeReplyMessageEnable', 'true', '是否启用rpc消息', null, 1, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(23, 'processReplyMessageThreadPoolNums', '16+核数*2', '处理rpc消息线程池大小', null, 2, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(23, 'replyThreadPoolQueueCapacity', '10000', '处理rpc消息的线程池队列大小', null, 3, 0, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'traceOn', 'true', '是否启用trace', null, 1, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'aclEnable', 'false', '是否启用权限校验,若启用需要配置权限文件', null, 2, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'messageStorePlugIn', null, '消息存储插件', null, 3, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'checkCRCOnRecover', 'true', 'load完消息校验消息是否用CRC32', null, 4, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'debugLockEnable', 'false', 'commitlog写入超过1秒打印堆栈', null, 5, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'cleanResourceInterval', '10000', 'CommitLog&ConsumeQueue清除任务执行间隔', '单位ms,默认10秒', 6, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'flushConsumerOffsetInterval', '5000', 'consumerOffset.json持久化间隔', '单位ms,默认5秒', 7, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'flushConsumerOffsetHistoryInterval', '60000', '此项作废,以flushConsumerOffsetInterval为准', null, 8, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'flushDelayOffsetInterval', '10000', 'delayOffset.json持久化间隔', '单位ms,默认10秒', 9, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'abortFile', null, '自动以storePathRootDir拼装', '无需设置', 10, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'storePathIndex', null, '自动以storePathRootDir拼装', '无需设置', 11, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'storeCheckpoint', null, '自动以storePathRootDir拼装', '无需设置', 12, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'highSpeedMode', 'false', '此项作废', null, 14, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'commercialEnable', 'true', '此项作废', null, 15, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'commercialTimerCount', '1', '此项作废', null, 16, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'commercialTransCount', '1', '此项作废', null, 17, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'commercialBigCount', '1', '此项作废', null, 18, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'commercialBaseCount', '1', '此项作废', null, 19, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'putMsgIndexHightWater', '600000', '此项作废', null, 20, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'maxDelayTime', '40', '此项作废', null, 21, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'regionId', 'DefaultRegion', 'broker区域，trace使用', null, 22, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'duplicationEnable', 'false', '是否支持重写consume queue', null, 23, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'enableDLegerCommitLog', 'false', '是否支持DLeger', null, 24, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'dLegerGroup', null, 'DLeger相关配置', null, 25, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'dLegerPeers', null, 'DLeger相关配置', null, 26, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'dLegerSelfId', null, 'DLeger相关配置', null, 27, 0, null, 0);