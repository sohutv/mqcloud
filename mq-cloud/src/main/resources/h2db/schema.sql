-- ----------------------------
-- Table structure for `audit`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户id',
  `type` tinyint(4) NOT NULL COMMENT '申请类型:0:新建TOPIC,1:修改TOPIC ,2:删除TOPIC ,3:新建消费者,4:删除消费者,5:重置offset,6:跳过堆积,7:关联生产者,8:关联消费者,9:成为管理员',
  `info` varchar(360) DEFAULT NULL COMMENT '申请描述',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0:等待审批,1:审批通过,2:驳回',
  `refuse_reason` varchar(360) DEFAULT NULL COMMENT '驳回理由',
  `auditor` varchar(64) DEFAULT NULL COMMENT '审计员(邮箱)',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for `audit_associate_consumer`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_associate_consumer` (
  `uid` int(11) NOT NULL COMMENT '关联的用户ID',
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `cid` int(11) NOT NULL COMMENT 'consumer id',
  PRIMARY KEY (`aid`)
);

-- ----------------------------
-- Table structure for `audit_associate_producer`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_associate_producer` (
  `uid` int(11) NOT NULL COMMENT '关联的用户ID',
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `producer` varchar(64) NOT NULL COMMENT '关联的生产者名字',
  PRIMARY KEY (`aid`)
);

-- ----------------------------
-- Table structure for `audit_consumer`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_consumer` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `consumer` varchar(64) NOT NULL COMMENT '消费者名字',
  `consume_way` int(4) NOT NULL DEFAULT '0' COMMENT '0:集群消费,1:广播消费',
  `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace',
  `permits_per_second` int(11) DEFAULT NULL COMMENT 'qps',
  UNIQUE KEY `tid` (`tid`,`consumer`)
);

-- ----------------------------
-- Table structure for `audit_consumer_delete`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_consumer_delete` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `cid` int(11) NOT NULL COMMENT 'consumer id',
  `consumer` varchar(64) DEFAULT NULL COMMENT 'consumer名字',
  `topic` varchar(64) DEFAULT NULL
);

-- ----------------------------
-- Table structure for `audit_producer_delete`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_producer_delete` (
  `uid` int(11) NOT NULL,
  `aid` int(11) NOT NULL COMMENT '审核id',
  `pid` int(11) NOT NULL COMMENT 'userProducer id',
  `producer` varchar(64) DEFAULT NULL,
  `topic` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`aid`)
);

-- ----------------------------
-- Table structure for `audit_reset_offset`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_reset_offset` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `consumer_id` int(11) DEFAULT NULL COMMENT 'consumer id',
  `offset` varchar(64) DEFAULT NULL COMMENT 'null:重置为最大offset,时间戳:重置为某时间点(yyyy-MM-dd#HH:mm:ss:SSS)',
  `message_key` varchar(360) DEFAULT NULL COMMENT '消息key'
);

-- ----------------------------
-- Table structure for `audit_topic`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_topic` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `name` varchar(64) NOT NULL COMMENT 'topic名',
  `queue_num` int(11) NOT NULL COMMENT '队列长度',
  `producer` varchar(64) NOT NULL COMMENT '生产者名字',
  `ordered` int(4) NOT NULL DEFAULT '0' COMMENT '0:无序,1:有序',
  `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace',
  `transaction_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启事务,1:开启事务',
  `msg_type` int(4) NOT NULL DEFAULT '0' COMMENT '消息类型，0:普通消息，1:延迟消息，2:定时消息',
  `test_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:非测试topic,1:测试topic',
  `info` varchar(360) DEFAULT NULL COMMENT 'topic描述',
  `qps` int(11) DEFAULT NULL COMMENT '消息量qps预估',
  `qpd` int(11) DEFAULT NULL COMMENT '一天消息量预估',
  `serializer` int(4) NOT NULL DEFAULT '0' COMMENT '序列化器 0:Protobuf,1:String'
);

-- ----------------------------
-- Table structure for `audit_topic_delete`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_topic_delete` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `topic` varchar(64) DEFAULT NULL COMMENT 'topic名字'
);

-- ----------------------------
-- Table structure for `audit_topic_update`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_topic_update` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `queue_num` int(11) NOT NULL COMMENT '队列长度'
);

-- ----------------------------
-- Table structure for `audit_user_consumer_delete`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_user_consumer_delete` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `uid` int(11) NOT NULL COMMENT '此消费者对应的用户',
  `ucid` int(11) NOT NULL COMMENT 'user_consumer id',
  `consumer` varchar(64) DEFAULT NULL,
  `topic` varchar(64) DEFAULT NULL
);

-- ----------------------------
-- Table structure for `audit_resend_message`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_resend_message` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `msgId` char(32) NOT NULL COMMENT 'broker offset msg id',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '申请类型:0:未处理,1:发送成功,2:发送失败',
  `times` int(11) NOT NULL DEFAULT '0' COMMENT '发送次数',
  `send_time` datetime COMMENT '发送时间'
);

-- ----------------------------
-- Table structure for `audit_resend_message_consumer`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_resend_message_consumer` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `consumer_id` int(11) NOT NULL COMMENT 'consumer id'
);

