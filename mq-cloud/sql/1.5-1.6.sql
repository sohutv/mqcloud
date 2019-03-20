alter table `cluster` add column `transaction_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不支持事务,1:支持事务';
alter table `cluster` add column `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不支持trace,1:支持trace';

alter table broker_traffic modify put_size bigint(20) NOT NULL DEFAULT '0' COMMENT '生产消息大小';
alter table broker_traffic modify put_count bigint(20) NOT NULL DEFAULT '0' COMMENT '生产消息量';
alter table broker_traffic modify get_count bigint(20) NOT NULL DEFAULT '0' COMMENT '消费消息量';
alter table broker_traffic modify get_size bigint(20) NOT NULL DEFAULT '0' COMMENT '消费消息大小';

alter table topic_traffic modify size bigint(20) DEFAULT NULL COMMENT 'topic put size';
alter table topic_traffic modify count bigint(20) DEFAULT NULL COMMENT 'topic put times';

alter table consumer_traffic modify size bigint(20) DEFAULT NULL COMMENT 'consumer pull size';
alter table consumer_traffic modify count bigint(20) DEFAULT NULL COMMENT 'consumer pull times';

alter table consumer_stat modify undone_msg_count bigint(20) DEFAULT NULL COMMENT '未消费的消息量';
alter table consumer_stat modify undone_1q_msg_count bigint(20) DEFAULT NULL COMMENT '单队列未消费的最大消息量';

alter table `audit_consumer` add column `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace';
alter table `consumer` add column `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace';