alter table `consumer` add column `info` varchar(360) DEFAULT NULL COMMENT '消费者描述';

alter table `audit_topic` add column `serializer` int(4) NOT NULL DEFAULT '0' COMMENT '序列化器 0:Protobuf,1:String';

alter table `topic` add column `serializer` int(4) NOT NULL DEFAULT '0' COMMENT '序列化器 0:Protobuf,1:String';