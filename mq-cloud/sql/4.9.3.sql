-- ----------------------------
-- Table structure for `client_language`
-- ----------------------------
DROP TABLE IF EXISTS `client_language`;
CREATE TABLE `client_language` (
    `cid` int(11) NOT NULL COMMENT 'cluster id',
    `tid` int(11) NOT NULL COMMENT 'topic id',
    `client_group_name` varchar(255) NOT NULL COMMENT '生产者/消费者名称',
    `client_group_type` tinyint(4) NOT NULL COMMENT '类型 0 生产者 1 消费者',
    `language` tinyint(4) DEFAULT NULL COMMENT '客户端语言',
    `version` varchar(50) DEFAULT NULL COMMENT '版本',
    `relation_uids` varchar(255) DEFAULT NULL COMMENT '关联人员id集合，逗号分隔',
    `modify_type` int(4) NOT NULL DEFAULT '0' COMMENT '修改方式 0 自动 1 手动',
    `create_date` date NOT NULL COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `union_key` (`cid`,`tid`,`client_group_name`),
    KEY `nomal_query_index` (`client_group_name`,`client_group_type`,`language`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='客户端语言';

alter table `audit_consumer` add column `http_consume_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启http消费,1:开启http消费';
alter table `consumer` add column `http_consume_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启http消费,1:开启http消费';
alter table `audit_topic` add column `http_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启http生产,1:开启http生产';
alter table `user_producer` add column `http_enabled` int(4) NOT NULL DEFAULT '0' COMMENT '0:不开启http生产,1:开启http生产';

-- ----------------------------
-- add new broker configuration
-- ----------------------------
INSERT INTO `broker_config`(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) VALUES (1, 'jvmMemory', '8g', 'broker启动内存大小,对应jvm参数中的xmx/xms,单位:g或m', '', 16, 0, NULL, 0);
INSERT INTO `broker_config`(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) VALUES (1, 'maxDirectMemorySize', '15g', '堆外内存大小,单位:g', NULL, 17, 0, NULL, 0);
INSERT INTO `broker_config`(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) VALUES (1, 'physicalMemorySize', '0', '物理内存(单位字节)，0代表使用全部物理内存', '部署到docker或多个broker部署到一台机器时使用', 18, 1, NULL, 0);