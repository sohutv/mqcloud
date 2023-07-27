alter table `audit_associate_producer` change column `http_enabled` `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table `audit_topic` change column `http_enabled` `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table `user_producer` change column `http_enabled` `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table `audit_consumer` change column `http_consume_enabled` `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
alter table `consumer` change column `http_consume_enabled` `protocol` int(4) NOT NULL DEFAULT '0' COMMENT '0:remoting,1:http,2:proxy remoting,3:grpc';
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('proxyAcls', '', 'proxy的acl列表，例如：[{"clusterId":1,"accessKey":"RocketMQ","secretKey":"12345678"}]');