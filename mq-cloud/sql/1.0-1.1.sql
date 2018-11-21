-- ----------------------------
-- update for user password init for 1.1.RELEASE
-- ----------------------------
alter table user modify column `password` varchar(256) COMMENT '登录方式采用用户名密码验证时使用';
update user set `password` = '21232f297a57a5a743894a0e4a801fc3' where email = 'admin@admin.com';
delete from `common_config` where `key` in ('nexusDomain','alertClass','loginClass','ticketKey','clientArtifactId','producerClass','consumerClass');