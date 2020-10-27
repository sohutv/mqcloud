INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('queryMessageFromSlave', 'true', '是否从slave查询消息');
alter table `common_config` modify column `value` varchar(20000) DEFAULT '' COMMENT '配置值';
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('consumeFallBehindSize', '1073741824', '消费落后多少进行预警,单位byte');
alter table `audit_consumer` add column `permits_per_second` int(11) DEFAULT NULL COMMENT 'qps';

alter table `topic` add column `traffic_warn_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启流量预警,1:开启流量预警';
CREATE TABLE `topic_traffic_stat` (
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `avg_max` bigint(20) NOT NULL COMMENT '指定天数内,每天流量最大值的平均值',
  `max_max` bigint(20) NOT NULL COMMENT '指定天数内,去除异常点后流量的最大值',
  `days` int(4) NOT NULL COMMENT '指定统计流量的天数',
  `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='topic流量统计表';

INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('messageTypeLocation', 'classpath*:msg-type/*.class', '消息序列化方式为protostuf并且发送为自定义类型时，需要配置消息类型的class路径,例如 1:classpath*:msg-type/*.class 2：jar:file:///tmp/msgType.jar!/**/*.class 3：jar:http://127.0.0.1:8080/msgType.jar!/**/*.class');

CREATE TABLE `topic_traffic_warn_config` (
  `avg_multiplier` float(11,3) DEFAULT '5.000' COMMENT '平均流量值的乘数阈值;流量统计时，大于该值乘以平均流量值认定为异常值而被剔除',
  `avg_max_percentage_increase` float(11,3) DEFAULT '200.000' COMMENT '30天内每天流量最大值的平均值的百分比阈值；某时刻流量值大于最大值的平均值的增长阈值，则预警',
  `max_max_percentage_increase` float(11,3) DEFAULT '30.000' COMMENT '30天内流量最大值的增幅百分比阈值；某时刻流量值若大于最大值的该增幅阈值，则预警',
  `alarm_receiver` int(4) DEFAULT '0' COMMENT '告警接收人,0:生产者消费者及管理员,1:生产者和管理员,2:消费者和管理员,3:仅管理员,4:不告警',
  `topic` varchar(64) DEFAULT '' COMMENT 'topic名称，为空代表默认配置，只有一条默认配置',
  UNIQUE KEY `topic` (`topic`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='topic流量预警阈值配置';
INSERT INTO `topic_traffic_warn_config`(avg_multiplier,avg_max_percentage_increase,max_max_percentage_increase,alarm_receiver) VALUES (5, 200, 30, 0);

CREATE TABLE `audit_topic_traffic_warn` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `traffic_warn_enabled` int(11) NOT NULL COMMENT '0:不开启topic流量预警,1:开启topic流量预警'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核topic trafficWarn相关表';