-- ----------------------------
-- Table structure for `audit_topic_trace`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_topic_trace` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `trace_enabled` int(11) NOT NULL COMMENT '0:不开启trece,1:开启trace'
);

-- ----------------------------
-- Table structure for `audit_timespan_message_consume`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_timespan_message_consume` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `topic` varchar(64) NOT NULL COMMENT 'topic',
  `consumer` varchar(64) NOT NULL COMMENT 'consumer',
  `client_id` varchar(255) DEFAULT NULL COMMENT '暂停的客户端Id',
  `start` bigint(20) DEFAULT NULL COMMENT '消费开始时间戳',
  `end` bigint(20) DEFAULT NULL COMMENT '消费结束时间戳'
);

-- ----------------------------
-- Table structure for `broker_traffic`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `broker_traffic` (
  `ip` varchar(255) NOT NULL COMMENT 'addr',
  `create_date` date NOT NULL COMMENT '数据收集天',
  `create_time` char(4) NOT NULL COMMENT '数据收集小时分钟,格式:HHMM',
  `cluster_id` int(11) NOT NULL COMMENT 'cluster_id',
  `put_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '生产消息量',
  `put_size` bigint(20) NOT NULL DEFAULT '0' COMMENT '生产消息大小',
  `get_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '消费消息量',
  `get_size` bigint(20) NOT NULL DEFAULT '0' COMMENT '消费消息大小',
  PRIMARY KEY (`ip`,`create_date`,`create_time`)
);

-- ----------------------------
-- Table structure for `client_version`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `client_version` (
  `topic` varchar(255) NOT NULL,
  `client` varchar(255) NOT NULL,
  `role` tinyint(4) NOT NULL COMMENT '1:producer,2:consumer',
  `version` varchar(255) NOT NULL,
  `create_date` date NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `cv_topic` (`topic`,`client`,`role`)
);

-- ----------------------------
-- Table structure for `cluster`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `cluster` (
  `id` int(11) NOT NULL COMMENT '集群id，也会作为ns发现的一部分',
  `name` varchar(64) NOT NULL COMMENT '集群名',
  `vip_channel_enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否开启vip通道, 1:开启, 0:关闭, rocketmq 4.x版本默认开启',
  `online` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否为线上集群, 1:是, 0:否, 线上集群会开启流量抓取',
  `transaction_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不支持事务,1:支持事务',
  `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不支持trace,1:支持trace',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for `common_config`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `common_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(64) DEFAULT NULL COMMENT '配置key',
  `value` varchar(20000) DEFAULT '' COMMENT '配置值',
  `comment` varchar(256) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for `consumer`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `consumer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `name` varchar(64) NOT NULL COMMENT 'consumer名',
  `consume_way` int(4) NOT NULL DEFAULT '0' COMMENT '0:集群消费,1:广播消费',
  `create_date` date NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace',
  `info` varchar(360) DEFAULT NULL COMMENT '消费者描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `cname` (`name`)
);

-- ----------------------------
-- Table structure for `consumer_block`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `consumer_block` (
  `csid` int(11) DEFAULT NULL COMMENT 'consumer_stat id',
  `instance` varchar(255) DEFAULT NULL COMMENT 'consumer instance_id',
  `updatetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `broker` varchar(255) DEFAULT NULL COMMENT 'broker',
  `qid` int(11) DEFAULT NULL COMMENT 'qid',
  `block_time` bigint(20) DEFAULT NULL COMMENT '毫秒=当前时间-最新消费时间',
  `offset_moved_times` int(11) DEFAULT '0' COMMENT 'offset moved times',
  `offset_moved_time` bigint(20) DEFAULT NULL COMMENT 'offset moved msg store time',
  UNIQUE KEY `csid` (`csid`,`broker`,`qid`)
);

