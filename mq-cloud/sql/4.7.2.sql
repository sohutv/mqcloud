alter table `name_server` add column `base_dir` varchar(360) DEFAULT '/opt/mqcloud/ns' COMMENT '安装路径';
alter table `broker` add column `base_dir` varchar(360) DEFAULT NULL COMMENT '安装路径';