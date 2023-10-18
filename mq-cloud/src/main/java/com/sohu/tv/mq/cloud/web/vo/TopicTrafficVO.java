package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.Traffic;
import com.sohu.tv.mq.cloud.util.DateUtil;

import java.util.Date;

/**
 * topic流量vo
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月3日
 */
public class TopicTrafficVO {
    // id
    private long id;
    // topic name
    private String name;
    // topic 流量
    private Traffic topicTraffic;
    // consumer流量
    private Traffic consumerTraffic;
    
    private boolean own;

    // topic描述
    private String info;

    // 创建日期
    private Date createDate;
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Traffic getTopicTraffic() {
        return topicTraffic;
    }

    public void setTopicTraffic(Traffic topicTraffic) {
        this.topicTraffic = topicTraffic;
    }

    public Traffic getConsumerTraffic() {
        return consumerTraffic;
    }

    public void setConsumerTraffic(Traffic consumerTraffic) {
        this.consumerTraffic = consumerTraffic;
    }

    public boolean isOwn() {
        return own;
    }

    public void setOwn(boolean own) {
        this.own = own;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getCreateDateFormat() {
        return DateUtil.getFormat(DateUtil.YMD_DASH).format(createDate);
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "TopicTrafficVO [id=" + id + ", name=" + name + ", topicTraffic=" + topicTraffic + ", consumerTraffic="
                + consumerTraffic + ", info=" + info + "]";
    }
}
