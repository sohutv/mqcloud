alter table `server` add column `room` varchar(255) DEFAULT NULL COMMENT '机房';
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('machineRoom', '["默认"]', '机房列表');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('machineRoomColor', '["#95a5a6"]', '机房节点颜色');

CREATE TABLE `broker_store_stat` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='broker存储统计';