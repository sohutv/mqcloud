package com.sohu.tv.mq.cloud.util;

/**
 * 返回状态
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月12日
 */
public enum Status {
    // 2xx代表正常返回
    OK(200, "OK"), 
    NO_RESULT(201, "暂无数据"),
    DELETE_ERR_CONSUMER_EXIST_RESULT(203, "请先删除消费者"),
    DELETE_ERR_PRODUCER_EXIST_RESULT(204, "请先删除生产者"),
    DATA_NO_CHANGE(205, "数据没有改变，请重新修改"),
    DELETE_ERR_USER_PRODUCER_EXIST_RESULT(206, "存在待审核的用户——生产者关联，请联系管理员"),
    DELETE_ERR_USER_CONSUMER_EXIST_RESULT(207, "存在待审核的用户——消费者关联，请联系管理员"),
    
    FILTERED_TOPIC(208, "被过滤"),
    NO_CONFIG_TOPIC(209, "查无配置"),
    NO_ONLINE(210, "消费者不在线"),
    
    // 3xx代表参数问题
    PARAM_ERROR(300, "参数错误"),
    NOLOGIN_ERROR(301, "nologin"),
    PERMISSION_DENIED_ERROR(303, "permission denied"),
    NOT_INIT_IP(304, "请先执行初始化脚本"),
    REPEAT_ERROR(305, "关联关系已存在"),
    LONGIN_ERROR(306, "用户名或密码错误"),
    OLD_PASSWORD_ERROR(307, "原始密码输入有误"),
    NO_NEED_MODIFY_ERROR(308, "无需修改"),
    
    // 4xx代表请求问题
    NOT_FOUND_ERROR(404, "请求不存在"),
    
    // 5xx代表外部依赖异常
    DB_ERROR(500, "数据库异常"),
    DB_DUPLICATE_KEY(501, "数据重复"),
    DB_UPDATE_ERR_TOPIC_OK(502, "TOPIC创建成功,但是审核记录更新失败"),
    DB_UPDATE_ERR_CONSUME_OK(503, "消费者创建成功,但是审核记录更新失败"),
    DB_UPDATE_ERR_ASSOCIATE_CONSUME_OK(504, "消费者关联成功,但是审核记录更新失败"),
    DB_UPDATE_ERR_ASSOCIATE_PRODUCER_OK(505, "生产者关联成功,但是审核记录更新失败"),
    DB_UPDATE_ERR_DELETE_TOPIC_OK(506, "topic创建成功,但是审核记录更新失败"),
    DB_UPDATE_ERR_DELETE_CONSUMER_OK(507, "consumer删除成功,但是审核记录更新失败"),
    DB_UPDATE_ERR_DELETE_RESET_OFFSET_OK(508, "重置成功,但是审核记录更新失败"),
    DB_UPDATE_ERR_DELETE_TOPIC_UPDATE_OK(509, "topic更新成功,但是审核记录更新失败"),
    DB_UPDATE_ERR_USER_UPDATE_OK(510, "用户信息更新成功,但是审核记录更新失败"),
    DB_UPDATE_ERR_DELETE_USERPRODUCER_OK(511, "userProducer删除成功,但是审核记录更新失败"),
    EMAIL_SEND_ERR(512, "数据操作成功，发送审核邮件失败"),
    DB_UPDATE_ERR_DELETE_USERCONSUMER_OK(513, "userConsumer删除成功,但是审核记录更新失败"),
    AUDIT_RECORD_REPEAT(514, "已提交该审核记录，不可重复提交，请耐心等待"),
    AUDIT_MESSAGE_CANNOT_AUTID_WHEN_NOT_SEND_OK(515, "消息还未全部发送成功，不可审批"),
    AUDIT_MESSAGE_NOT_SEND_OK(516, "消息未发送成功"),
    DELETE_BROKER_CONFIG_FIRST(517, "请先删除配置组下的配置"),
    DB_UPDATE_ERR_UPDATE_CONSUMER_CONFIG_OK(518, "更新消费者配置成功,但是审核记录更新失败"),
    DB_UPDATE_ERR_CONSUME_TIMESPAN_MESSAGE_OK(519, "重新消费成功,但是审核记录更新失败"),
    REQUEST_ERROR(520, "请求异常"),
    BROKER_UNSUPPORTED_ERROR(521, "broker暂不支持"),
    BROKER_NOT_EXIST_ERROR(522, "master不存在"),
    CONSUMER_CONNECTION_EXIST_ERROR(523, "此消费者还存在链接"),
    ROUTE_EXIST_ERROR(524, "路由已存在"),
    ROUTE_NOT_EXIST_ERROR(525, "路由不存在"),
    
    // 6xx代表web请求异常
    WEB_ERROR(600, "请求错误"),
    WEB_ERROR_PAGE(601, "出错了，请联系管理员"),
    // 7XX用于数据校验时返回
    TOPIC_REPEAT(700, "与其他topic名称重复"),
    PRODUCER_REPEAT(701, "与其他生产者名称重复"),
    CONSUMER_REPEAT(702, "与其他消费者名称重复"),
    CONSUMER_TRACE_OPEN(703, "请先关闭消费者的trace"),
    AUDITED(704, "已审核完毕"),
    
    // 8XX用于创建Trace topic时详细描述异常信息
    TRACE_CLUSTER_ID_IS_NULL(800, "Trace集群ID为空"),
    TRACE_CLUSTER_IS_NULL(801, "获取Trace集群失败"),
    TRACE_TOPIC_CREATE_ERROR(802, "创建Trace topic失败"),
    TOPIC_CREATE_OK_BUT_TRACE_TOPIC_CREATE_ERROR(803, "topic创建成功，但是Trace topic创建失败"),
    ;

    private int key;
    private String value;

    private Status(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
