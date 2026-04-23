alter table `warn_config` add column `consumer_dead_count` int(11) COMMENT '死消息数量';
alter table `audit_consumer_delete` modify column `consumer` varchar(255) DEFAULT NULL COMMENT 'consumer名';
alter table `audit_user_consumer_delete` modify column `consumer` varchar(255) DEFAULT NULL COMMENT 'consumer名';
alter table `audit_timespan_message_consume` modify column `consumer` varchar(255) NOT NULL COMMENT 'consumer名';
alter table `consumer` modify column `name` varchar(255) NOT NULL COMMENT 'consumer名';
alter table `consumer_config` modify column `consumer` varchar(255) NOT NULL COMMENT 'consumer名';
alter table `consumer_pause_config` modify column `consumer` varchar(255) NOT NULL COMMENT 'consumer名';
alter table `consumer_client_metrics` modify column `consumer` varchar(255) NOT NULL COMMENT 'consumer名';
alter table `warn_config` modify column `consumer` varchar(255) DEFAULT '' COMMENT 'consumer名，为空时代表默认（仅一条默认记录）';