alter table `audit_consumer_config` add column `unregister` tinyint(4) DEFAULT NULL COMMENT '0:不解注册,1:解注册';
alter table `consumer_config` add column `unregister` tinyint(4) DEFAULT NULL COMMENT '0:不解注册,1:解注册';
alter table `broker` add column `writable` int(4) NOT NULL DEFAULT '1' COMMENT '0:不可写入,1:可写入';
alter table `audit_associate_producer` add column `http_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启http生产,1:开启http生产';
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('oldReqestCodeBrokerSet', '', '使用旧请求码的broker列表，例如：["127.0.0.1:10911","127.0.0.2:10911"]');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('rocketmq5FilePath', 'classpath:static/software/rocketmq5.zip', 'rocketmq5安装文件路径，支持以下三种资源加载方式,例如 1:classpath:static/software/rocketmq5.zip 2：file:///tmp/rocketmq5.zip 3：http://127.0.0.1:8080/software/rocketmq5.zip');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('clientGroupNSConfig', '{}', '客户端ns配置');

DROP TABLE IF EXISTS `controller`;
CREATE TABLE `controller` (
    `cid`          int(11) NOT NULL COMMENT '集群id',
    `addr`         varchar(255) NOT NULL COMMENT 'controller 地址',
    `create_time`  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `check_status` tinyint(4) DEFAULT 0 COMMENT '检测结果:0:未知,1:正常,2:异常',
    `check_time`   datetime COMMENT '检测时间',
    `base_dir`     varchar(360) DEFAULT '/opt/mqcloud/controller' COMMENT '安装路径',
    UNIQUE KEY `cid` (`cid`, `addr`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='controller表';

-- ----------------------------
-- Table structure for `proxy`
-- ----------------------------
DROP TABLE IF EXISTS `proxy`;
CREATE TABLE `proxy` (
    `cid`          int(11) NOT NULL COMMENT '集群id',
    `addr`         varchar(255) NOT NULL COMMENT 'proxy grpc 地址',
    `create_time`  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `check_status` tinyint(4) DEFAULT 0 COMMENT '检测结果:0:未知,1:正常,2:异常',
    `check_time`   datetime COMMENT '检测时间',
    `base_dir`     varchar(360) DEFAULT '/opt/mqcloud/proxy' COMMENT '安装路径',
    `config`       text COMMENT '配置',
    UNIQUE KEY `cid` (`cid`, `addr`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='proxy表';
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsExporterType', 'DISABLE', '指标输出类型', null, 32, 0, 'DISABLE:DISABLE;OTLP_GRPC:OTLP_GRPC;PROM:PROM;LOG:LOG;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsGrpcExporterTarget', null, 'OTLP_GRPC输出地址', null, 33, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsGrpcExporterHeader', null, 'OTLP_GRPC输出header', null, 34, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricGrpcExporterTimeOutInMills', '3000', 'OTLP_GRPC输出超时时间', '默认3秒，单位ms', 35, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricGrpcExporterIntervalInMills', '60000', 'OTLP_GRPC输出间隔', '默认一分钟，单位ms', 36, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricLoggingExporterIntervalInMills', '10000', '指标日志输出间隔', '默认10秒，单位ms', 37, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsPromExporterPort', '5557', 'PROM指标输出端口', null, 38, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsPromExporterHost', null, 'PROM指标输出ip', null, 39, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsLabel', null, '指标标签', '格式:name1:label1,name2:label2', 40, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'validateSystemTopicWhenUpdateTopic', 'false', '新建topic是否校验是不是系统topic', null, 41, 1, null, 1);

insert into broker_config_group(`id`, `group`, `order`) values(25, 'controller配置', 25);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'enableControllerMode', 'false', '是否启用controller模式', null, 1, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'controllerAddr', null, 'controller的地址', 'ip:port;ip:port;或域名:port', 2, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'fetchControllerAddrByDnsLookup', 'false', '是否使用域名获取controller的地址', 'controllerAddr填写域名时请启用该选项', 3, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'controllerHeartBeatTimeoutMills', '10000', 'broker和controller之间心跳超时时间', '单位ms,默认10秒', 4, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'syncBrokerMetadataPeriod', '5000', '向controller同步broker副本信息的时间间隔', '单位ms,默认5秒', 5, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'checkSyncStateSetPeriod', '5000', '检查SyncStateSet的时间间隔', '单位ms,默认5秒', 6, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'syncControllerMetadataPeriod', '10000', '同步controller元数据的时间间隔，主要是获取active controller的地址', '单位ms,默认10秒', 7, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'haMaxTimeSlaveNotCatchup', '15000', 'slave没有跟上Master的最大时间间隔，若在SyncStateSet中的slave超过该时间间隔会将其从SyncStateSet移除', '单位ms,默认15秒', 8, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'storePathEpochFile', null, '存储epoch文件的位置', '默认在store目录下', 9, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'allAckInSyncStateSet', 'false', '一条消息需要复制到SyncStateSet中的每一个副本才会向客户端返回成功，可以保证消息不丢失', null, 10, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'syncFromLastFile', 'false', '若slave是空盘启动，是否从最后一个文件进行复制', null, 11, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'asyncLearner', 'false', '异步复制的learner', '为true时不参与选举', 12, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'inSyncReplicas', '1', '需保持同步的副本组数量', 'allAckInSyncStateSet=true时该参数无效', 13, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'minInSyncReplicas', '1', '最小需保持同步的副本组数量', '若SyncStateSet中副本个数小于minInSyncReplicas则putMessage直接返回PutMessageStatus.IN_SYNC_REPLICAS_NOT_ENOUGH', 14, 0, null, 0);

insert into broker_config_group(`id`, `group`, `order`) values(26, '压缩topic配置', 26);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(26, 'compactionMappedFileSize', '104857600', 'CompactinLog文件大小', '默认100m', 1, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(26, 'compactionThreadNum', '6', '压缩线程数', null, 2, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(26, 'compactionScheduleInternal', '900000', '压缩间隔', null, 3, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(26, 'compactionCqMappedFileSize', '10485760', 'CompactinConsumeQueue文件大小', '默认10m', 4, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(26, 'enableCompaction', 'true', '是否启用压缩', null, 5, 0, 'true:是;false:否;', 0);

insert into broker_config_group(`id`, `group`, `order`) values(27, '时间轮延迟消息', 27);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'mappedFileSizeTimerLog', '104857600', 'TimerLog文件大小', '默认100m', 1, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerPrecisionMs', '1000', '时间轮精度', '单位毫秒', 2, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerRollWindowSlot', '172800', '超长延迟消息滚动窗口', '默认超过2天会滚动，单位ms', 3, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerFlushIntervalMs', '1000', '延迟消息刷新间隔', '默认1秒，单位ms', 4, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerGetMessageThreadNum', '3', '出dequeueGetQueue从commitLog拉取消息，放入dequeuePutQueue的线程数', null, 5, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerPutMessageThreadNum', '3', '出dequeuePutQueue放入原始队列线程数', null, 6, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerEnableDisruptor', 'false', '是否使用Disruptor', null, 7, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerEnableCheckMetrics', 'true', '是否检查指标', null, 8, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerCheckMetricsWhen', '05', '指标检查时间', null, 9, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerMaxDelaySec', '259200', '延迟消息最大延迟', '默认3天，单位s', 10, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerWheelEnable', 'true', '是否启用时间轮延迟消息', null, 11, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerStopEnqueue', 'false', '是否停止写入enqueuePutQueue', null, 12, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerStopDequeue', 'false', '是否停止写入dequeueGetQueue', null, 13, 1, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerSkipUnknownError', 'false', '发生未知错误时是否跳过', null, 14, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerCongestNumEachSlot', '2147483647', '每个槽最大消息量', null, 15, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerMetricSmallThreshold', '1000000', '指标小阈值', null, 16, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerProgressLogIntervalMs', '10000', '延迟消息处理日志打印间隔', '默认10秒，单位ms', 17, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerWarmEnable', 'false', '已废弃', null, 18, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerInterceptDelayLevel', 'false', '已废弃', null, 19, 0, null, 0);

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'grpcServerPort', '8081', 'proxy grpc协议端口', null, 19, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'remotingListenPort', '8080', 'proxy remoting协议端口', null, 20, 0, null, 0);
