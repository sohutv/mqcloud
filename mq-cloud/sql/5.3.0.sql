alter table `name_server` add column `status` tinyint(4) default 0 comment '状态:0:正常,1:流量剔除';
alter table `proxy` add column `status` tinyint(4) default 0 comment '状态:0:正常,1:流量剔除';
alter table `producer_total_stat` add column `ip` varchar(100) default NULL comment 'ip';
create index date_ip on producer_total_stat (`create_date`,`ip`);
insert into `common_config`(`key`, `value`, `comment`) values ('mqcloudServers', '["127.0.0.1"]', 'mqcloud的server列表');
alter table `cluster` add column `status` tinyint(4) default 0 comment '状态:0:正常,1:更新中';