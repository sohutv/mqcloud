CREATE TABLE `message_reset` (
  `consumer` varchar(64) NOT NULL COMMENT 'consumer名',
  `reset_to` bigint(20) NOT NULL COMMENT '重置至时间戳，小于此时间的都将不再消息',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `consumer` (`consumer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息重置表';