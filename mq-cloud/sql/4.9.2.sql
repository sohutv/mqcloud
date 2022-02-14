-- ----------------------------
-- Table structure for `audit_timespan_message_consume`
-- ----------------------------
DROP TABLE IF EXISTS `audit_timespan_message_consume`;
CREATE TABLE `audit_timespan_message_consume` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `topic` varchar(64) NOT NULL COMMENT 'topic',
  `consumer` varchar(64) NOT NULL COMMENT 'consumer',
  `client_id` varchar(255) DEFAULT NULL COMMENT '暂停的客户端Id',
  `start` bigint(20) DEFAULT NULL COMMENT '消费开始时间戳',
  `end` bigint(20) DEFAULT NULL COMMENT '消费结束时间戳'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='时间段消息消费审核表';

-- ----------------------------
-- Table structure for `user_group`
-- ----------------------------
DROP TABLE IF EXISTS `user_group`;
CREATE TABLE `user_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'group id',
  `name` varchar(64) NOT NULL COMMENT 'name',
  `create_date` date NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户组';

alter table `user` add column `gid` int(11) NOT NULL DEFAULT '0' COMMENT '组id';