-- ----------------------------
-- Table structure for `consumer_stat`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `consumer_stat` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `consumer_group` varchar(255) DEFAULT NULL COMMENT 'consumer group',
  `topic` varchar(255) DEFAULT NULL COMMENT 'host',
  `updatetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `undone_msg_count` bigint(20) DEFAULT NULL COMMENT '未消费的消息量',
  `undone_1q_msg_count`bigint(20) DEFAULT NULL COMMENT '单队列未消费的最大消息量',
  `undone_delay` bigint(20) DEFAULT NULL COMMENT '毫秒=broker最新消息存储时间-最新消费时间',
  `sbscription` varchar(255) DEFAULT NULL COMMENT '订阅关系,如果一个group订阅不同的topic,在这里会有体现',
  PRIMARY KEY (`id`),
  UNIQUE KEY `consumer_group` (`consumer_group`)
);

-- ----------------------------
-- Table structure for `consumer_traffic`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `consumer_traffic` (
  `consumer_id` int(11) NOT NULL DEFAULT '0' COMMENT 'consumer id',
  `create_date` date NOT NULL COMMENT '数据收集天',
  `create_time` char(4) NOT NULL COMMENT '数据收集小时分钟,格式:HHMM',
  `count` bigint(20) DEFAULT NULL COMMENT 'consumer pull times',
  `size` bigint(20) DEFAULT NULL COMMENT 'consumer pull size',
  PRIMARY KEY (`consumer_id`,`create_date`,`create_time`)
);

-- ----------------------------
-- Table structure for `feedback`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户id',
  `content` text NOT NULL COMMENT '反馈内容',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for `need_warn_config`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `need_warn_config` (
  `oKey` varchar(255) NOT NULL COMMENT '报警频率的key（type_topic_group）',
  `times` int(11) NOT NULL COMMENT '次数',
  `update_time` bigint(13) NOT NULL COMMENT '计时起始时间时间',
  UNIQUE KEY `key` (`oKey`)
);

-- ----------------------------
-- Table structure for `notice`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `notice` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(512) NOT NULL COMMENT '通知内容',
  `status` tinyint(4) NOT NULL COMMENT '0:无效,1:有效',
  `create_date` date NOT NULL,
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for `producer_stat`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `producer_stat` (
  `total_id` int(11) NOT NULL COMMENT 'producer_total_stat id',
  `broker` varchar(20) NOT NULL COMMENT 'broker',
  `max` int(11) NOT NULL COMMENT '最大耗时',
  `avg` double NOT NULL COMMENT '平均耗时',
  `count` int(11) NOT NULL COMMENT '调用次数',
  `exception` text COMMENT '异常记录'
);

-- ----------------------------
-- Table structure for `producer_total_stat`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `producer_total_stat` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `producer` varchar(255) NOT NULL COMMENT 'producer',
  `client` varchar(100) NOT NULL COMMENT 'client',
  `ip` varchar(100) NOT NULL COMMENT 'ip',
  `percent90` int(11) NOT NULL COMMENT '耗时百分位90',
  `percent99` int(11) NOT NULL COMMENT '耗时百分位99',
  `avg` double NOT NULL COMMENT '平均耗时',
  `count` int(11) NOT NULL COMMENT '调用次数',
  `create_date` int(11) NOT NULL COMMENT '创建日期',
  `create_time` char(4) NOT NULL COMMENT '创建分钟,格式:HHMM',
  `stat_time` int(11) NOT NULL COMMENT '统计时间',
  `exception` text COMMENT '异常记录',
  PRIMARY KEY (`id`),
  UNIQUE KEY `producer` (`producer`,`stat_time`,`client`)
);

-- ----------------------------
-- Table structure for `server`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `server` (
  `ip` varchar(16) NOT NULL COMMENT 'ip',
  `machine_type` int(4) DEFAULT NULL COMMENT '机器类型：0-未知，1-物理机，2-虚拟机，3-docker',
  `host` varchar(255) DEFAULT NULL COMMENT 'host',
  `nmon` varchar(255) DEFAULT NULL COMMENT 'nmon version',
  `cpus` tinyint(4) DEFAULT NULL COMMENT 'logic cpu num',
  `cpu_model` varchar(255) DEFAULT NULL COMMENT 'cpu 型号',
  `dist` varchar(255) DEFAULT NULL COMMENT '发行版信息',
  `kernel` varchar(255) DEFAULT NULL COMMENT '内核信息',
  `ulimit` varchar(255) DEFAULT NULL COMMENT 'ulimit -n,ulimit -u',
  `updatetime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `room` varchar(255) DEFAULT NULL COMMENT '机房',
  PRIMARY KEY (`ip`)
);

-- ----------------------------
-- Table structure for `server_stat`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `server_stat` (
  `ip` varchar(16) NOT NULL COMMENT 'ip',
  `cdate` date NOT NULL COMMENT '数据收集天',
  `ctime` char(4) NOT NULL COMMENT '数据收集小时分钟',
  `cuser` float DEFAULT NULL COMMENT '用户态占比',
  `csys` float DEFAULT NULL COMMENT '内核态占比',
  `cwio` float DEFAULT NULL COMMENT 'wio占比',
  `c_ext` text COMMENT '子cpu占比',
  `cload1` float DEFAULT NULL COMMENT '1分钟load',
  `cload5` float DEFAULT NULL COMMENT '5分钟load',
  `cload15` float DEFAULT NULL COMMENT '15分钟load',
  `mtotal` float DEFAULT NULL COMMENT '总内存,单位M',
  `mfree` float DEFAULT NULL COMMENT '空闲内存',
  `mcache` float DEFAULT NULL COMMENT 'cache',
  `mbuffer` float DEFAULT NULL COMMENT 'buffer',
  `mswap` float DEFAULT NULL COMMENT 'cache',
  `mswap_free` float DEFAULT NULL COMMENT 'cache',
  `nin` float DEFAULT NULL COMMENT '网络入流量 单位K/s',
  `nout` float DEFAULT NULL COMMENT '网络出流量 单位k/s',
  `nin_ext` text COMMENT '各网卡入流量详情',
  `nout_ext` text COMMENT '各网卡出流量详情',
  `tuse` int(11) DEFAULT NULL COMMENT 'tcp estab连接数',
  `torphan` int(11) DEFAULT NULL COMMENT 'tcp orphan连接数',
  `twait` int(11) DEFAULT NULL COMMENT 'tcp time wait连接数',
  `dread` float DEFAULT NULL COMMENT '磁盘读速率 单位K/s',
  `dwrite` float DEFAULT NULL COMMENT '磁盘写速率 单位K/s',
  `diops` float DEFAULT NULL COMMENT '磁盘io速率 交互次数/s',
  `dbusy` float DEFAULT NULL COMMENT '磁盘io带宽使用百分比',
  `d_ext` text COMMENT '磁盘各分区占比',
  `dspace` text COMMENT '磁盘各分区空间使用率',
  PRIMARY KEY (`ip`,`cdate`,`ctime`)
);

