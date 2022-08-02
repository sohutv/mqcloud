package com.sohu.tv.mq.cloud.bo;

/**
 * 用户生产者对象
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月12日
 */
public class UserProducer extends UserTopic {
    private String producer;

    private boolean stats;

    // 是否开启http生产
    private int httpEnabled;

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public boolean isStats() {
        return stats;
    }

    public void setStats(boolean stats) {
        this.stats = stats;
    }

    public int getHttpEnabled() {
        return httpEnabled;
    }

    public void setHttpEnabled(int httpEnabled) {
        this.httpEnabled = httpEnabled;
    }

    public boolean httpEnabled() {
        return 1 == httpEnabled;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((producer == null) ? 0 : producer.hashCode());
        result = prime * result + (int) (getTid() ^ (getTid() >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserProducer other = (UserProducer) obj;
        if (producer == null) {
            if (other.producer != null)
                return false;
        } else if (!producer.equals(other.producer))
            return false;
        if (getTid() != other.getTid())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "UserProducer [producer=" + producer + ", toString()=" + super.toString() + "]";
    }
}
