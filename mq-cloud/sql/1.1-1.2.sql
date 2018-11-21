-- ----------------------------
-- update for email server init for 1.2.RELEASE
-- ----------------------------
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailHost', 'smtp.xx.com', '邮件服务器域名');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailUsername', 'xxx@xx.com', '邮件服务器用户');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailPassword', '密码或授权码', '邮件服务器用户密码');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailPort', '25', '邮件服务器端口');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailProtocol', 'smtp', '邮件服务器通信协议');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('mailTimeout', '5000', '邮件服务器超时时间');
INSERT INTO `common_config`(`key`, `value`, `comment`) VALUES ('isOpenRegister', '1', '是否开启注册功能：0-不开启，1-开启');
alter table server add `machine_type` int(4) DEFAULT NULL COMMENT '机器类型：0-未知，1-物理机，2-虚拟机，3-docker';

-- ----------------------------
-- Table structure for `server_warn_config`
-- ----------------------------
DROP TABLE IF EXISTS `server_warn_config`;
CREATE TABLE `server_warn_config` (
  `ip` varchar(15) NOT NULL COMMENT 'ip',
  `memory_usage_rate` int(4) NOT NULL DEFAULT '0' COMMENT '内存使用率',
  `load1` int(4) NOT NULL DEFAULT '0' COMMENT '一分钟load',
  `connect` int(4) NOT NULL DEFAULT '0' COMMENT 'tcp连接数',
  `wait` int(4) NOT NULL DEFAULT '0' COMMENT 'tcp等待数',
  `iops` int(4) NOT NULL DEFAULT '0' COMMENT '磁盘io速率 交互次数/s',
  `iobusy` int(4) NOT NULL DEFAULT '0' COMMENT '磁盘io带宽使用百分比',
  `cpu_usage_rate` int(4) NOT NULL DEFAULT '0' COMMENT 'cpu使用率',
  `net_in` int(4) NOT NULL DEFAULT '0' COMMENT '入网流量',
  `net_out` int(4) NOT NULL DEFAULT '0' COMMENT '出网流量',
  `io_usage_rate` int(4) NOT NULL DEFAULT '0' COMMENT '磁盘使用率',
  UNIQUE KEY `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务器预警配置表';

-- ----------------------------
-- Table structure for `broker`
-- ----------------------------
DROP TABLE IF EXISTS `broker`;
CREATE TABLE `broker` (
  `cid` int(11) NOT NULL COMMENT '集群id',
  `addr` varchar(255) NOT NULL COMMENT 'broker 地址',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `check_status` tinyint(4) DEFAULT 0 COMMENT '检测结果:0:未知,1:正常,2:异常',
  `check_time` datetime COMMENT '检测时间',
  `broker_name` varchar(64) NOT NULL COMMENT 'broker名字',
  `broker_id` int(4) NOT NULL COMMENT 'broker ID，0-master，1-slave',
  UNIQUE KEY `cid` (`cid`,`addr`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='broker表';

-- ----------------------------
-- update for warn_config 
-- ----------------------------
alter table `warn_config` add column `consumer` varchar(64) DEFAULT '' COMMENT 'consumer名，为空时代表默认（仅一条默认记录）';
alter table `warn_config` add unique key (`consumer`);
alter table `warn_config` drop column `id`;
alter table `warn_config` drop column `uid`;
alter table `warn_config` drop column `topic`;
alter table `warn_config` drop column `ignore_topic`;

alter table `name_server` add `check_status` tinyint(4) DEFAULT 0 COMMENT '检测结果:0:未知,1:正常,2:异常';
alter table `name_server` add `check_time` datetime COMMENT '检测时间';

alter table `need_warn_config` modify column `oKey` varchar(255);