package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description
 * @date 2023/7/5 16:59:15
 */
public class WheelMsgCancelParam {

    @NotNull
    private Integer tid;

    @NotBlank
    private String dataIndexs;

    @NotBlank
    private String uniqueIds;

    @NotBlank
    private String offsetMsgIds;

    @NotBlank
    private String deliverTimes;

    private List<String> uniqueIdList;

    private List<String> offsetMsgIdList;

    private List<String> deliverTimeList;

    public WheelMsgCancelParam() {
    }

    public String getDataIndexs() {
        return dataIndexs;
    }

    public void setDataIndexs(String dataIndexs) {
        this.dataIndexs = dataIndexs;
    }

    public Integer getTid() {
        return tid;
    }

    public void setTid(Integer tid) {
        this.tid = tid;
    }

    public String getUniqueIds() {
        return uniqueIds;
    }

    public void setUniqueIds(String uniqueIds) {
        this.uniqueIds = uniqueIds;
    }

    public String getOffsetMsgIds() {
        return offsetMsgIds;
    }

    public void setOffsetMsgIds(String offsetMsgIds) {
        this.offsetMsgIds = offsetMsgIds;
    }

    public String getDeliverTimes() {
        return deliverTimes;
    }

    public void setDeliverTimes(String deliverTimes) {
        this.deliverTimes = deliverTimes;
    }

    public List<String> getUniqueIdList() {
        return uniqueIdList;
    }

    public List<String> getOffsetMsgIdList() {
        return offsetMsgIdList;
    }

    public List<String> getDeliverTimeList() {
        return deliverTimeList;
    }

    public List<SingleWheelMsgCancelParam> getSingleWheelMsgCancelParamList() {
        uniqueIdList = Arrays.asList(uniqueIds.split(","));
        offsetMsgIdList = Arrays.asList(offsetMsgIds.split(","));
        deliverTimeList = Arrays.asList(deliverTimes.split(","));
        if (uniqueIdList.size() != offsetMsgIdList.size() || uniqueIdList.size() != deliverTimeList.size()) {
            return null;
        }
        List<SingleWheelMsgCancelParam> singleWheelMsgCancelParamList = new ArrayList<>();
        for (int i = 0; i < uniqueIdList.size(); i++) {
            SingleWheelMsgCancelParam singleWheelMsgCancelParam = new SingleWheelMsgCancelParam();
            singleWheelMsgCancelParam.setTid(tid);
            singleWheelMsgCancelParam.setUniqueId(uniqueIdList.get(i));
            singleWheelMsgCancelParam.setOffsetMsgId(offsetMsgIdList.get(i));
            singleWheelMsgCancelParam.setDeliverTime(deliverTimeList.get(i));
            singleWheelMsgCancelParamList.add(singleWheelMsgCancelParam);
        }
        return singleWheelMsgCancelParamList;
    }

    public List<String> getExistDataIndexList(List<String> existUniqueIdList) {
        List<String> dataIndexList = Arrays.asList(dataIndexs.split(","));
        List<String> result = new ArrayList<>();
        for (String u : existUniqueIdList) {
            int index = uniqueIdList.indexOf(u);
            if (index != -1) {
                result.add(dataIndexList.get(index));
            }
        }
        return result;
    }

    public class SingleWheelMsgCancelParam {

        private Integer tid;
        private String uniqueId;
        private String offsetMsgId;
        private String deliverTime;

        public SingleWheelMsgCancelParam() {
        }

        public Integer getTid() {
            return tid;
        }

        public void setTid(Integer tid) {
            this.tid = tid;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        public String getOffsetMsgId() {
            return offsetMsgId;
        }

        public void setOffsetMsgId(String offsetMsgId) {
            this.offsetMsgId = offsetMsgId;
        }

        public String getDeliverTime() {
            return deliverTime;
        }

        public void setDeliverTime(String deliverTime) {
            this.deliverTime = deliverTime;
        }
    }
}
