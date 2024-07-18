-- ----------------------------
-- user init
-- ----------------------------
INSERT IGNORE INTO `user`(`id`, name, email, mobile, type, create_date, update_time, receive_notice, password) VALUES ('1', 'admin', 'admin@admin.com', '18688888888', '1', '2018-10-01', '2018-10-01 09:49:00', '1', '21232f297a57a5a743894a0e4a801fc3');

-- ----------------------------
-- common_config init
-- ----------------------------
INSERT IGNORE INTO `common_config` VALUES ('1', 'domain', '127.0.0.1:8080', 'mqcloud的域名');
INSERT IGNORE INTO `common_config` VALUES ('2', 'serverUser', 'mqcloud', '服务器 ssh 用户');
INSERT IGNORE INTO `common_config` VALUES ('3', 'serverPassword', '9j7t4SDJOIusddca+Mzd6Q==', '服务器 ssh 密码');
INSERT IGNORE INTO `common_config` VALUES ('4', 'serverPort', '22', '服务器 ssh 端口');
INSERT IGNORE INTO `common_config` VALUES ('5', 'serverConnectTimeout', '6000', '服务器 ssh 链接建立超时时间');
INSERT IGNORE INTO `common_config` VALUES ('6', 'serverOPTimeout', '12000', '服务器 ssh 操作超时时间');
INSERT IGNORE INTO `common_config` VALUES ('7', 'ciperKey', 'DJs32jslkdghDSDf', '密码助手的key,长度需为8的倍数');
INSERT IGNORE INTO `common_config` VALUES ('8', 'operatorContact', '[{"name":"admin","phone":"010-1234","mobile":"18688888888","qq":"88888888","email":"admin@admin.com"}]', '运维人员json');
INSERT IGNORE INTO `common_config` VALUES ('9',  'mailHost', 'smtp.xx.com', '邮件服务器域名');
INSERT IGNORE INTO `common_config` VALUES ('10',  'mailUsername', 'xxx@xx.com', '邮件服务器用户');
INSERT IGNORE INTO `common_config` VALUES ('11',  'mailPassword', '密码或授权码', '邮件服务器用户密码');
INSERT IGNORE INTO `common_config` VALUES ('12',  'mailPort', '25', '邮件服务器端口');
INSERT IGNORE INTO `common_config` VALUES ('13',  'mailProtocol', 'smtp', '邮件服务器通信协议');
INSERT IGNORE INTO `common_config` VALUES ('14',  'mailTimeout', '10000', '邮件服务器超时时间');
INSERT IGNORE INTO `common_config` VALUES ('15',  'mailUseSSL', 'false', '邮件是否使用SSL');
INSERT IGNORE INTO `common_config` VALUES ('16',  'isOpenRegister', '1', '是否开启注册功能：0-不开启，1-开启');
INSERT IGNORE INTO `common_config` VALUES ('17',  'rocketmqFilePath', 'classpath:static/software/rocketmq.zip', 'rocketmq安装文件路径，支持以下三种资源加载方式,例如 1:classpath:static/software/rocketmq.zip 2：file:///tmp/rocketmq.zip 3：http://127.0.0.1:8080/software/rocketmq.zip');
INSERT IGNORE INTO `common_config` VALUES ('18',  'privateKey', '', '私钥');
INSERT IGNORE INTO `common_config` VALUES ('19',  'adminAccessKey', '', '管理员访问名(broker&nameserver使用)');
INSERT IGNORE INTO `common_config` VALUES ('20',  'adminSecretKey', '', '管理员访问私钥(broker&nameserver使用)');
INSERT IGNORE INTO `common_config` VALUES ('21',  'machineRoom', '["默认"]', '机房列表');
INSERT IGNORE INTO `common_config` VALUES ('22',  'machineRoomColor', '["#95a5a6"]', '机房节点颜色');
INSERT IGNORE INTO `common_config` VALUES ('23',  'queryMessageFromSlave', 'true', '是否从slave查询消息');
INSERT IGNORE INTO `common_config` VALUES ('24',  'consumeFallBehindSize', '1073741824', '消费落后多少进行预警,单位byte');
INSERT IGNORE INTO `common_config` VALUES ('25',  'messageTypeLocation', 'classpath*:msg-type/*.class', '消息序列化方式为protostuf并且发送为自定义类型时，需要配置消息类型的class路径,例如 1:classpath*:msg-type/*.class 2：jar:file:///tmp/msgType.jar!/**/*.class 3：jar:http://127.0.0.1:8080/msgType.jar!/**/*.class');
INSERT IGNORE INTO `common_config` VALUES ('26',  'slaveFallBehindSize', '10485760', 'slave的commitlog落后master多少进行预警,单位byte');
INSERT IGNORE INTO `common_config` VALUES ('27', 'mqProxyServerString', '127.0.0.1', 'MQProxy服务器地址列表，多个用逗号分割');
INSERT IGNORE INTO `common_config` VALUES ('28', 'oldReqestCodeBrokerSet', '', '使用旧请求码的broker列表，例如：["127.0.0.1:10911","127.0.0.2:10911"]');
INSERT IGNORE INTO `common_config` VALUES ('29', 'rocketmq5FilePath', 'classpath:static/software/rocketmq5.zip', 'rocketmq5安装文件路径，支持以下三种资源加载方式,例如 1:classpath:static/software/rocketmq5.zip 2：file:///tmp/rocketmq5.zip 3：http://127.0.0.1:8080/software/rocketmq5.zip');
INSERT IGNORE INTO `common_config` VALUES ('30', 'clientGroupNSConfig', '{}', '客户端ns配置');
INSERT IGNORE INTO `common_config` VALUES ('31', 'proxyAcls', '', 'proxy的acl列表，例如：[{"clusterId":1,"accessKey":"RocketMQ","secretKey":"12345678"}]');
INSERT IGNORE INTO `common_config` VALUES ('32', 'exportedMessageLocalPath', '/tmp', '消息导出时的本地路径，例如/tmp');
INSERT IGNORE INTO `common_config` VALUES ('33', 'exportedMessageRemotePath', '', '消息导出时的远程地址，例如127.0.0.1:/tmp，请赋予mqcloud权限');
INSERT IGNORE INTO `common_config` VALUES ('34', 'exportedMessageDownloadUrlPrefix', '', '消息导出时的下载地址，例如http://127.0.0.1/tmp/，请用http开头');
INSERT IGNORE INTO `common_config` VALUES ('35', 'orderTopicKVConfig', '{}', '全局有序topic路由配置');
INSERT IGNORE INTO `common_config` VALUES ('36', 'rsyncConfig', '{"user":"mqcloud","module":"mqcloud","password":"rsync"}', 'rsync配置');
-- ----------------------------
-- warn_config init
-- ----------------------------
INSERT IGNORE INTO `warn_config`(accumulate_time,accumulate_count,block_time,consumer_fail_count,warn_unit_time,warn_unit_count,ignore_warn) VALUES (300000, 10000, 10000, 10, 1, 1, 0);

