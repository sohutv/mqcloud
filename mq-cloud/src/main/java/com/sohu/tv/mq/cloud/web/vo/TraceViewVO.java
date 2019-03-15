package com.sohu.tv.mq.cloud.web.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.cloud.bo.TraceMessageDetail;
import com.sohu.tv.mq.cloud.util.DateUtil;

/**
 * trace view vo
 * 
 * @author yongfeigao
 * @date 2019年2月25日
 */
public class TraceViewVO {
    // 表示肯定
    public static int YES = 1;

    private ViewVO producer;

    private String broker;

    // consumer 可能会有多个, 同一个consumer的requestId相同
    private Map<String/* requestId */, ViewVO> consumer;

    public static class ViewVO {

        private String addr;

        private Boolean success;

        private String time;

        private List<TraceMessageDetail> detail;

        private String group;

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public List<TraceMessageDetail> getDetail() {
            return detail;
        }

        public void setDetail(List<TraceMessageDetail> detail) {
            this.detail = detail;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String status() {
            if (success == null) {
                return "未知";
            }
            return success ? "成功" : "失败";
        }

        @Override
        public String toString() {
            return "ViewVO [addr=" + addr + ", success=" + success + ", time=" + time + ", group=" + group + "]";
        }
    }

    public ViewVO getProducer() {
        return producer;
    }

    public void setProducer(ViewVO producer) {
        this.producer = producer;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public Map<String, ViewVO> getConsumer() {
        return consumer;
    }

    public void setConsumer(Map<String, ViewVO> consumer) {
        this.consumer = consumer;
    }

    /**
     * 生产者详情转json
     * 
     * @return
     */
    public String producerToJsonString() {
        if (getProducer() == null || getProducer().getDetail() == null) {
            return "{}";
        }
        return JSON.toJSONString(getProducer().getDetail());
    }

    /**
     * 消费者详情转json
     * 
     * @return
     */
    public String consumerToJsonString() {
        if (getConsumer() == null || getConsumer().isEmpty()) {
            return "{}";
        }
        List<List<TraceMessageDetail>> messageList = new ArrayList<List<TraceMessageDetail>>();
        for (ViewVO ConsumerViewVO : getConsumer().values()) {
            if (ConsumerViewVO.getDetail() != null) {
                messageList.add(ConsumerViewVO.getDetail());
            }
        }
        return JSON.toJSONString(messageList);
    }

    public void buildProducer(String addr, Boolean isSuccess, long time, TraceMessageDetail detail,
            String producerGroup) {
        ViewVO viewVo = new ViewVO();
        buildViewVo(viewVo, addr, isSuccess, time, detail, producerGroup);
        setProducer(viewVo);
    }

    /**
     * 构建消费者
     * 
     * @param addr
     * @param isSuccess
     * @param time
     * @param consumerStatus
     * @param detail
     */
    public void buildConsumer(String addr, Boolean isSuccess, long time,
            TraceMessageDetail detail, String requestId, String consumerGroup) {
        Map<String, ViewVO> consumer = getConsumer();
        if (consumer == null) {
            consumer = new HashMap<String, ViewVO>();
            setConsumer(consumer);
        }
        ViewVO viewVo = consumer.get(requestId);
        if (viewVo == null) {
            viewVo = new ViewVO();
            consumer.put(requestId, viewVo);
        }
        buildViewVo(viewVo, addr, isSuccess, detail, consumerGroup);
        if (time > 0) {
            viewVo.setTime(DateUtil.getFormat(DateUtil.YMD_DASH_HMS_COLON_DOT_SSS).format(new Date(time)));
        }
    }

    private void buildViewVo(ViewVO viewVo, String addr, Boolean isSuccess, TraceMessageDetail detail, String group) {
        if (viewVo.getAddr() == null) {
            viewVo.setAddr(addr);
        }
        if (viewVo.getGroup() == null || "".equals(viewVo.getGroup())) {
            viewVo.setGroup(group);
        }
        if (viewVo.getSuccess() == null) {
            viewVo.setSuccess(isSuccess);
        }
        List<TraceMessageDetail> traceMessageDetailList = viewVo.getDetail();
        if (traceMessageDetailList == null) {
            traceMessageDetailList = new ArrayList<TraceMessageDetail>();
        }
        traceMessageDetailList.add(detail);
        viewVo.setDetail(traceMessageDetailList);
    }

    private void buildViewVo(ViewVO viewVo, String addr, Boolean isSuccess, long time, TraceMessageDetail detail,
            String group) {
        buildViewVo(viewVo, addr, isSuccess, detail, group);
        if (viewVo.getTime() == null) {
            viewVo.setTime(DateUtil.getFormat(DateUtil.YMD_DASH_HMS_COLON_DOT_SSS).format(new Date(time)));
        }
    }

    @Override
    public String toString() {
        return "TraceViewVO [producer=" + producer + ", broker=" + broker + ", consumer=" + consumer + "]";
    }
}
