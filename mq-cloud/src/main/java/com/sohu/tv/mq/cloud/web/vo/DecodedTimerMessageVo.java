package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.DecodedMessage;
import com.sohu.tv.mq.cloud.service.MessageService;

import java.text.SimpleDateFormat;

import static com.sohu.tv.mq.cloud.web.vo.DecodedTimerMessageVo.MessageDeliveryType.*;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description 解码消息Vo
 * @date 2023/7/31 18:34:27
 */
public class DecodedTimerMessageVo extends DecodedMessage {

    private int timerStatus;

    private String timerStatusDesc;

    private String timerDeliverTimeDesc;

    private boolean isCanceled;

    private boolean showSysFlag;

    public DecodedTimerMessageVo() {
    }

    public DecodedTimerMessageVo(DecodedMessage decodedMessage) {
        setOffsetMsgId(decodedMessage.getOffsetMsgId());
        setMsgId(decodedMessage.getMsgId());
        setTimerDeliverTime(decodedMessage.getTimerDeliverTime());
        setStoreTimestamp(decodedMessage.getStoreTimestamp());
        setBornTimestamp(decodedMessage.getBornTimestamp());
        setQueueOffset(decodedMessage.getQueueOffset());
        setMessageBodySerializer(decodedMessage.getMessageBodySerializer());
        setMsgLength(decodedMessage.getMsgLength());
        setMessageBodyType(decodedMessage.getMessageBodyType());
        setKeys(decodedMessage.getKeys());
        setTags(decodedMessage.getTags());
        setTimerRollTimes(decodedMessage.getTimerRollTimes());
        setBornHost(decodedMessage.getBornHost());
        setDecodedBody(decodedMessage.getDecodedBody());
        setQueueId(decodedMessage.getQueueId());
        setStoreHost(decodedMessage.getStoreHost());
        setBroker(decodedMessage.getBroker());
    }

    public int getTimerStatus() {
        return timerStatus;
    }

    public void setTimerStatus(int timerStatus) {
        this.timerStatus = timerStatus;
    }

    public String getTimerStatusDesc() {
        return timerStatusDesc;
    }

    public void setTimerStatusDesc(String timerStatusDesc) {
        this.timerStatusDesc = timerStatusDesc;
    }

    public String getTimerDeliverTimeDesc() {
        return timerDeliverTimeDesc;
    }