-- ----------------------------
-- Table structure for `shedlock`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `shedlock` (
  `name` varchar(64) NOT NULL DEFAULT '',
  `lock_until` timestamp(3) NULL DEFAULT NULL,
  `locked_at` timestamp(3) NULL DEFAULT NULL,
  `locked_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
);

-- ----------------------------
-- Table structure for `topic`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `topic` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) NOT NULL COMMENT 'cluster id',
  `name` varchar(64) NOT NULL COMMENT 'topic名',
  `queue_num` int(11) NOT NULL COMMENT '队列长度',
  `ordered` int(4) NOT NULL DEFAULT '0' COMMENT '0:无序,1:有序',
  `count` int(11) DEFAULT NULL COMMENT 'topic put times',
  `size` bigint(20) DEFAULT '0' COMMENT 'topic put size',
  `info` varchar(360) DEFAULT NULL COMMENT 'topic描述',
  `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace',
  `msg_type` int(4) NOT NULL DEFAULT '0' COMMENT '消息类型，0:普通消息，1:延迟消息，2:定时消息',
  `create_date` date NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `serializer` int(4) NOT NULL DEFAULT '0' COMMENT '序列化器 0:Protobuf,1:String',
  `traffic_warn_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启流量突增预警,1:开启流量突增预警',
  `effective` int(4) NOT NULL DEFAULT '0' COMMENT '状态确认 0 未确认 1 确认',
  `size_1d` bigint(20) DEFAULT '0' COMMENT 'topic put size in one day',
  `size_2d` bigint(20) DEFAULT '0' COMMENT 'topic put size in two days',
  `size_3d` bigint(20) DEFAULT '0' COMMENT 'topic put size in three days',
  `size_5d` bigint(20) DEFAULT '0' COMMENT 'topic put size in five days',
  `size_7d` bigint(20) DEFAULT '0' COMMENT 'topic put size in seven days',
  `count_1d` bigint(20) DEFAULT '0' COMMENT 'topic put count in one day',
  `count_2d` bigint(20) DEFAULT '0' COMMENT 'topic put count in two days',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
);

-- ----------------------------
-- Table structure for `topic_traffic`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `topic_traffic` (
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `create_date` date NOT NULL COMMENT '数据收集天',
  `create_time` char(4) NOT NULL COMMENT '数据收集小时分钟,格式:HHMM',
  `count` bigint(20) DEFAULT NULL COMMENT 'topic put times',
  `size` bigint(20) DEFAULT NULL COMMENT 'topic put size',
  PRIMARY KEY (`tid`,`create_date`,`create_time`)
);

-- ----------------------------
-- Table structure for `user`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL COMMENT '用户名',
  `email` varchar(64) NOT NULL COMMENT '邮箱',
  `mobile` varchar(16) DEFAULT NULL COMMENT '手机',
  `type` int(4) NOT NULL DEFAULT '0' COMMENT '0:普通用户,1:管理员',
  `create_date` date NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `receive_notice` int(4) NOT NULL DEFAULT '0' COMMENT '管理员是否接收邮件通知,0:不接收,1:接收',
  `password` varchar(256) COMMENT '登录方式采用用户名密码验证时使用',
  `receive_phone_notice` int(4) NOT NULL DEFAULT '0' COMMENT '用户是否接收手机通知,0:不接收,1:接收',
  `gid` int(11) NOT NULL DEFAULT '0' COMMENT '组id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
);

-- ----------------------------
-- Table structure for `user_consumer`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_consumer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `consumer_id` int(11) DEFAULT NULL COMMENT 'consumer id',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for `user_message`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户id',
  `message` varchar(512) NOT NULL COMMENT '消息内容',
  `status` tinyint(4) NOT NULL COMMENT '0:未读,1:已读',
  `create_date` datetime NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for `user_producer`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_producer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `producer` varchar(64) NOT NULL COMMENT 'producer名',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for `warn_config`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `warn_config` (
  `consumer` varchar(64) DEFAULT '' COMMENT 'consumer名，为空时代表默认（仅一条默认记录）',
  `accumulate_time` int(11) DEFAULT '300000' COMMENT '堆积时间',
  `accumulate_count` int(11) DEFAULT '10000' COMMENT '堆积数量',
  `block_time` int(11) DEFAULT '10000' COMMENT '阻塞时间',
  `consumer_fail_count` int(11) DEFAULT '10' COMMENT '消费失败数量',
  `warn_unit_time` int(4) DEFAULT '1' COMMENT '报警频率的单位时间，单位小时',
  `warn_unit_count` int(4) DEFAULT '2' COMMENT '报警频率在单位时间的次数',
  `ignore_warn` int(4) DEFAULT '0' COMMENT '0:接收所有报警,1:不接收所有报警，此字段优先级最高',
  PRIMARY KEY (`consumer`)
);

-- ----------------------------
-- Table structure for `name_server`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `name_server` (
  `cid` int(11) NOT NULL COMMENT '集群id',
  `addr` varchar(255) NOT NULL COMMENT 'name server 地址',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `check_status` tinyint(4) DEFAULT 0 COMMENT '检测结果:0:未知,1:正常,2:异常',
  `check_time` datetime COMMENT '检测时间',
  `base_dir` varchar(360) DEFAULT '/opt/mqcloud/ns' COMMENT '安装路径',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态:0:正常,1:流量剔除',
  UNIQUE KEY `ns_cid` (`cid`,`addr`)
);

-- ----------------------------
-- Table structure for `server_warn_config`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `server_warn_config` (
  `ip` varchar(15) NOT NULL COMMENT 'ip',
  `memory_usage_rate` int(4) NOT NULL DEFAULT '0' COMMENT '内存使用率',
  `load1` int(4) NOT NULL DEFAULT '0' COMMENT '一分钟load',
  `connect` int(4) NOT NULL DEFAULT '0' COMMENT 'tcp连接数',
  `wait` int(4) NOT NULL DEFAULT '0' COMMENT 'tcp等待数',
  `iops` int(4) NOT NULL DEFAULT '0' COMMENT '磁盘io速率 交互次数/s',
  `iobusy` int(4) NOT NULL DEFAULT '0' COMMENT '磁盘io带宽使用百分比',
  `cpu_usage_rate` int(4) NOT NULL DEFAULT '0' COMMENT 'cpu使用率',
  `net_in` int(4) NOT NULL DEFAULT '0' COMMENT '入网流量',
  `net_out` int(4) NOT NULL DEFAULT '0' COMMENT '出网流量',
  `io_usage_rate` int(4) NOT NULL DEFAULT '0' COMMENT '磁盘使用率',
  PRIMARY KEY (`ip`)
);

-- ----------------------------
-- Table structure for `broker`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `broker` (
  `cid` int(11) NOT NULL COMMENT '集群id',
  `addr` varchar(255) NOT NULL COMMENT 'broker 地址',
  `broker_name` varchar(64) NOT NULL COMMENT 'broker名字',
  `broker_id` int(4) NOT NULL COMMENT 'broker ID，0-master，1-slave',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `check_status` tinyint(4) DEFAULT 0 COMMENT '检测结果:0:未知,1:正常,2:异常',
  `check_time` datetime COMMENT '检测时间',
  `base_dir` varchar(360) DEFAULT NULL COMMENT '安装路径',
    `writable` int(4) NOT NULL DEFAULT '1' COMMENT '0:不可写入,1:可写入',
    `size_1d` bigint(20) DEFAULT '0' COMMENT 'put size in one day',
    `size_2d` bigint(20) DEFAULT '0' COMMENT 'put size in two days',
    `size_3d` bigint(20) DEFAULT '0' COMMENT 'put size in three days',
    `size_5d` bigint(20) DEFAULT '0' COMMENT 'put size in five days',
    `size_7d` bigint(20) DEFAULT '0' COMMENT 'put size in seven days',
    `version` varchar(64) NOT NULL DEFAULT '5' COMMENT 'broker版本:4,5',
    UNIQUE KEY `cid` (`cid`,`addr`)
);

-- ----------------------------
-- Table structure for `consumer_client_stat`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `consumer_client_stat` (
  `consumer` varchar(255) NOT NULL COMMENT 'consumer',
  `client` varchar(20) NOT NULL COMMENT 'client',
  `create_date` date NOT NULL COMMENT '创建日期'
);

-- ----------------------------
-- Table structure for `audit_batch_associate`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_batch_associate` (
  `uids` text NOT NULL COMMENT '关联的用户id',
  `aid` int(11) NOT NULL COMMENT '审核id',
  `producer_ids` text NULL COMMENT '生产者id',
  `consumer_ids` text NULL COMMENT '消费者id',
  PRIMARY KEY (`aid`)
);

-- ----------------------------
-- Table structure for `broker_store_stat`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `broker_store_stat` (
  `cluster_id` int(11) NOT NULL COMMENT 'cluster_id',
  `broker_ip` varchar(255) NOT NULL COMMENT 'broker ip',
  `percent90` int(11) NOT NULL COMMENT '耗时百分位90',
  `percent99` int(11) NOT NULL COMMENT '耗时百分位99',
  `avg` double NOT NULL COMMENT '平均耗时',
  `max` int(11) NOT NULL COMMENT '最大耗时',
  `count` bigint(20) NOT NULL COMMENT '调用次数',
  `create_date` int(11) NOT NULL COMMENT '创建日期',
  `create_time` char(4) NOT NULL COMMENT '创建分钟,格式:HHMM',
  `stat_time` int(11) NOT NULL COMMENT '统计时间',
  PRIMARY KEY (`create_date`,`broker_ip`,`create_time`, `cluster_id`)
);

-- ----------------------------
-- Table structure for `cluster_config`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `cluster_config` (
  `cid` int(11) NOT NULL COMMENT '集群id',
  `bid` int(11) NOT NULL COMMENT 'broker config id',
  `online_value` varchar(256) COMMENT '线上值',
  UNIQUE KEY `cid_key` (`cid`,`bid`)
);

-- ----------------------------
-- Table structure for `broker_config_group`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `broker_config_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group` varchar(255) NOT NULL COMMENT '配置组',
  `order` int(11) NOT NULL COMMENT '序号小排前',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_key`  (`group`)
);

-- ----------------------------
-- Table structure for `broker_config`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `broker_config` (
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
);

-- ----------------------------
-- Table structure for `audit_consumer_config`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_consumer_config` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `consumer_id` int(11) DEFAULT NULL COMMENT 'consumer id',
  `permits_per_second` float DEFAULT NULL COMMENT 'qps',
  `enable_rate_limit` tinyint(4) DEFAULT NULL COMMENT '0:不限速,1:限速',
  `pause` tinyint(4) DEFAULT NULL COMMENT '0:不暂停,1:暂停',
  `pause_client_id` varchar(255) DEFAULT NULL COMMENT '暂停的客户端Id'
);