-- ----------------------------
-- notice init
-- ----------------------------
INSERT IGNORE INTO `notice` VALUES (1, '欢迎您使用MQCloud，为了更好为您的服务，请花一分钟时间看下文档，如果有任何问题，欢迎联系我们^_^', 1, now());

-- ----------------------------
-- user message init
-- ----------------------------
INSERT IGNORE INTO `user_message` (`id`, `uid`, `message`, `status`, `create_date`) VALUES (1, 1, 'Hello！Welcome to MQCloud！', 0, now());

-- ----------------------------
-- `broker_config_group` record
-- ----------------------------
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(1, '常见配置', 1);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(2, '发现机制', 2);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(3, 'topic&订阅', 3);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(4, '写入限流机制', 4);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(5, '请求处理线程池', 5);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(6, '内存预热', 6);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(7, 'CommitLog相关', 7);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(8, 'ConsumeQueue相关', 8);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(9, '堆外内存相关', 9);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(10, 'HA机制', 10);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(11, '数据文件保留机制', 11);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(12, '消息相关', 12);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(13, '消费优化', 13);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(14, '拉取消息', 14);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(15, '快速失败机制', 15);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(16, 'broker保护机制', 16);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(17, '注册相关', 17);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(18, '事务相关', 18);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(19, 'salve相关', 19);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(20, 'filter相关', 20);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(21, 'netty server相关', 21);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(22, 'netty client相关', 22);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(23, 'rpc消息', 23);
INSERT IGNORE into broker_config_group(`id`, `group`, `order`) values(24, '其他配置', 24);
insert IGNORE into broker_config_group(`id`, `group`, `order`) values(25, 'controller配置', 25);
insert IGNORE into broker_config_group(`id`, `group`, `order`) values(26, '压缩topic配置', 26);
insert IGNORE into broker_config_group(`id`, `group`, `order`) values(27, '时间轮延迟消息', 27);
insert IGNORE into broker_config_group(`id`, `group`, `order`) values(28, '备代主模式', 28);


