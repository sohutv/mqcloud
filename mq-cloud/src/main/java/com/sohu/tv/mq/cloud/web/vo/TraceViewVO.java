package com.sohu.tv.mq.cloud.web.vo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sohu.tv.mq.util.JSONUtil;
import org.apache.rocketmq.client.trace.TraceContext;
import org.apache.rocketmq.client.trace.TraceType;

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
    // 耗时计算保留两位
    public static DecimalFormat df = new DecimalFormat("0.00");
    // 一秒
    public static int ONE_SECOND = 1000;
    // 一分钟
    public static int ONE_MINUTE = 60 * ONE_SECOND;
    // 一小时
    public static int ONE_HOUR = 60 * ONE_MINUTE;

    // 生产者请求视图
    private RequestViewVO producerRequestView;

    // 消费者请求视图
    private List<RequestViewVO> consumerRequestViewList;

    public static class RequestViewVO implements Comparable<RequestViewVO> {

        private String addr;

        private Boolean success;

        private List<TraceMessageDetail> detail;

        private String group;
        
        // 耗时 ms
        private Integer costTime;
        
        private String requestId;
        
        // 请求开始时间
        private Long requestStartTime;

        public Long getRequestStartTime() {
            return requestStartTime;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

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
            if(requestStartTime == null || requestStartTime <= 0) {
                return "";
            }
            return DateUtil.getFormat(DateUtil.YMD_DASH_HMS_COLON_DOT_SSS).format(new Date(requestStartTime));
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

        public Integer getCostTime() {
            return costTime;
        }

        public void setCostTime(Integer costTime) {
            this.costTime = costTime;
        }

        public void setRequestStartTime(Long requestStartTime) {
            this.requestStartTime = requestStartTime;
        }

        /**
         * 简单计算耗时，只保留两位小数
         */
        public String costTimes() {
            if (costTime == null) {
                return "未知";
            }
            if (costTime < ONE_SECOND) {
                return costTime + "ms";
            } else if (costTime < ONE_MINUTE) {
                return df.format((float)costTime/ONE_SECOND) + "s";
            } else if (costTime < ONE_HOUR) {
                return df.format((float)costTime/ONE_MINUTE) + "m";
            } else {
                return df.format((float)costTime/ONE_HOUR) + "h";
            }
        }

        @Override
        public int compareTo(RequestViewVO o) {
            int rst = group.compareTo(o.getGroup());
            if(rst == 0) {
                if(requestStartTime != null && o.requestStartTime != null) {
                    return (int)(requestStartTime - o.requestStartTime);
                }
            }
            return rst;
        }
    }

    public RequestViewVO getProducerRequestView() {
        return producerRequestView;
    }

    public List<RequestViewVO> getConsumerRequestViewList() {
        return consumerRequestViewList;
    }

    public void setConsumerRequestViewList(List<RequestViewVO> consumerRequestViewList) {
        this.consumerRequestViewList = consumerRequestViewList;
    }

    /**
     * 生产者详情转json
     * 
     * @return
     */
    public String producerToJsonString() {
        if (producerRequestView == null || producerRequestView.getDetail() == null) {
            return "{}";
        }
        return JSONUtil.toJSONString(producerRequestView.getDetail());
    }

    /**
     * 消费者详情转json
     * 
     * @return
     */
    public String consumerToJsonString() {
        if (consumerRequestViewList == null || consumerRequestViewList.isEmpty()) {
            return "{}";
        }
        List<List<TraceMessageDetail>> messageList = new ArrayList<List<TraceMessageDetail>>();
        for (RequestViewVO consumerViewVO : consumerRequestViewList) {
            if (consumerViewVO.getDetail() != null) {
                messageList.add(consumerViewVO.getDetail());
            }
        }
        return JSONUtil.toJSONString(messageList);
    }

    public void buildProducer(TraceMessageDetail traceMessageDetail, TraceContext traceContext) {
        producerRequestView = new RequestViewVO();
        producerRequestView.setAddr(traceMessageDetail.getClientHost());
        List<TraceMessageDetail> traceMessageDetailList = new ArrayList<>();
        traceMessageDetailList.add(traceMessageDetail);
        producerRequestView.setDetail(traceMessageDetailList);
        buildRequestViewVo(producerRequestView, traceContext);
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
    public void buildConsumer(TraceMessageDetail traceMessageDetail, TraceContext traceContext) {
        if(consumerRequestViewList == null) {
            consumerRequestViewList = new ArrayList<>();
        }
        RequestViewVO requestViewVO = findRequestViewVO(traceContext.getRequestId());
        if (requestViewVO == null) {
            requestViewVO = new RequestViewVO();
            requestViewVO.setAddr(traceMessageDetail.getClientHost());
            consumerRequestViewList.add(requestViewVO);
        }
        if(requestViewVO.getDetail() == null) {
            List<TraceMessageDetail> traceMessageDetailList = new ArrayList<>();
            requestViewVO.setDetail(traceMessageDetailList);
        }
        requestViewVO.getDetail().add(traceMessageDetail);
        
        buildRequestViewVo(requestViewVO, traceContext);
    }
    
    private RequestViewVO findRequestViewVO(String requestId) {
        for(RequestViewVO requestViewVO : consumerRequestViewList) {
            if(requestViewVO.getRequestId().equals(requestId)) {
                return requestViewVO;
            }
        }
        return null;
    }
    
    private void buildRequestViewVo(RequestViewVO requestViewVO, TraceContext traceContext) {
        if (requestViewVO.getGroup() == null || "".equals(requestViewVO.getGroup())) {
            requestViewVO.setGroup(traceContext.getGroupName());
        }
        if (requestViewVO.getSuccess() == null && traceContext.getTraceType() != TraceType.SubBefore) {
            requestViewVO.setSuccess(traceContext.isSuccess());
        }
        if (requestViewVO.getCostTime() == null && traceContext.getTraceType() != TraceType.SubBefore) {
            requestViewVO.setCostTime(traceContext.getCostTime());
        }
        if (requestViewVO.getRequestStartTime() == null) {
            requestViewVO.setRequestStartTime(traceContext.getTimeStamp());
        }
        if (requestViewVO.getRequestId() == null) {
            requestViewVO.setRequestId(traceContext.getRequestId());
        }
    }
}
