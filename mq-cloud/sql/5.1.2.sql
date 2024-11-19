alter table `topic_traffic_warn_config` add column `min_warn_count` bigint(20) DEFAULT NULL COMMENT '最小预警数量';
update `topic_traffic_warn_config` set `min_warn_count` = 100 where topic = '';

-- ----------------------------
-- Table structure for `data_migration`
-- ----------------------------
DROP TABLE IF EXISTS `data_migration`;
CREATE TABLE `data_migration`
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='数据迁移表';

INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('rsyncConfig', '{"user":"mqcloud","password":"rsync","module":"mqcloud"}', 'rsync配置');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('pauseAudit', 'false', '在集群运维时，设置为true，会暂停审核功能，保障数据安全');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('clusterStoreWarnConfig', '[{"max":500,"percent99":400}]', '集群broker存储过慢预警配置，默认为最大响应超过500ms或百分之99响应超过400ms进行预警，可以单独对某个集群进行配置，例如[{"max":500,"percent99":400},{"clusterId":1,"max":1000,"percent99":800}]');

-- ----------------------------
-- Table structure for `broker_tmp`
-- ----------------------------
DROP TABLE IF EXISTS `broker_tmp`;
CREATE TABLE `broker_tmp`
(
    `cid`         int(11) NOT NULL COMMENT '集群id',
    `addr`        varchar(255) NOT NULL COMMENT 'broker 地址',
    `broker_name` varchar(64)  NOT NULL COMMENT 'broker名字',
    `broker_id`   int(4) NOT NULL COMMENT 'broker ID，0-master，1-slave',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `base_dir`    varchar(360)          DEFAULT NULL COMMENT '安装路径',
    UNIQUE KEY `cid` (`cid`,`addr`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='broker临时表';

-- ----------------------------
-- Table structure for `audit_http_consumer_config`
-- ----------------------------
DROP TABLE IF EXISTS `audit_http_consumer_config`;
CREATE TABLE `audit_http_consumer_config`
(
    `aid`             int(11) NOT NULL COMMENT '审核id',
    `consumer_id`     int(11) NOT NULL COMMENT 'consumer id',
    `pull_size`       int(11) DEFAULT NULL COMMENT '拉取消息量',
    `pull_timeout`    int(11) DEFAULT NULL COMMENT '拉取超时时间，单位毫秒',
    `consume_timeout` int(11) DEFAULT NULL COMMENT '消费超时时间，单位毫秒',
    PRIMARY KEY (`aid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核HTTP消费者配置相关表';

insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(4, 'sendMsgRateLimitQps', '10000', '发送消息限流qps', null, 4, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(4, 'sendRetryMsgRateLimitQps', '1000', '发送重试消息限流qps', null, 5, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(4, 'enableRateLimit', 'true', '是否启用发送消息限流', null, 6, 1, null, 0);

-- ----------------------------
-- Table structure for `topic_warn_config`
-- ----------------------------
DROP TABLE IF EXISTS `topic_warn_config`;
CREATE TABLE `topic_warn_config`
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='topic报警阈值配置表';

alter table `topic` add column `count_1d` bigint(20) DEFAULT '0' COMMENT 'topic put count in one day';
alter table `topic` add column `count_2d` bigint(20) DEFAULT '0' COMMENT 'topic put count in two days';

alter table `topic` add column `msg_type` int(4) NOT NULL DEFAULT '0' COMMENT '消息类型，0:普通消息，1:延迟消息，2:定时消息';
alter table `audit_topic` add column `msg_type` int(4) NOT NULL DEFAULT '0' COMMENT '消息类型，0:普通消息，1:延迟消息，2:定时消息';