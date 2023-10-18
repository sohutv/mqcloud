create table `audit_wheel_message_cancel`
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='时间轮定时消息取消表';

alter table `audit_associate_producer` change column `http_enabled` `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table `audit_topic` change column `http_enabled` `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table `user_producer` change column `http_enabled` `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table `audit_consumer` change column `http_consume_enabled` `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table `consumer` change column `http_consume_enabled` `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('proxyAcls', '', 'proxy的acl列表，例如：[{"clusterId":1,"accessKey":"RocketMQ","secretKey":"12345678"}]');

create table `cancel_uniqid`
(
    `tid`        int(11)     not null COMMENT 'topic id',
    `uniqueId`   varchar(50) not null COMMENT '取消消息uniqueId',
    `createTime` datetime    not null COMMENT '生成时间',
    UNIQUE KEY  uniqIndex (uniqueId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='取消ID记录';

-- ----------------------------
-- Table structure for `audit_timespan_message_export`
-- ----------------------------
DROP TABLE IF EXISTS `audit_timespan_message_export`;
CREATE TABLE `audit_timespan_message_export` (
    `aid` int(11) NOT NULL COMMENT '审核id',
    `topic` varchar(64) NOT NULL COMMENT 'topic',
    `start` bigint(20) DEFAULT NULL COMMENT '消费开始时间戳',
    `end` bigint(20) DEFAULT NULL COMMENT '消费结束时间戳'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='时间段消息导出审核表';

INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('exportedMessageLocalPath', '/tmp', '消息导出时的本地路径，例如/tmp');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('exportedMessageRemotePath', '消息导出时的远程地址，例如127.0.0.1:/tmp，请赋予mqcloud权限');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('exportedMessageDownloadUrlPrefix', '消息导出时的下载地址，例如http://127.0.0.1/tmp/，请用http开头');

-- ----------------------------
-- Table structure for `message_export`
-- ----------------------------
DROP TABLE IF EXISTS `message_export`;
CREATE TABLE `message_export`
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息导出记录表';

-- ----------------------------
-- Table structure for `consumer_client_metrics`
-- ----------------------------
DROP TABLE IF EXISTS `consumer_client_metrics`;
CREATE TABLE `consumer_client_metrics`
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消费者客户端统计';