    public void setTimerDeliverTimeDesc(String timerDeliverTimeDesc) {
        this.timerDeliverTimeDesc = timerDeliverTimeDesc;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    public boolean isShowSysFlag() {
        return showSysFlag;
    }

    public void setShowSysFlag(boolean showSysFlag) {
        this.showSysFlag = showSysFlag;
    }

    public void initTimerDeliverTimeDesc(boolean showFlag, boolean isCancelMessage, boolean forceRoll) {
        setCanceled(isCancelMessage);
        setShowSysFlag(showFlag);
        if (showFlag) {
            buildAllTimerDesc(forceRoll);
        } else {
            buildUserTimerDesc();
        }
    }

    /***
     * @description 展示管理员侧的话术 包含全部消息
     * @param forceRoll
     * @return void
     * @author fengwang219475
     * @date 2023/8/4 09:56:31
     */
    private void buildAllTimerDesc(boolean forceRoll) {
        boolean isCancelMessage = isSysCancelMessage();
        StringBuilder sb = new StringBuilder();
        sb.append("当前状态：").append(getAllTimerCurrentStatus(isCancelMessage, isCanceled, forceRoll));
        sb.append("</br>");

        long timerDeliverTime = getTimerDeliverTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(TIMER_ROLLED.getType() == getTimerStatus()) {
            sb.append("滚动时间：").append(sdf.format(getStoreTimestamp()));
        } else {
            if (!isDeliverTimeUp()) {
                sb.append("预计");
            }
            if (isCanceled) {
                sb.append("原始投递时间：").append(sdf.format(timerDeliverTime));
            } else if (isCancelMessage){
                sb.append("生效时间：").append(sdf.format(timerDeliverTime));
            } else {
                sb.append("投递时间：").append(sdf.format(timerDeliverTime));
            }
        }
        sb.append("</br>");
        sb.append("已滚动次数：").append(getTimerRollTimes());
        this.timerDeliverTimeDesc = sb.toString();
    }

    /***
     * @description 构建用户视角的话术
     * @return void
     * @author fengwang219475
     * @date 2023/8/4 09:56:04
     */
    private void buildUserTimerDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("当前状态：").append(getUserTimerCurrentStatus(isCanceled));
        sb.append("</br>");
        long timerDeliverTime = getTimerDeliverTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!isCanceled) {
            if (!isDeliverTimeUp()) {
                sb.append("预计");
            }
            sb.append("投递时间：").append(sdf.format(timerDeliverTime));
        }
        this.timerDeliverTimeDesc = sb.toString();
    }

    /**
     * 获取全部定时消息当前状态，包含系统消息和用户消息
     * @param isCancelMessage 是否是取消消息
     * @param completeCancelMessage 是否是取消消息
     * @return
     */
    private String getAllTimerCurrentStatus(boolean isCancelMessage,
                                            boolean completeCancelMessage,
                                            boolean forceRoll) {
        if (forceRoll) {
            setTimerStatus(TIMER_ROLLED.getType());
            return "已滚动";
        }
        if (isDeliverTimeUp()) {
            if (isCancelMessage) {
                setTimerStatus(CANCEL_MESSAGE_EXECUTE.getType());
                return "已生效";
            } else if (completeCancelMessage) {
                setTimerStatus(TIMER_CANCEL.getType());
                return "已取消";
            } else {
                setTimerStatus(TIMER_DELIVERY.getType());
                return "已投递";
            }
        }
        if (isCancelMessage) {
            setTimerStatus(CANCEL_MESSAGE_WAIT_EXECUTE.getType());
            return "未生效";
        } else if (completeCancelMessage) {
            setTimerStatus(TIMER_WAIT_CANCEL.getType());
            return "待取消";
        } else {
            setTimerStatus(TIMER_WAIT_DELIVERY.getType());
            return "未投递";
        }
    }

    /**
     * 获取用户视角定时消息当前状态
     * @param completeCancelMessage 是否是取消消息
     * @return
     */
    private String getUserTimerCurrentStatus (boolean completeCancelMessage) {
        if (isDeliverTimeUp()) {
            if (completeCancelMessage) {
                setTimerStatus(TIMER_CANCEL.getType());
                return "已取消";
            } else {
                setTimerStatus(TIMER_DELIVERY.getType());
                return "已投递";
            }
        }
        if (completeCancelMessage) {
            setTimerStatus(TIMER_WAIT_CANCEL.getType());
            return "已取消";
        } else {
            setTimerStatus(TIMER_WAIT_DELIVERY.getType());
            return "未投递";
        }
    }

    public boolean isSysCancelMessage() {
        return MessageService.DEFAULT_CANCEL_MESSAGE_TAGS.equals(getTags());
    }

    public enum MessageDeliveryType {
        TIMER_CANCEL(1, "定时消息已取消"),
        TIMER_WAIT_CANCEL(2, "定时消息待取消"),
        TIMER_DELIVERY(3, "定时消息已投递"),
        TIMER_WAIT_DELIVERY(4, "定时消息待投递"),
        CANCEL_MESSAGE_EXECUTE(5, "取消消息已生效"),
        CANCEL_MESSAGE_WAIT_EXECUTE(6, "取消消息待生效"),
        TIMER_ROLLED(7, "定时消息已滚动");

        private final int type;
        private final String desc;

        MessageDeliveryType(int type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        public int getType() {
            return type;
        }
    }
}
