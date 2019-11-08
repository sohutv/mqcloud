alter table `producer_total_stat` add column `exception` text COMMENT '异常记录';

CREATE TABLE `message_reset` (
  `consumer` varchar(64) NOT NULL COMMENT 'consumer名',
  `reset_to` bigint(20) NOT NULL COMMENT '重置至时间戳，小于此时间的都将不再消息',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `consumer` (`consumer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息重置表';

CREATE TABLE `audit_resend_message_consumer` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `consumer_id` int(11) NOT NULL COMMENT 'consumer id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息重发给消费者审核表';
