-- ----------------------------
-- Table structure for `audit_resend_message`
-- ----------------------------
DROP TABLE IF EXISTS `audit_resend_message`;
CREATE TABLE `audit_resend_message` (
  `aid` int(11) NOT NULL COMMENT '审核id',
  `tid` int(11) NOT NULL COMMENT 'topic id',
  `msgId` char(32) NOT NULL COMMENT 'broker offset msg id',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '申请类型:0:未处理,1:发送成功,2:发送失败',
  `times` int(11) NOT NULL DEFAULT '0' COMMENT '发送次数',
  `send_time` datetime COMMENT '发送时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息重发审核表';