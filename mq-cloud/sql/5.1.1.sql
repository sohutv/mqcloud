insert into broker_config_group(`id`, `group`, `order`) values(28, '备代主模式', 28);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'enableSlaveActingMaster', 'false', '是否启用备代主', null, 1, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'enableRemoteEscape', 'false', '是否允许二级消息远程逃逸', null, 2, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'totalReplicas', '1', 'broker副本总个数（包括master）', null, 3, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'brokerHeartbeatInterval', '1000', 'broker向ns发送的轻量心跳的时间间隔', '默认1秒，单位ms', 4, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'brokerNotActiveTimeoutMillis', '10000', '超时ns未收到心跳认为broker下线', '默认10秒，单位ms', 5, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'sendHeartbeatTimeoutMillis', '1000', 'broker向ns发送的轻量心跳的超时时间，单位毫秒', '默认1秒，单位ms', 6, 0, null, 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'skipPreOnline', 'false', 'master重启是否跳过预上线流程', '预上线流程用于master启动后先不注册到ns，从代理备同步元数据后再注册', 7, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'compatibleWithOldNameSrv', 'true', '是否兼容旧的ns', null, 8, 0, 'true:是;false:否;', 0);
insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'lockInStrictMode', 'false', 'lockInStrictMode', null, 9, 0, 'true:是;false:否;', 0);

INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('ignoreErrorProducerSet', '[]', '忽略生产错误预警的生产者');

-- ----------------------------
-- Table structure for `consumer_pause_config`
-- ----------------------------
DROP TABLE IF EXISTS `consumer_pause_config`;
CREATE TABLE `consumer_pause_config`(
    `consumer`        varchar(64) NOT NULL COMMENT 'consumer名',
    `pause_client_id` varchar(255)         DEFAULT NULL COMMENT '暂停的客户端Id',
    `unregister`      tinyint(4) DEFAULT NULL COMMENT '0:不解注册,1:解注册',
    `update_time`     timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `c_p_c` (`consumer`, `pause_client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='客户端暂停配置表';

alter table `topic` add column `size` bigint(20) DEFAULT '0' COMMENT 'topic put size in one hour';
alter table `topic` add column `size_1d` bigint(20) DEFAULT '0' COMMENT 'topic put size in one day';
alter table `topic` add column `size_2d` bigint(20) DEFAULT '0' COMMENT 'topic put size in two days';
alter table `topic` add column `size_3d` bigint(20) DEFAULT '0' COMMENT 'topic put size in three days';
alter table `topic` add column `size_5d` bigint(20) DEFAULT '0' COMMENT 'topic put size in five days';
alter table `topic` add column `size_7d` bigint(20) DEFAULT '0' COMMENT 'topic put size in seven days';

alter table `broker` add column `size_1d` bigint(20) DEFAULT '0' COMMENT 'put size in one day';
alter table `broker` add column `size_2d` bigint(20) DEFAULT '0' COMMENT 'put size in two days';
alter table `broker` add column `size_3d` bigint(20) DEFAULT '0' COMMENT 'put size in three days';
alter table `broker` add column `size_5d` bigint(20) DEFAULT '0' COMMENT 'put size in five days';
alter table `broker` add column `size_7d` bigint(20) DEFAULT '0' COMMENT 'put size in seven days';