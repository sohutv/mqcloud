alter table common_config modify column `value` varchar(65535) COMMENT '配置值';
INSERT INTO `common_config`(`key`, `comment`) VALUES ('privateKey', '私钥');