alter table `audit_topic` add column `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace';
alter table `audit_topic` add column `transaction_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启事务,1:开启事务';
alter table `topic` add column `trace_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启trace,1:开启trace';