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