-- ----------------------------
-- Table structure for `consumer_config`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `consumer_config` (
  `consumer` varchar(64) NOT NULL COMMENT 'consumer名',
  `retry_message_reset_to` bigint(20) DEFAULT NULL COMMENT '重置至时间戳，小于此时间的都将不再消息',
  `permits_per_second` float DEFAULT NULL COMMENT 'qps',
  `enable_rate_limit` tinyint(4) DEFAULT NULL COMMENT '0:不限速,1:限速',
  `pause` tinyint(4) DEFAULT NULL COMMENT '0:不暂停,1:暂停',
  `pause_client_id` varchar(255) DEFAULT NULL COMMENT '暂停的客户端Id',
  `retry_message_skip_key` varchar(360) DEFAULT NULL COMMENT '消息key',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `consumer` (`consumer`)
);

-- ----------------------------
-- Table structure for `topic_traffic_stat`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `topic_traffic_stat` (
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `avg_max` bigint(20) NOT NULL COMMENT '指定天数内,每天流量最大值的平均值',
  `max_max` bigint(20) NOT NULL COMMENT '指定天数内,去除异常点后流量的最大值',
  `days` int(4) NOT NULL COMMENT '指定统计流量的天数',
  `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tid`)
);

-- ----------------------------
-- Table structure for `topic_traffic_warn_config`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `topic_traffic_warn_config` (
  `min_warn_count` bigint(20) DEFAULT NULL COMMENT '最小预警数量',
  `avg_multiplier` float DEFAULT '5' COMMENT '平均流量值的乘数阈值;流量统计时，大于该值乘以平均流量值认定为异常值而被剔除',
  `avg_max_percentage_increase` float DEFAULT '200' COMMENT '30天内每天流量最大值的平均值的百分比阈值；某时刻流量值大于最大值的平均值的增长阈值，则预警',
  `max_max_percentage_increase` float DEFAULT '30' COMMENT '30天内流量最大值的增幅百分比阈值；某时刻流量值若大于最大值的该增幅阈值，则预警',
  `alarm_receiver` int(4) DEFAULT '0' COMMENT '告警接收人,0:生产者消费者及管理员,1:生产者和管理员,2:消费者和管理员,3:仅管理员,4:不告警',
  `topic` varchar(64) DEFAULT '' COMMENT 'topic名称，为空代表默认配置，只有一条默认配置',
  UNIQUE KEY `topic` (`topic`)
);

