alter table `audit_consumer_config` add column `unregister` tinyint(4) DEFAULT NULL COMMENT '0:不解注册,1:解注册';
alter table `consumer_config` add column `unregister` tinyint(4) DEFAULT NULL COMMENT '0:不解注册,1:解注册';
alter table `broker` add column `writable` int(4) NOT NULL DEFAULT '1' COMMENT '0:不可写入,1:可写入';