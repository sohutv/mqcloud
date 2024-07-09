alter table `topic_traffic_warn_config` add column `min_warn_count` bigint(20) DEFAULT NULL COMMENT '最小预警数量';
update `topic_traffic_warn_config` set `min_warn_count` = 100 where topic = '';