-- ----------------------------
-- Table structure for `audit_topic_traffic_warn`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `audit_topic_traffic_warn` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `traffic_warn_enabled` int(11) NOT NULL COMMENT '0:不开启topic流量突增预警,1:开启topic流量突增预警'
);

-- ----------------------------
-- Table structure for `user_warn`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_warn` (
  `uid` int(11) NOT NULL,
  `type` int(4) NOT NULL COMMENT '警告类型',
  `resource` varchar(100) DEFAULT NULL COMMENT '警告资源:topic,producer等',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `wid` int(11) NOT NULL
);

-- ----------------------------
-- Table structure for `warn_info`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `warn_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` text DEFAULT NULL COMMENT '警告内容',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for `user_group`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'group id',
  `name` varchar(64) NOT NULL COMMENT 'name',
  `create_date` date NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `gname` (`name`)
);

-- ----------------------------
-- Table structure for `user_footprint`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_footprint` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `uid` int(11) NOT NULL COMMENT 'user id',
    `tid` int(11) NOT NULL COMMENT 'topic id',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ft_utid` (`uid`, `tid`)
);

-- ----------------------------
-- Table structure for `user_favorite`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_favorite` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `uid` int(11) NOT NULL COMMENT 'user id',
    `tid` int(11) NOT NULL COMMENT 'topic id',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `fa_utid` (`uid`, `tid`)
);

alter table IF EXISTS  `topic` add column IF NOT EXISTS `effective` int(4) NOT NULL DEFAULT '0' COMMENT 'topic状态确认 0 未确认 1 确认';

-- ----------------------------
-- Table structure for `client_language`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `client_language` (
    `cid` int(11) NOT NULL COMMENT 'cluster id',
    `tid` int(11) NOT NULL COMMENT 'topic id',
    `client_group_name` varchar(255) NOT NULL COMMENT '生产者/消费者名称',
    `client_group_type` tinyint(4) NOT NULL COMMENT '类型 0 生产者 1 消费者',
    `language` tinyint(4) DEFAULT NULL COMMENT '客户端语言',
    `version` varchar(50) DEFAULT NULL COMMENT '版本',
    `relation_uids` varchar(255) DEFAULT NULL COMMENT '关联人员id集合，逗号分隔',
    `modify_type` int(4) NOT NULL DEFAULT '0' COMMENT '修改方式 0 自动 1 手动',
    `create_date` date NOT NULL COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `union_key` (`cid`,`tid`,`client_group_name`),
    KEY `nomal_query_index` (`client_group_name`,`client_group_type`,`language`) USING BTREE
);

