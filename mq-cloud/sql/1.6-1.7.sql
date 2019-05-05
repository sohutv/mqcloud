alter table `audit_topic` add column `test_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:非测试topic,1:测试topic';

alter table `topic` add column `info` varchar(360) DEFAULT NULL COMMENT 'topic描述';

alter table `topic` add column `delay_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不发送延迟消息,1:发送延迟消息。注：此字段不强制该topic的消息类型，仅为流量展示';

alter table `audit_topic` add column `delay_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不发送延迟消息,1:发送延迟消息。注：此字段不强制该topic的消息类型，仅为流量展示';