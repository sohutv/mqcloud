alter table producer_total_stat modify column `client` varchar(100) NOT NULL COMMENT 'client';
alter table producer_total_stat drop index `client`;
alter table producer_total_stat add index `date_client` (`create_date`,`client`);

DROP TABLE IF EXISTS `consumer_client_stat`;
CREATE TABLE `consumer_client_stat` (
  `consumer` varchar(255) NOT NULL COMMENT 'consumer',
  `client` varchar(20) NOT NULL COMMENT 'client',
  `create_date` date NOT NULL COMMENT '创建日期',
  KEY `cck` (`create_date`,`client`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消费者客户端统计表';

DROP TABLE IF EXISTS `audit_batch_associate`;
CREATE TABLE `audit_batch_associate` (
  `uids` text NOT NULL COMMENT '关联的用户id',
  `aid` int(11) NOT NULL COMMENT '审核id',
  `producer_ids` text NULL COMMENT '生产者id',
  `consumer_ids` text NULL COMMENT '消费者id',
  PRIMARY KEY (`aid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审核批量关联';