alter table IF EXISTS `consumer` add column IF NOT EXISTS `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table IF EXISTS `audit_consumer` add column IF NOT EXISTS `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table IF EXISTS `audit_topic` add column IF NOT EXISTS `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table IF EXISTS `user_producer` add column IF NOT EXISTS `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table IF EXISTS `audit_consumer_config` add column IF NOT EXISTS `unregister` tinyint(4) DEFAULT NULL COMMENT '0:不解注册,1:解注册';
alter table IF EXISTS `consumer_config` add column IF NOT EXISTS `unregister` tinyint(4) DEFAULT NULL COMMENT '0:不解注册,1:解注册';
alter table IF EXISTS `broker` add column IF NOT EXISTS `writable` int(4) NOT NULL DEFAULT '1' COMMENT '0:不可写入,1:可写入';
alter table IF EXISTS `audit_associate_producer` add column IF NOT EXISTS `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';

CREATE TABLE IF NOT EXISTS `controller` (
    `cid`          int(11) NOT NULL COMMENT '集群id',
    `addr`         varchar(255) NOT NULL COMMENT 'controller 地址',
    `create_time`  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `check_status` tinyint(4) DEFAULT 0 COMMENT '检测结果:0:未知,1:正常,2:异常',
    `check_time`   datetime COMMENT '检测时间',
    `base_dir`     varchar(360) DEFAULT '/opt/mqcloud/controller' COMMENT '安装路径',
    UNIQUE KEY `c_cid` (`cid`, `addr`)
);

CREATE TABLE IF NOT EXISTS `proxy` (
    `cid`          int(11) NOT NULL COMMENT '集群id',
    `addr`         varchar(255) NOT NULL COMMENT 'proxy grpc 地址',
    `create_time`  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `status`       tinyint(4) default 0 comment '状态:0:正常,1:流量剔除',
    `check_status` tinyint(4) DEFAULT 0 COMMENT '检测结果:0:未知,1:正常,2:异常',
    `check_time`   datetime COMMENT '检测时间',
    `base_dir`     varchar(360) DEFAULT '/opt/mqcloud/proxy' COMMENT '安装路径',
    `config`       text COMMENT '配置',
    UNIQUE KEY `p_cid` (`cid`, `addr`)
);

create table IF NOT EXISTS `audit_wheel_message_cancel`
(
    `id`          int(11)       not null AUTO_INCREMENT,
    `uid`         int(11)       not null COMMENT '申请用户ID',
    `aid`         int(11)       not null COMMENT '审核记录ID',
    `tid`         int(11)       not null COMMENT 'topic ID',
    `uniqueId`    varchar(50)   not null COMMENT '待取消消息uniqId',
    `brokerName`  varchar(50)   not null COMMENT '待取消消息所在的brokerName',
    `deliverTime` bigint        not null COMMENT '定时时间',
    `status`      tinyint(4)    default 0 comment '状态 0失败 1成功 ',
    `createTime`  datetime      null,
    PRIMARY KEY (`id`)
);

create table IF NOT EXISTS `cancel_uniqid`
(
    `tid`          int(11)     not null COMMENT 'topic id',
    `uniqueId`     varchar(50) not null COMMENT '取消消息uniqueId',
    `createTime`   datetime    not null COMMENT '生成时间',
    UNIQUE KEY  uniqIndex (uniqueId)
);

CREATE TABLE IF NOT EXISTS `audit_timespan_message_export` (
    `aid` int(11) NOT NULL COMMENT '审核id',
    `topic` varchar(64) NOT NULL COMMENT 'topic',
    `start` bigint(20) DEFAULT NULL COMMENT '消费开始时间戳',
    `end` bigint(20) DEFAULT NULL COMMENT '消费结束时间戳'
);

CREATE TABLE IF NOT EXISTS `message_export`
(
    `aid`   int(11) NOT NULL COMMENT '审核id',
    `ip` varchar(255) NOT NULL COMMENT '执行导出的机器ip',
    `total_msg_count` bigint(20) COMMENT '总消息量',
    `exported_msg_count` bigint(20) COMMENT '导出的消息量',
    `left_time` bigint(20) COMMENT '剩余时间',
    `export_cost_time` bigint(20) COMMENT '导出用时',
    `compress_cost_time` bigint(20) COMMENT '压缩用时',
    `scp_cost_time` bigint(20) COMMENT 'scp用时',
    `exported_file_path` varchar(255) COMMENT '导出文件路径',
    `info` text COMMENT '任务执行的信息',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    primary key (`aid`)
);

CREATE TABLE IF NOT EXISTS `consumer_client_metrics`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT,
    `consumer`    varchar(64)  NOT NULL COMMENT 'consumer',
    `client`      varchar(100) NOT NULL COMMENT 'client',
    `max`         int(11) NOT NULL COMMENT '最大耗时',
    `avg`         double       NOT NULL COMMENT '平均耗时',
    `count`       int(11) NOT NULL COMMENT '调用次数',
    `exception`   text COMMENT '异常记录',
    `create_date` int(11) NOT NULL COMMENT '创建日期',
    `create_time` char(4)      NOT NULL COMMENT '创建分钟,格式:HHMM',
    `stat_time`   int(11) NOT NULL COMMENT '统计时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `s_s_c` (`consumer`,`stat_time`,`client`),
    KEY           `create_date` (`create_date`,`consumer`)
);

CREATE TABLE IF NOT EXISTS `consumer_pause_config` (
    `consumer`        varchar(64) NOT NULL COMMENT 'consumer名',
    `pause_client_id` varchar(255)         DEFAULT NULL COMMENT '暂停的客户端Id',
    `unregister`      tinyint(4) DEFAULT NULL COMMENT '0:不解注册,1:解注册',
    `update_time`     timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `c_p_c` (`consumer`, `pause_client_id`)
);

CREATE TABLE IF NOT EXISTS `data_migration`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT,
    `source_ip`     varchar(255) NOT NULL COMMENT '源ip',
    `source_path`   varchar(255) NOT NULL COMMENT '源路径',
    `dest_ip`       varchar(255) NOT NULL COMMENT '目标ip',
    `dest_path`     varchar(255) NOT NULL COMMENT '目标路径',
    `data_count`  bigint(20) NOT NULL COMMENT '数据量',
    `cost_time`   bigint(20) DEFAULT '0' COMMENT '耗时',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status`      tinyint(4) NOT NULL DEFAULT '0' COMMENT '0:未开始,1:进行中,2:完成',
    `info`        text COMMENT '执行的信息',
    primary key (`id`)
);

