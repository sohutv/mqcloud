-- ----------------------------
-- DATABASE structure for `mq-cloud`
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `mq-cloud` DEFAULT CHARACTER SET utf8;

use `mq-cloud`;
-- ----------------------------
-- Table structure for `audit`
-- ----------------------------
DROP TABLE IF EXISTS `audit`;
CREATE TABLE `audit` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核主表';

-- ----------------------------
-- Table structure for `audit_associate_consumer`
-- ----------------------------
DROP TABLE IF EXISTS `audit_associate_consumer`;
CREATE TABLE `audit_associate_consumer` (
  `uid` int(11) NOT NULL COMMENT '关联的用户ID',
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `cid` int(11) NOT NULL COMMENT 'consumer id',
  PRIMARY KEY (`aid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核关联消费者相关表';

-- ----------------------------
-- Table structure for `audit_associate_producer`
-- ----------------------------
DROP TABLE IF EXISTS `audit_associate_producer`;
CREATE TABLE `audit_associate_producer` (
  `uid` int(11) NOT NULL COMMENT '关联的用户ID',
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `producer` varchar(64) NOT NULL COMMENT '关联的生产者名字',
  PRIMARY KEY (`aid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核关联生产者相关表';

-- ----------------------------
-- Table structure for `audit_consumer`
-- ----------------------------
DROP TABLE IF EXISTS `audit_consumer`;
CREATE TABLE `audit_consumer` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `consumer` varchar(64) NOT NULL COMMENT '消费者名字',
  `consume_way` int(4) NOT NULL DEFAULT '0' COMMENT '0:集群消费,1:广播消费',
  `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace',
  UNIQUE KEY `tid` (`tid`,`consumer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核消费者相关表';

-- ----------------------------
-- Table structure for `audit_consumer_delete`
-- ----------------------------
DROP TABLE IF EXISTS `audit_consumer_delete`;
CREATE TABLE `audit_consumer_delete` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `cid` int(11) NOT NULL COMMENT 'consumer id',
  `consumer` varchar(64) DEFAULT NULL COMMENT 'consumer名字',
  `topic` varchar(64) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核消费者删除相关表';

-- ----------------------------
-- Table structure for `audit_producer_delete`
-- ----------------------------
DROP TABLE IF EXISTS `audit_producer_delete`;
CREATE TABLE `audit_producer_delete` (
  `uid` int(11) NOT NULL,
  `aid` int(11) NOT NULL COMMENT '审核id',
  `pid` int(11) NOT NULL COMMENT 'userProducer id',
  `producer` varchar(64) DEFAULT NULL,
  `topic` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`aid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核用户与生产者组关系删除相关表';

-- ----------------------------
-- Table structure for `audit_reset_offset`
-- ----------------------------
DROP TABLE IF EXISTS `audit_reset_offset`;
CREATE TABLE `audit_reset_offset` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `consumer_id` int(11) DEFAULT NULL COMMENT 'consumer id',
  `offset` varchar(64) DEFAULT NULL COMMENT 'null:重置为最大offset,时间戳:重置为某时间点(yyyy-MM-dd#HH:mm:ss:SSS)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核offset相关表';

-- ----------------------------
-- Table structure for `audit_topic`
-- ----------------------------
DROP TABLE IF EXISTS `audit_topic`;
CREATE TABLE `audit_topic` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `name` varchar(64) NOT NULL COMMENT 'topic名',
  `queue_num` int(11) NOT NULL COMMENT '队列长度',
  `producer` varchar(64) NOT NULL COMMENT '生产者名字',
  `ordered` int(4) NOT NULL DEFAULT '0' COMMENT '0:无序,1:有序',
  `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace',
  `transaction_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启事务,1:开启事务',
  `delay_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不发送延迟消息,1:发送延迟消息。注：此字段不强制该topic的消息类型',
  `test_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:非测试topic,1:测试topic',
  `info` varchar(360) DEFAULT NULL COMMENT 'topic描述',
  `qps` int(11) DEFAULT NULL COMMENT '消息量qps预估',
  `qpd` int(11) DEFAULT NULL COMMENT '一天消息量预估',
  `serializer` int(4) NOT NULL DEFAULT '0' COMMENT '序列化器 0:Protobuf,1:String'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核topic相关表';

-- ----------------------------
-- Table structure for `audit_topic_delete`
-- ----------------------------
DROP TABLE IF EXISTS `audit_topic_delete`;
CREATE TABLE `audit_topic_delete` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `topic` varchar(64) DEFAULT NULL COMMENT 'topic名字'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核topic删除相关表';

-- ----------------------------
-- Table structure for `audit_topic_update`
-- ----------------------------
DROP TABLE IF EXISTS `audit_topic_update`;
CREATE TABLE `audit_topic_update` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `queue_num` int(11) NOT NULL COMMENT '队列长度'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核topic更新相关表';

-- ----------------------------
-- Table structure for `audit_user_consumer_delete`
-- ----------------------------
DROP TABLE IF EXISTS `audit_user_consumer_delete`;
CREATE TABLE `audit_user_consumer_delete` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `uid` int(11) NOT NULL COMMENT '此消费者对应的用户',
  `ucid` int(11) NOT NULL COMMENT 'user_consumer id',
  `consumer` varchar(64) DEFAULT NULL,
  `topic` varchar(64) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核用户与消费者组关系删除相关表';

-- ----------------------------
-- Table structure for `audit_resend_message`
-- ----------------------------
DROP TABLE IF EXISTS `audit_resend_message`;
CREATE TABLE `audit_resend_message` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `msgId` char(32) NOT NULL COMMENT 'broker offset msg id',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '申请类型:0:未处理,1:发送成功,2:发送失败',
  `times` int(11) NOT NULL DEFAULT '0' COMMENT '发送次数',
  `send_time` datetime COMMENT '发送时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息重发审核表';

-- ----------------------------
-- Table structure for `audit_resend_message_consumer`
-- ----------------------------
CREATE TABLE `audit_resend_message_consumer` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `consumer_id` int(11) NOT NULL COMMENT 'consumer id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息重发给消费者审核表';

-- ----------------------------
-- Table structure for `audit_topic_trace`
-- ----------------------------
CREATE TABLE `audit_topic_trace` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `trace_enabled` int(11) NOT NULL COMMENT '0:不开启trece,1:开启trace'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核topic trace相关表';

-- ----------------------------
-- Table structure for `broker_traffic`
-- ----------------------------
DROP TABLE IF EXISTS `broker_traffic`;
CREATE TABLE `broker_traffic` (
  `ip` varchar(16) NOT NULL COMMENT 'ip',
  `create_date` date NOT NULL COMMENT '数据收集天',
  `create_time` char(4) NOT NULL COMMENT '数据收集小时分钟,格式:HHMM',
  `cluster_id` int(11) NOT NULL COMMENT 'cluster_id',
  `put_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '生产消息量',
  `put_size` bigint(20) NOT NULL DEFAULT '0' COMMENT '生产消息大小',
  `get_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '消费消息量',
  `get_size` bigint(20) NOT NULL DEFAULT '0' COMMENT '消费消息大小',
  PRIMARY KEY (`ip`,`create_date`,`create_time`),
  KEY `time` (`create_date`,`cluster_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='broker流量表';

-- ----------------------------
-- Table structure for `client_version`
-- ----------------------------
DROP TABLE IF EXISTS `client_version`;
CREATE TABLE `client_version` (
  `topic` varchar(255) NOT NULL,
  `client` varchar(255) NOT NULL,
  `role` tinyint(4) NOT NULL COMMENT '1:producer,2:consumer',
  `version` varchar(255) NOT NULL,
  `create_date` date NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `topic` (`topic`,`client`,`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='topic客户端版本';

-- ----------------------------
-- Table structure for `cluster`
-- ----------------------------
DROP TABLE IF EXISTS `cluster`;
CREATE TABLE `cluster` (
  `id` int(11) NOT NULL COMMENT '集群id，也会作为ns发现的一部分',
  `name` varchar(64) NOT NULL COMMENT '集群名',
  `vip_channel_enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否开启vip通道, 1:开启, 0:关闭, rocketmq 4.x版本默认开启',
  `online` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否为线上集群, 1:是, 0:否, 线上集群会开启流量抓取',
  `transaction_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不支持事务,1:支持事务',
  `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不支持trace,1:支持trace',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='集群表';

-- ----------------------------
-- Table structure for `common_config`
-- ----------------------------
DROP TABLE IF EXISTS `common_config`;
CREATE TABLE `common_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(64) DEFAULT NULL COMMENT '配置key',
  `value` varchar(65535) NOT NULL COMMENT '配置值',
  `comment` varchar(256) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `consumer`
-- ----------------------------
DROP TABLE IF EXISTS `consumer`;
CREATE TABLE `consumer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `name` varchar(64) NOT NULL COMMENT 'consumer名',
  `consume_way` int(4) NOT NULL DEFAULT '0' COMMENT '0:集群消费,1:广播消费',
  `create_date` date NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace',
  `info` varchar(360) DEFAULT NULL COMMENT '消费者描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `tid` (`tid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消费者表';

-- ----------------------------
-- Table structure for `consumer_block`
-- ----------------------------
DROP TABLE IF EXISTS `consumer_block`;
CREATE TABLE `consumer_block` (
  `csid` int(11) DEFAULT NULL COMMENT 'consumer_stat id',
  `instance` varchar(255) DEFAULT NULL COMMENT 'consumer instance_id',
  `updatetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `broker` varchar(255) DEFAULT NULL COMMENT 'broker',
  `qid` int(11) DEFAULT NULL COMMENT 'qid',
  `block_time` bigint(20) DEFAULT NULL COMMENT '毫秒=当前时间-最新消费时间',
  `offset_moved_times` int(11) DEFAULT '0' COMMENT 'offset moved times',
  `offset_moved_time` bigint(20) DEFAULT NULL COMMENT 'offset moved msg store time',
  UNIQUE KEY `csid` (`csid`,`broker`,`qid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `consumer_stat`
-- ----------------------------
DROP TABLE IF EXISTS `consumer_stat`;
CREATE TABLE `consumer_stat` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `consumer_traffic`
-- ----------------------------
DROP TABLE IF EXISTS `consumer_traffic`;
CREATE TABLE `consumer_traffic` (
  `consumer_id` int(11) NOT NULL DEFAULT '0' COMMENT 'consumer id',
  `create_date` date NOT NULL COMMENT '数据收集天',
  `create_time` char(4) NOT NULL COMMENT '数据收集小时分钟,格式:HHMM',
  `count` bigint(20) DEFAULT NULL COMMENT 'consumer pull times',
  `size` bigint(20) DEFAULT NULL COMMENT 'consumer pull size',
  PRIMARY KEY (`consumer_id`,`create_date`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消费者流量表';

-- ----------------------------
-- Table structure for `feedback`
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户id',
  `content` text NOT NULL COMMENT '反馈内容',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='反馈表';

-- ----------------------------
-- Table structure for `need_warn_config`
-- ----------------------------
DROP TABLE IF EXISTS `need_warn_config`;
CREATE TABLE `need_warn_config` (
  `oKey` varchar(255) NOT NULL COMMENT '报警频率的key（type_topic_group）',
  `times` int(11) NOT NULL COMMENT '次数',
  `update_time` bigint(13) NOT NULL COMMENT '计时起始时间时间',
  UNIQUE KEY `key` (`oKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='报警频率表';

-- ----------------------------
-- Table structure for `notice`
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(512) NOT NULL COMMENT '通知内容',
  `status` tinyint(4) NOT NULL COMMENT '0:无效,1:有效',
  `create_date` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='通知表';

-- ----------------------------
-- Table structure for `producer_stat`
-- ----------------------------
DROP TABLE IF EXISTS `producer_stat`;
CREATE TABLE `producer_stat` (
  `total_id` int(11) NOT NULL COMMENT 'producer_total_stat id',
  `broker` varchar(20) NOT NULL COMMENT 'broker',
  `max` int(11) NOT NULL COMMENT '最大耗时',
  `avg` double NOT NULL COMMENT '平均耗时',
  `count` int(11) NOT NULL COMMENT '调用次数',
  `exception` text COMMENT '异常记录',
  KEY `total_id` (`total_id`,`broker`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='生产者统计';

-- ----------------------------
-- Table structure for `producer_total_stat`
-- ----------------------------
DROP TABLE IF EXISTS `producer_total_stat`;
CREATE TABLE `producer_total_stat` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `producer` varchar(255) NOT NULL COMMENT 'producer',
  `client` varchar(100) NOT NULL COMMENT 'client',
  `percent90` int(11) NOT NULL COMMENT '耗时百分位90',
  `percent99` int(11) NOT NULL COMMENT '耗时百分位99',
  `avg` double NOT NULL COMMENT '平均耗时',
  `count` int(11) NOT NULL COMMENT '调用次数',
  `create_date` int(11) NOT NULL COMMENT '创建日期',
  `create_time` char(4) NOT NULL COMMENT '创建分钟,格式:HHMM',
  `stat_time` int(11) NOT NULL COMMENT '统计时间',
  `exception` text COMMENT '异常记录',
  PRIMARY KEY (`id`),
  UNIQUE KEY `producer` (`producer`,`stat_time`,`client`),
  KEY `create_date` (`create_date`,`producer`),
  KEY `date_client` (`create_date`,`client`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='生产者总体统计';

-- ----------------------------
-- Table structure for `server`
-- ----------------------------
DROP TABLE IF EXISTS `server`;
CREATE TABLE `server` (
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
  PRIMARY KEY (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `server_stat`
-- ----------------------------
DROP TABLE IF EXISTS `server_stat`;
CREATE TABLE `server_stat` (
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
  PRIMARY KEY (`ip`,`cdate`,`ctime`),
  KEY `cdate` (`cdate`,`ctime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `shedlock`
-- ----------------------------
DROP TABLE IF EXISTS `shedlock`;
CREATE TABLE `shedlock` (
  `name` varchar(64) NOT NULL DEFAULT '',
  `lock_until` timestamp(3) NULL DEFAULT NULL,
  `locked_at` timestamp(3) NULL DEFAULT NULL,
  `locked_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `topic`
-- ----------------------------
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) NOT NULL COMMENT 'cluster id',
  `name` varchar(64) NOT NULL COMMENT 'topic名',
  `queue_num` int(11) NOT NULL COMMENT '队列长度',
  `ordered` int(4) NOT NULL DEFAULT '0' COMMENT '0:无序,1:有序',
  `count` int(11) DEFAULT NULL COMMENT 'topic put times',
  `info` varchar(360) DEFAULT NULL COMMENT 'topic描述',
  `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace',
  `delay_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不发送延迟消息,1:发送延迟消息。注：此字段不强制该topic的消息类型',
  `create_date` date NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `serializer` int(4) NOT NULL DEFAULT '0' COMMENT '序列化器 0:Protobuf,1:String',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='topic表';

-- ----------------------------
-- Table structure for `topic_traffic`
-- ----------------------------
DROP TABLE IF EXISTS `topic_traffic`;
CREATE TABLE `topic_traffic` (
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `create_date` date NOT NULL COMMENT '数据收集天',
  `create_time` char(4) NOT NULL COMMENT '数据收集小时分钟,格式:HHMM',
  `count` bigint(20) DEFAULT NULL COMMENT 'topic put times',
  `size` bigint(20) DEFAULT NULL COMMENT 'topic put size',
  PRIMARY KEY (`tid`,`create_date`,`create_time`),
  KEY `time` (`create_date`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='topic流量表';

-- ----------------------------
-- Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL COMMENT '用户名',
  `email` varchar(64) NOT NULL COMMENT '邮箱',
  `mobile` varchar(16) DEFAULT NULL COMMENT '手机',
  `type` int(4) NOT NULL DEFAULT '0' COMMENT '0:普通用户,1:管理员',
  `create_date` date NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `receive_notice` int(4) NOT NULL DEFAULT '0' COMMENT '是否接收各种通知,0:不接收,1:接收',
  `password` varchar(256) COMMENT '登录方式采用用户名密码验证时使用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

-- ----------------------------
-- Table structure for `user_consumer`
-- ----------------------------
DROP TABLE IF EXISTS `user_consumer`;
CREATE TABLE `user_consumer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `consumer_id` int(11) DEFAULT NULL COMMENT 'consumer id',
  PRIMARY KEY (`id`),
  KEY `t_c` (`tid`,`consumer_id`),
  KEY `u_t` (`uid`,`tid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户与消费者关系表';

-- ----------------------------
-- Table structure for `user_message`
-- ----------------------------
DROP TABLE IF EXISTS `user_message`;
CREATE TABLE `user_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户id',
  `message` varchar(512) NOT NULL COMMENT '消息内容',
  `status` tinyint(4) NOT NULL COMMENT '0:未读,1:已读',
  `create_date` datetime NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户消息表';

-- ----------------------------
-- Table structure for `user_producer`
-- ----------------------------
DROP TABLE IF EXISTS `user_producer`;
CREATE TABLE `user_producer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `producer` varchar(64) NOT NULL COMMENT 'producer名',
  PRIMARY KEY (`id`),
  KEY `t_p` (`tid`,`producer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户与生产者关系表';

-- ----------------------------
-- Table structure for `warn_config`
-- ----------------------------
DROP TABLE IF EXISTS `warn_config`;
CREATE TABLE `warn_config` (
  `consumer` varchar(64) DEFAULT '' COMMENT 'consumer名，为空时代表默认（仅一条默认记录）',
  `accumulate_time` int(11) DEFAULT '300000' COMMENT '堆积时间',
  `accumulate_count` int(11) DEFAULT '10000' COMMENT '堆积数量',
  `block_time` int(11) DEFAULT '10000' COMMENT '阻塞时间',
  `consumer_fail_count` int(11) DEFAULT '10' COMMENT '消费失败数量',
  `warn_unit_time` int(4) DEFAULT '1' COMMENT '报警频率的单位时间，单位小时',
  `warn_unit_count` int(4) DEFAULT '2' COMMENT '报警频率在单位时间的次数',
  `ignore_warn` int(4) DEFAULT '0' COMMENT '0:接收所有报警,1:不接收所有报警，此字段优先级最高',
  unique key (`consumer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='报警阈值配置表';

-- ----------------------------
-- Table structure for `name_server`
-- ----------------------------
DROP TABLE IF EXISTS `name_server`;
CREATE TABLE `name_server` (
  `cid` int(11) NOT NULL COMMENT '集群id',
  `addr` varchar(255) NOT NULL COMMENT 'name server 地址',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `check_status` tinyint(4) DEFAULT 0 COMMENT '检测结果:0:未知,1:正常,2:异常',
  `check_time` datetime COMMENT '检测时间',
  UNIQUE KEY `cid` (`cid`,`addr`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='name server表';

-- ----------------------------
-- Table structure for `server_warn_config`
-- ----------------------------
DROP TABLE IF EXISTS `server_warn_config`;
CREATE TABLE `server_warn_config` (
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
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务器预警配置表';

-- ----------------------------
-- Table structure for `message_reset`
-- ----------------------------
CREATE TABLE `message_reset` (
  `consumer` varchar(64) NOT NULL COMMENT 'consumer名',
  `reset_to` bigint(20) NOT NULL COMMENT '重置至时间戳，小于此时间的都将不再消息',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `consumer` (`consumer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息重置表';

-- ----------------------------
-- Table structure for `broker`
-- ----------------------------
DROP TABLE IF EXISTS `broker`;
CREATE TABLE `broker` (
  `cid` int(11) NOT NULL COMMENT '集群id',
  `addr` varchar(255) NOT NULL COMMENT 'broker 地址',
  `broker_name` varchar(64) NOT NULL COMMENT 'broker名字',
  `broker_id` int(4) NOT NULL COMMENT 'broker ID，0-master，1-slave',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `check_status` tinyint(4) DEFAULT 0 COMMENT '检测结果:0:未知,1:正常,2:异常',
  `check_time` datetime COMMENT '检测时间',
  UNIQUE KEY `cid` (`cid`,`addr`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='broker表';

-- ----------------------------
-- Table structure for `consumer_client_stat`
-- ----------------------------
DROP TABLE IF EXISTS `consumer_client_stat`;
CREATE TABLE `consumer_client_stat` (
  `consumer` varchar(255) NOT NULL COMMENT 'consumer',
  `client` varchar(20) NOT NULL COMMENT 'client',
  `create_date` date NOT NULL COMMENT '创建日期',
  KEY `cck` (`create_date`,`client`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消费者客户端统计表';

-- ----------------------------
-- Table structure for `audit_batch_associate`
-- ----------------------------
DROP TABLE IF EXISTS `audit_batch_associate`;
CREATE TABLE `audit_batch_associate` (
  `uids` text NOT NULL COMMENT '关联的用户id',
  `aid` int(11) NOT NULL COMMENT '审核id',
  `producer_ids` text NULL COMMENT '生产者id',
  `consumer_ids` text NULL COMMENT '消费者id',
  PRIMARY KEY (`aid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核批量关联';
-- ----------------------------
-- user init
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'admin', 'admin@admin.com', '18688888888', '1', '2018-10-01', '2018-10-01 09:49:00', '1', '21232f297a57a5a743894a0e4a801fc3');

-- ----------------------------
-- common_config init
-- ----------------------------
INSERT INTO `common_config` VALUES ('1', 'domain', '127.0.0.1:8080', 'mqcloud的域名');
INSERT INTO `common_config` VALUES ('5', 'serverUser', 'mqcloud', '服务器 ssh 用户');
INSERT INTO `common_config` VALUES ('6', 'serverPassword', '9j7t4SDJOIusddca+Mzd6Q==', '服务器 ssh 密码');
INSERT INTO `common_config` VALUES ('7', 'serverPort', '22', '服务器 ssh 端口');
INSERT INTO `common_config` VALUES ('8', 'serverConnectTimeout', '6000', '服务器 ssh 链接建立超时时间');
INSERT INTO `common_config` VALUES ('9', 'serverOPTimeout', '12000', '服务器 ssh 操作超时时间');
INSERT INTO `common_config` VALUES ('10', 'ciperKey', 'DJs32jslkdghDSDf', '密码助手的key,长度需为8的倍数');
INSERT INTO `common_config` VALUES ('12', 'operatorContact', '[{\"name\":\"admin\",\"phone\":\"010-1234\",\"mobile\":\"18688888888\",\"qq\":\"88888888\",\"email\":\"admin@admin.com\"}]', '运维人员json');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailHost', 'smtp.xx.com', '邮件服务器域名');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailUsername', 'xxx@xx.com', '邮件服务器用户');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailPassword', '密码或授权码', '邮件服务器用户密码');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailPort', '25', '邮件服务器端口');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailProtocol', 'smtp', '邮件服务器通信协议');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailTimeout', '10000', '邮件服务器超时时间');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('isOpenRegister', '1', '是否开启注册功能：0-不开启，1-开启');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('rocketmqFilePath', 'classpath:static/software/rocketmq.zip', 'rocketmq安装文件路径，支持以下三种资源加载方式,例如 1:classpath:static/software/rocketmq.zip 2：file:///tmp/rocketmq.zip 3：http://127.0.0.1:8080/software/rocketmq.zip');
INSERT INTO `common_config`(`key`, `comment`) VALUES ('privateKey', '私钥');
INSERT INTO `common_config`(`key`, `comment`) VALUES ('adminAccessKey', '管理员访问名(broker&nameserver使用)');
INSERT INTO `common_config`(`key`, `comment`) VALUES ('adminSecretKey', '管理员访问私钥(broker&nameserver使用)');
-- ----------------------------
-- warn_config init
-- ----------------------------
INSERT INTO `warn_config`(accumulate_time,accumulate_count,block_time,consumer_fail_count,warn_unit_time,warn_unit_count,ignore_warn) VALUES (300000, 10000, 10000, 10, 1, 1, 0);

-- ----------------------------
-- notice init
-- ----------------------------
INSERT INTO `notice` (`content`, `status`, `create_date`) VALUES ('欢迎您使用MQCloud，为了更好为您的服务，请花一分钟时间看下快速指南，如果有任何问题，欢迎联系我们^_^', 1, now());

-- ----------------------------
-- user message init
-- ----------------------------
INSERT INTO `user_message` (`uid`, `message`, `status`, `create_date`) VALUES (1, 'Hello！Welcome to MQCloud！', 0, now());
