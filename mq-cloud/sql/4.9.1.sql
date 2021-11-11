insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'enableScheduleMessageStats', 'true', '开启schedule队列消息统计', null, 28, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'isEnableBatchPush', 'false', 'DLedger批量复制', null, 29, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'autoDeleteUnusedStats', 'false', '删除topic或订阅时一起删除相关统计', null, 30, 1, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'registerBroker', 'true', '是否注册broker', null, 31, 1, null, 0);


INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailUseSSL', 'false', '邮件是否使用SSL');

alter table `audit_reset_offset` add column `message_key` varchar(360) DEFAULT NULL COMMENT '消息key';
alter table `consumer_config` add column `retry_message_skip_key` varchar(360) DEFAULT NULL COMMENT '消息key';
alter table `user` add column `receive_phone_notice` int(4) NOT NULL DEFAULT '0' COMMENT '用户是否接收手机通知,0:不接收,1:接收';

-- ----------------------------
-- Table structure for `user_warn`
-- ----------------------------
DROP TABLE IF EXISTS `user_warn`;
CREATE TABLE `user_warn` (
  `uid` int(11) NOT NULL,
  `type` int(4) NOT NULL COMMENT '警告类型',
  `resource` varchar(100) DEFAULT NULL COMMENT '警告资源:topic,producer等',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `wid` int(11) NOT NULL,
  KEY `ut` (`uid`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户警告表';

-- ----------------------------
-- Table structure for `warn_info`
-- ----------------------------
DROP TABLE IF EXISTS `warn_info`;
CREATE TABLE `warn_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` text DEFAULT NULL COMMENT '警告内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='警告信息表';