CREATE TABLE IF NOT EXISTS `broker_tmp`
(
    `cid`         int(11) NOT NULL COMMENT '集群id',
    `addr`        varchar(255) NOT NULL COMMENT 'broker 地址',
    `broker_name` varchar(64)  NOT NULL COMMENT 'broker名字',
    `broker_id`   int(4) NOT NULL COMMENT 'broker ID，0-master，1-slave',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `base_dir`    varchar(360)          DEFAULT NULL COMMENT '安装路径',
    UNIQUE KEY `b_t_idx` (`cid`,`addr`)
);

CREATE TABLE IF NOT EXISTS `audit_http_consumer_config`
(
    `aid`             int(11) NOT NULL COMMENT '审核id',
    `consumer_id`     int(11) NOT NULL COMMENT 'consumer id',
    `pull_size`       int(11) DEFAULT NULL COMMENT '拉取消息量',
    `pull_timeout`    int(11) DEFAULT NULL COMMENT '拉取超时时间，单位毫秒',
    `consume_timeout` int(11) DEFAULT NULL COMMENT '消费超时时间，单位毫秒',
    PRIMARY KEY (`aid`)
);

CREATE TABLE IF NOT EXISTS `topic_warn_config`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `tid`           int(11) NOT NULL COMMENT 'topic id',
    `operand_type`  tinyint(4) DEFAULT 0 COMMENT '操作类型，0:5分钟生产条数;1:一小时生产条数;2:一天生产条数;3:5分钟环比;4:时环比;5:日环比',
    `operator_type` tinyint(4) DEFAULT 0 COMMENT '比较符类型，0:大于;1:小于;2:大于等于;3:小于等于',
    `threshold`     double DEFAULT 0 COMMENT '阈值',
    `warn_interval` int(11) DEFAULT 0 COMMENT '报警间隔，单位分钟',
    `warn_time`     varchar(64) COMMENT '预警时间，格式：HH:mm-HH:mm',
    `enabled`       int(4) NOT NULL DEFAULT '1' COMMENT '0:未启用,1:启用',
    PRIMARY KEY (`id`),
    KEY `tid_idx` (`tid`)
);

CREATE TABLE IF NOT EXISTS `broker_auto_update`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `cid`         int(11) NOT NULL COMMENT 'cluster id',
    `status`      tinyint(4) NOT NULL DEFAULT '0' COMMENT '1:未开始,2:已就绪,3:进行中,4:暂停中,5:成功,6:失败,7:手动结束',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `start_time`  timestamp NULL DEFAULT NULL COMMENT '开始时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `broker_auto_update_step`
(
    `id`                    int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `broker_auto_update_id` int(11) NOT NULL COMMENT 'broker_auto_update id',
    `broker_addr`           varchar(255) NOT NULL COMMENT 'broker 地址',
    `broker_name`           varchar(64)  NOT NULL COMMENT 'broker名字',
    `broker_id`             int(4) NOT NULL COMMENT 'broker ID，0-master，1-slave',
    `broker_base_dir`       varchar(360) NOT NULL COMMENT 'broker安装路径',
    `broker_version`        varchar(64)  NOT NULL COMMENT 'broker版本：4, 5',
    `order`                 int(11) NOT NULL COMMENT '操作顺序',
    `action`                tinyint(4) NOT NULL DEFAULT '0' COMMENT '0:停写,1:取消注册,2:关闭,3:备份数据,4:下载安装包,5:解压安装包,6:恢复数据,7:启动,8:注册,9:恢复写入',
    `status`                tinyint(4) NOT NULL DEFAULT '0' COMMENT '1:未开始,2:进行中,3:成功,4:失败,5:手动结束',
    `info`                  text COMMENT '操作信息',
    `start_time`            timestamp NULL DEFAULT NULL COMMENT '开始时间',
    `end_time`              timestamp NULL DEFAULT NULL COMMENT '结束时间',
    PRIMARY KEY (`id`)
);