-- ----------------------------
-- `broker_config` record
-- ----------------------------
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerName', null, 'broker名', null, 1, 0, null, 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'rocketmqHome', null, 'broker安装目录', '启动脚本中已经设置,无需再设置', 2, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerRole', 'ASYNC_MASTER', 'broker角色', null, 3, 0, 'ASYNC_MASTER:异步复制master;SYNC_MASTER:同步双写master;SLAVE:slave;', 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'flushDiskType', 'ASYNC_FLUSH', '刷盘机制', null, 4, 0, 'ASYNC_FLUSH:异步刷盘;SYNC_FLUSH:同步刷盘;', 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerIP1', null, 'broker的ip', '服务器ip,尤其对于多网卡情况', 5, 0, null, 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerIP2', null, 'master HA ip', '与brokerIP1一致', 6, 0, null, 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'listenPort', '10911', 'broker监听端口', null, 7, 0, null, 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerClusterName', null, '集群名', null, 8, 0, null, 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerId', '0', '0:master,非0:slave', null, 9, 0, null, 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'brokerPermission', '6', 'broker权限', 'broker下线可以设置为只读', 10, 1, '2:只写;4:只读;6:读写', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'storePathRootDir', null, '数据文件存储根目录', '务必设置', 11, 0, null, 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'storePathCommitLog', null, 'CommitLog文件存储根目录', '务必设置', 12, 0, null, 1);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(2, 'namesrvAddr', null, 'namesrv地址', '若采用域名寻址模式无需设置', 1, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(2, 'fetchNamesrvAddrByAddressServer', 'true', '域名方式获取NameServer地址', '若配置namesrvAddr无需设置此项', 2, 0, 'true:是;false:否;', 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(2, 'rmqAddressServerDomain', null, 'NameServer地址域名', null, 3, 0, null, 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(2, 'rmqAddressServerSubGroup', null, 'NameServer地址域名子路径', null, 4, 0, null, 1);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'autoCreateTopicEnable', 'true', '发送消息时没有topic自动创建', '线上建议设置为false', 1, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'autoCreateSubscriptionGroup', 'true', '自动创建订阅组', '线上建议设置为false', 2, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'defaultTopicQueueNums', '8', 'autoCreateTopicEnable为true时,创建topic的队列数', '无需修改', 3, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'clusterTopicEnable', 'true', '自动创建以cluster为名字的topic', null, 4, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'brokerTopicEnable', 'true', '自动创建以broker为名字的topic', null, 5, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'traceTopicEnable', 'false', '是否启用trace topic', null, 6, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(3, 'msgTraceTopicName', 'RMQ_SYS_TRACE_TOPIC', '默认的trace topic名', null, 7, 0, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(4, 'useReentrantLockWhenPutMessage', 'false', '写消息是否使用重入锁', '若不使用建议配合transientStorePool使用；若使用要加大sendMessageThreadPoolNums', 1, 0, 'true:是;false:否;', 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(4, 'sendMessageThreadPoolNums', '1', '处理发消息的线程池大小', '默认使用spin锁', 2, 0, null, 1);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(4, 'waitTimeMillsInSendQueue', '200', '消息发送请求超过阈值没有处理则返回失败', '若出现broker busy建议调大', 3, 1, null, 1);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'sendThreadPoolQueueCapacity', '10000', '处理发消息的线程池队列大小', null, 2, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'pullMessageThreadPoolNums', '16+核数*2', '处理拉消息的线程池大小', null, 3, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'pullThreadPoolQueueCapacity', '100000', '处理拉消息的线程池队列大小', null, 4, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'queryMessageThreadPoolNums', '8+核数', '处理消息查询的线程池大小', null, 5, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'queryThreadPoolQueueCapacity', '20000', '处理消息查询的线程池队列大小', null, 6, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'adminBrokerThreadPoolNums', '16', '处理admin请求的线程池大小', null, 7, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'clientManageThreadPoolNums', '32', '处理客户端(解注册等)的线程池大小', null, 8, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'clientManagerThreadPoolQueueCapacity', '1000000', '处理客户端(解注册等)的线程池队列大小', null, 9, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'consumerManageThreadPoolNums', '32', '处理消费者请求的线程池大小', null, 10, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'consumerManagerThreadPoolQueueCapacity', '1000000', '处理消费者请求的线程池队列大小', null, 11, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'heartbeatThreadPoolNums', 'min(32, 核数)', '处理客户端心跳的线程池大小', null, 12, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'heartbeatThreadPoolQueueCapacity', '50000', '处理客户端心跳的线程池队列大小', null, 13, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'endTransactionThreadPoolNums', '8+核数*2', '处理结束事务请求的线程池大小', null, 14, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(5, 'endTransactionPoolQueueCapacity', '100000', '处理结束事务请求的线程池队列大小', null, 15, 0, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(6, 'warmMapedFileEnable', 'false', 'mmap时进行是否进行内存预热，避免缺页异常', null, 1, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(6, 'flushLeastPagesWhenWarmMapedFile', '4096', '预热时同时刷多少页内存(同步刷盘时)', null, 2, 1, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'mappedFileSizeCommitLog', '1073741824', 'CommitLog文件大小', '默认大小1G，如非必要请勿修改', 1, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'flushIntervalCommitLog', '500', '异步刷盘时间间隔', '单位ms', 2, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'flushCommitLogTimed', 'false', '是否定时刷CommitLog，若否会实时刷', null, 3, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'flushCommitLogLeastPages', '4', '最少凑够多少页内存才刷CommitLog', null, 4, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'flushCommitLogThoroughInterval', '10000', '两次刷CommitLog最大间隔，若超过，不校验页数直接刷', '单位ms,默认10秒', 5, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(7, 'syncFlushTimeout', '5000', '同步刷CommitLog超时时间', '单位ms,默认5秒', 6, 1, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(8, 'mappedFileSizeConsumeQueue', '6000000', 'ConsumeQueue文件存储的条目', '默认为30万，如非必要请勿修改', 1, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(8, 'flushIntervalConsumeQueue', '1000', '异步刷ConsumeQueue时间间隔', '单位ms,默认1秒', 2, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(8, 'flushConsumeQueueLeastPages', '2', '最少凑够多少页内存才刷ConsumeQueue', null, 3, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(8, 'flushConsumeQueueThoroughInterval', '60000', '两次刷ConsumeQueue最大间隔，若超过，不校验页数直接刷', '单位ms,默认1分钟', 4, 1, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'transientStorePoolEnable', 'false', '是否启动堆外内存池加速写', null, 1, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'transientStorePoolSize', '5', '堆外内存池大小', null, 2, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'fastFailIfNoBufferInStorePool', 'false', '是否启用快速失败', null, 3, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'commitIntervalCommitLog', '200', '异步刷堆外内存时间间隔', '单位ms', 4, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'commitCommitLogLeastPages', '4', '最少凑够多少页内存才刷堆外内存', null, 5, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(9, 'commitCommitLogThoroughInterval', '200', '两次刷堆外内存最大间隔，若超过，不校验页数直接刷', '单位ms', 6, 1, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(10, 'haListenPort', '10912', 'master监听的HA端口', null, 1, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(10, 'haHousekeepingInterval', '20000', 'master与slave链接超时间隔', '单位ms,默认20秒', 2, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(10, 'haTransferBatchSize', '32768', 'master批量传输给slave数据大小', '单位字节,默认32K', 3, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(10, 'haSlaveFallbehindMax', '268435456', '同步双写master，判断slave落后大于多少为不可用', '单位字节,默认256M', 4, 1, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'fileReservedTime', '72', 'CommitLog保留的时间', '单位小时,默认3天', 1, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'deleteWhen', '04', 'CommitLog删除时间点，多个用;分隔', null, 2, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'destroyMapedFileIntervalForcibly', '120000', '删除文件时，若文件被占用，等待多久后强制删除', '单位ms,默认2分钟', 3, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'cleanFileForciblyEnable', 'true', '磁盘超过阈值、且无过期文件情况下, 是否强制删除文件', null, 4, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'diskMaxUsedSpaceRatio', '75', '磁盘最大使用阈值', null, 5, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'deleteCommitLogFilesInterval', '100', '删除CommitLog间隔，中间将sleep', '单位ms', 6, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'deleteConsumeQueueFilesInterval', '100', '删除ConsumeQueue间隔，中间将sleep', '单位ms', 7, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(11, 'redeleteHangedFileInterval', '120000', '重新删除已经执行过删除却未删掉的文件间隔', '单位ms,默认2分钟', 8, 1, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'maxMessageSize', '4194304', '单条消息最大大小', '单位字节,默认4M', 1, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'messageIndexEnable', 'true', '消息是否开启索引', null, 2, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'messageIndexSafe', 'false', '消息索引恢复时是否安全校验', null, 3, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'maxHashSlotNum', '5000000', '单个消息文件hash槽个数', '默认5百万', 4, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'maxIndexNum', '20000000', '单个消息文件hash槽个数', '默认2千万', 5, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(12, 'messageDelayLevel', '1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h', '延迟消息队列', null, 6, 0, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(13, 'slaveReadEnable', 'false', '拉取消息在硬盘时是否可以从slave拉取', '设置为true分担master压力', 1, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(13, 'longPollingEnable', 'true', '针对消费拉消息是否开启长轮询', null, 2, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(13, 'shortPollingTimeMills', '1000', '针对消费拉消息短轮询时间', '单位ms,默认1秒', 3, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(13, 'notifyConsumerIdsChangedEnable', 'true', '消费者上下线时是否通知客户端,以便再平衡', null, 4, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(13, 'transferMsgByHeap', 'true', '传输数据时是否使用零拷贝', '若消息量不大,基本都在pagecache,建议为false.否则消息在硬盘使用零拷贝会卡netty线程', 5, 1, 'true:是;false:否;', 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'accessMessageInMemoryMaxRatio', '40', '判断消息是否在内存的依据，此值仅为预估值，不准确', null, 1, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'maxTransferBytesOnMessageInMemory', '262144', '单次拉取内存消息传输的最大字节', '单位字节,默认256K', 2, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'maxTransferCountOnMessageInMemory', '32', '单次拉取内存消息传输的最大数量', null, 3, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'maxTransferBytesOnMessageInDisk', '65536', '单次拉取硬盘消息传输的最大字节', '单位字节,默认64K', 4, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'maxTransferCountOnMessageInDisk', '8', '单次拉取硬盘消息传输的最大数量', null, 5, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'maxMsgsNumBatch', '64', '按照key查询一次返回多少条消息（主要用于admin工具查询）', null, 6, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(14, 'defaultQueryMaxNum', '32', '按照msgId查询一次返回多少条消息（主要用于admin工具查询）', null, 7, 1, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(15, 'osPageCacheBusyTimeOutMills', '1000', '消息存储超过此时间，则将丢弃所有的写入请求', '单位ms,默认1秒', 1, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(15, 'brokerFastFailureEnable', 'true', '是否启用快速失败机制', null, 2, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(15, 'waitTimeMillsInPullQueue', '5000', '消息拉取请求超过阈值没有处理则返回失败', '单位ms,默认5秒', 4, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(15, 'waitTimeMillsInHeartbeatQueue', '31000', 'client心跳请求超过阈值没有处理则返回失败', '单位ms,默认31秒', 5, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(15, 'waitTimeMillsInTransactionQueue', '3000', '事务结束请求超过阈值没有处理则返回失败', '单位ms,默认3秒', 6, 1, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(16, 'startAcceptSendRequestTimeStamp', '0', 'broker启动多久后可以接受请求', '单位ms,默认0ms', 1, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(16, 'disableConsumeIfConsumerReadSlowly', 'false', '是否禁用消费慢的消费者', '启用slaveReadEnable代替此功能', 2, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(16, 'consumerFallbehindThreshold', '17179869184', '消费者拉取消息大小超过此值认为消费慢', '单位字节,默认16G', 3, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(16, 'diskFallRecorded', 'true', '记录消费者拉取消息大小', '不启用disableConsumeIfConsumerReadSlowly可以选否', 4, 1, 'true:是;false:否;', 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(17, 'compressedRegister', 'false', '向NameServer注册时数据是否压缩', 'topic过多可以开启', 1, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(17, 'forceRegister', 'true', '向NameServer注册时是否强制每次发送数据', 'topic过多可以关闭', 2, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(17, 'registerNameServerPeriod', '30000', '向NameServer注册周期', '单位ms,默认30秒', 3, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(17, 'registerBrokerTimeoutMills', '6000', '向NameServer注册时超时时间', '单位ms,默认6秒', 4, 1, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(18, 'rejectTransactionMessage', 'false', '是否拒绝发送事务消息', '非事务集群设置为true', 1, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(18, 'transactionTimeOut', '6000', '事务消息超过多久后首次检查', '单位ms,默认6秒', 2, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(18, 'transactionCheckMax', '15', '事务消息最大检查次数', null, 3, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(18, 'transactionCheckInterval', '60000', '事务消息检查间隔', '单位ms,默认60秒', 4, 1, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(19, 'haSendHeartbeatInterval', '5000', 'slave与master心跳间隔', '单位ms,默认5秒', 1, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(19, 'haMasterAddress', null, 'slave的HA master', '无需设置,向NameServer注册会返回master地址作为HA地址', 2, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(19, 'offsetCheckInSlave', 'false', '消费者从slave拉取消息offset不正确时，slave是否检查更正', null, 2, 1, 'true:是;false:否;', 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'filterServerNums', '0', '过滤服务数', null, 1, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'enableCalcFilterBitMap', 'false', '是否启用BitMap过滤计算', null, 2, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'bitMapLengthConsumeQueueExt', '112', 'BitMap大小', null, 3, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'expectConsumerNumUseFilter', '32', '预估的订阅同一topic的消费者数', null, 4, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'maxErrorRateOfBloomFilter', '20', 'bloom filter错误率', '单位%,默认20%', 5, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'filterDataCleanTimeSpan', '86400000', '清理n小时之前的filter数据', '单位ms,默认24小时', 6, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'enableConsumeQueueExt', 'false', '是否生成额外的consume queue文件', null, 7, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'mappedFileSizeConsumeQueueExt', '50331648', '额外的consume queue文件大小', '单位字节,默认48M', 8, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'filterSupportRetry', 'false', '是否支持过滤retry消费者', null, 9, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(20, 'enablePropertyFilter', 'false', '是否支持过滤SQL92', null, 10, 1, 'true:是;false:否;', 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverWorkerThreads', '8', 'worker线程', null, 1, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverCallbackExecutorThreads', '0', '默认公共线程', null, 2, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverSelectorThreads', '3', 'selector线程', null, 3, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverOnewaySemaphoreValue', '256', 'oneway信号量', null, 4, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverAsyncSemaphoreValue', '64', 'aysnc信号量', null, 5, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverChannelMaxIdleTimeSeconds', '120', 'idle最大时间', null, 6, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverSocketSndBufSize', '131072', 'SO_SNDBUF', null, 7, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverSocketRcvBufSize', '131072', 'SO_RCVBUF', null, 8, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'serverPooledByteBufAllocatorEnable', 'true', '是否开启bytebuffer池', null, 9, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(21, 'useEpollNativeSelector', 'false', '是否使用epoll', null, 10, 0, 'true:是;false:否;', 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientWorkerThreads', '4', 'worker线程', null, 1, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientCallbackExecutorThreads', '核数', '默认公共线程', null, 2, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientOnewaySemaphoreValue', '65535', 'oneway信号量', null, 3, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientAsyncSemaphoreValue', '65535', 'aysnc信号量', null, 4, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'channelNotActiveInterval', '60000', '此项作废', null, 5, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientChannelMaxIdleTimeSeconds', '120', '链接最大idle时间', null, 6, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'connectTimeoutMillis', '3000', '连接超时时间', null, 7, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientSocketSndBufSize', '131072', 'SO_SNDBUF', null, 8, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientSocketRcvBufSize', '131072', 'SO_RCVBUF', null, 9, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientPooledByteBufAllocatorEnable', 'false', '是否开启bytebuffer池', null, 10, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'clientCloseSocketIfTimeout', 'false', '超时是否关闭连接', null, 11, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(22, 'useTLS', 'false', '是否使用ssl', null, 12, 0, 'true:是;false:否;', 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(23, 'storeReplyMessageEnable', 'true', '是否启用rpc消息', null, 1, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(23, 'processReplyMessageThreadPoolNums', '16+核数*2', '处理rpc消息线程池大小', null, 2, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(23, 'replyThreadPoolQueueCapacity', '10000', '处理rpc消息的线程池队列大小', null, 3, 0, null, 0);

INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'traceOn', 'true', '是否启用trace', null, 1, 1, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'aclEnable', 'false', '是否启用权限校验,若启用需要配置权限文件', null, 2, 0, 'true:是;false:否;', 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'messageStorePlugIn', null, '消息存储插件', null, 3, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'checkCRCOnRecover', 'true', 'load完消息校验消息是否用CRC32', null, 4, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'debugLockEnable', 'false', 'commitlog写入超过1秒打印堆栈', null, 5, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'cleanResourceInterval', '10000', 'CommitLog&ConsumeQueue清除任务执行间隔', '单位ms,默认10秒', 6, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'flushConsumerOffsetInterval', '5000', 'consumerOffset.json持久化间隔', '单位ms,默认5秒', 7, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'flushConsumerOffsetHistoryInterval', '60000', '此项作废,以flushConsumerOffsetInterval为准', null, 8, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'flushDelayOffsetInterval', '10000', 'delayOffset.json持久化间隔', '单位ms,默认10秒', 9, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'abortFile', null, '自动以storePathRootDir拼装', '无需设置', 10, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'storePathIndex', null, '自动以storePathRootDir拼装', '无需设置', 11, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'storeCheckpoint', null, '自动以storePathRootDir拼装', '无需设置', 12, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'highSpeedMode', 'false', '此项作废', null, 14, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'commercialEnable', 'true', '此项作废', null, 15, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'commercialTimerCount', '1', '此项作废', null, 16, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'commercialTransCount', '1', '此项作废', null, 17, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'commercialBigCount', '1', '此项作废', null, 18, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'commercialBaseCount', '1', '此项作废', null, 19, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'putMsgIndexHightWater', '600000', '此项作废', null, 20, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'maxDelayTime', '40', '此项作废', null, 21, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'regionId', 'DefaultRegion', 'broker区域，trace使用', null, 22, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'duplicationEnable', 'false', '是否支持重写consume queue', null, 23, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'enableDLegerCommitLog', 'false', '是否支持DLeger', null, 24, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'dLegerGroup', null, 'DLeger相关配置', null, 25, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'dLegerPeers', null, 'DLeger相关配置', null, 26, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'dLegerSelfId', null, 'DLeger相关配置', null, 27, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'enableScheduleMessageStats', 'true', '开启schedule队列消息统计', null, 28, 1, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'isEnableBatchPush', 'false', 'DLedger批量复制', null, 29, 0, null, 0);
INSERT IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'autoDeleteUnusedStats', 'false', '删除topic或订阅时一起删除相关统计', null, 30, 1, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'registerBroker', 'true', '是否注册broker', null, 31, 1, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsExporterType', 'DISABLE', '指标输出类型', null, 32, 0, 'DISABLE:DISABLE;OTLP_GRPC:OTLP_GRPC;PROM:PROM;LOG:LOG;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsGrpcExporterTarget', null, 'OTLP_GRPC输出地址', null, 33, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsGrpcExporterHeader', null, 'OTLP_GRPC输出header', null, 34, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricGrpcExporterTimeOutInMills', '3000', 'OTLP_GRPC输出超时时间', '默认3秒，单位ms', 35, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricGrpcExporterIntervalInMills', '60000', 'OTLP_GRPC输出间隔', '默认一分钟，单位ms', 36, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricLoggingExporterIntervalInMills', '10000', '指标日志输出间隔', '默认10秒，单位ms', 37, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsPromExporterPort', '5557', 'PROM指标输出端口', null, 38, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsPromExporterHost', null, 'PROM指标输出ip', null, 39, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'metricsLabel', null, '指标标签', '格式:name1:label1,name2:label2', 40, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(24, 'validateSystemTopicWhenUpdateTopic', 'false', '新建topic是否校验是不是系统topic', null, 41, 1, null, 1);

insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'enableControllerMode', 'false', '是否启用controller模式', null, 1, 0, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'controllerAddr', null, 'controller的地址', 'ip:port;ip:port;或域名:port', 2, 0, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'fetchControllerAddrByDnsLookup', 'false', '是否使用域名获取controller的地址', 'controllerAddr填写域名时请启用该选项', 3, 0, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'controllerHeartBeatTimeoutMills', '10000', 'broker和controller之间心跳超时时间', '单位ms,默认10秒', 4, 1, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'syncBrokerMetadataPeriod', '5000', '向controller同步broker副本信息的时间间隔', '单位ms,默认5秒', 5, 1, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'checkSyncStateSetPeriod', '5000', '检查SyncStateSet的时间间隔', '单位ms,默认5秒', 6, 1, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'syncControllerMetadataPeriod', '10000', '同步controller元数据的时间间隔，主要是获取active controller的地址', '单位ms,默认10秒', 7, 1, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'haMaxTimeSlaveNotCatchup', '15000', 'slave没有跟上Master的最大时间间隔，若在SyncStateSet中的slave超过该时间间隔会将其从SyncStateSet移除', '单位ms,默认15秒', 8, 1, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'storePathEpochFile', null, '存储epoch文件的位置', '默认在store目录下', 9, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'allAckInSyncStateSet', 'false', '一条消息需要复制到SyncStateSet中的每一个副本才会向客户端返回成功，可以保证消息不丢失', null, 10, 1, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'syncFromLastFile', 'false', '若slave是空盘启动，是否从最后一个文件进行复制', null, 11, 1, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'asyncLearner', 'false', '异步复制的learner', '为true时不参与选举', 12, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'inSyncReplicas', '1', '需保持同步的副本组数量', 'allAckInSyncStateSet=true时该参数无效', 13, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(25, 'minInSyncReplicas', '1', '最小需保持同步的副本组数量', '若SyncStateSet中副本个数小于minInSyncReplicas则putMessage直接返回PutMessageStatus.IN_SYNC_REPLICAS_NOT_ENOUGH', 14, 0, null, 0);

insert IGNORE into broker_config_group(`id`, `group`, `order`) values(26, '压缩topic配置', 26);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(26, 'compactionMappedFileSize', '104857600', 'CompactinLog文件大小', '默认100m', 1, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(26, 'compactionThreadNum', '6', '压缩线程数', null, 2, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(26, 'compactionScheduleInternal', '900000', '压缩间隔', null, 3, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(26, 'compactionCqMappedFileSize', '10485760', 'CompactinConsumeQueue文件大小', '默认10m', 4, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(26, 'enableCompaction', 'true', '是否启用压缩', null, 5, 0, 'true:是;false:否;', 0);

insert IGNORE into broker_config_group(`id`, `group`, `order`) values(27, '时间轮延迟消息', 27);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'mappedFileSizeTimerLog', '104857600', 'TimerLog文件大小', '默认100m', 1, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerPrecisionMs', '1000', '时间轮精度', '单位毫秒', 2, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerRollWindowSlot', '172800', '超长延迟消息滚动窗口', '默认超过2天会滚动，单位ms', 3, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerFlushIntervalMs', '1000', '延迟消息刷新间隔', '默认1秒，单位ms', 4, 1, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerGetMessageThreadNum', '3', '出dequeueGetQueue从commitLog拉取消息，放入dequeuePutQueue的线程数', null, 5, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerPutMessageThreadNum', '3', '出dequeuePutQueue放入原始队列线程数', null, 6, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerEnableDisruptor', 'false', '是否使用Disruptor', null, 7, 0, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerEnableCheckMetrics', 'true', '是否检查指标', null, 8, 0, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerCheckMetricsWhen', '05', '指标检查时间', null, 9, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerMaxDelaySec', '259200', '延迟消息最大延迟', '默认3天，单位s', 10, 1, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerWheelEnable', 'true', '是否启用时间轮延迟消息', null, 11, 0, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerStopEnqueue', 'false', '是否停止写入enqueuePutQueue', null, 12, 1, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerStopDequeue', 'false', '是否停止写入dequeueGetQueue', null, 13, 1, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerSkipUnknownError', 'false', '发生未知错误时是否跳过', null, 14, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerCongestNumEachSlot', '2147483647', '每个槽最大消息量', null, 15, 1, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerMetricSmallThreshold', '1000000', '指标小阈值', null, 16, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerProgressLogIntervalMs', '10000', '延迟消息处理日志打印间隔', '默认10秒，单位ms', 17, 1, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerWarmEnable', 'false', '已废弃', null, 18, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(27, 'timerInterceptDelayLevel', 'false', '已废弃', null, 19, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'enableSlaveActingMaster', 'false', '是否启用备代主', null, 1, 0, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'enableRemoteEscape', 'false', '是否允许二级消息远程逃逸', null, 2, 0, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'totalReplicas', '1', 'broker副本总个数（包括master）', null, 3, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'brokerHeartbeatInterval', '1000', 'broker向ns发送的轻量心跳的时间间隔', '默认1秒，单位ms', 4, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'brokerNotActiveTimeoutMillis', '10000', '超时ns未收到心跳认为broker下线', '默认10秒，单位ms', 5, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'sendHeartbeatTimeoutMillis', '1000', 'broker向ns发送的轻量心跳的超时时间，单位毫秒', '默认1秒，单位ms', 6, 0, null, 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'skipPreOnline', 'false', 'master重启是否跳过预上线流程', '预上线流程用于master启动后先不注册到ns，从代理备同步元数据后再注册', 7, 0, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'compatibleWithOldNameSrv', 'true', '是否兼容旧的ns', null, 8, 0, 'true:是;false:否;', 0);
insert IGNORE into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(28, 'lockInStrictMode', 'false', 'lockInStrictMode', null, 9, 0, 'true:是;false:否;', 0);

-- ----------------------------
-- topic_traffic_warn_config init
-- ----------------------------
INSERT IGNORE INTO `topic_traffic_warn_config`(avg_multiplier,avg_max_percentage_increase,max_max_percentage_increase,alarm_receiver,min_warn_count) VALUES (5, 200, 30, 0, 100);

INSERT IGNORE INTO `broker_config`(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) VALUES (1, 'physicalMemorySize', '0', '物理内存(单位字节)，0代表使用全部物理内存', '部署到docker或多个broker部署到一台机器时使用', 18, 1, NULL, 0);
insert IGNORE into `broker_config`(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'grpcServerPort', '8081', 'proxy grpc协议端口', null, 19, 0, null, 0);
insert IGNORE into `broker_config`(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) values(1, 'remotingListenPort', '8080', 'proxy remoting协议端口', null, 20, 0, null, 0);