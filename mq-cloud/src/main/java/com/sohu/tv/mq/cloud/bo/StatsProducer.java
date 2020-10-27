package com.sohu.tv.mq.cloud.bo;

/**
 * 统计producer
 * 
 * @author yongfeigao
 * @date 2018年9月17日
 */
public class StatsProducer {
    // producer
    private String producer;
    // 是否进行过统计
    private boolean stats;
    // 前一分钟流量
    private Traffic traffic;

    public boolean isStats() {
        return stats;
    }

    public void setStats(boolean stats) {
        this.stats = stats;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public Traffic getTraffic() {
        return traffic;
    }

    public void setTraffic(Traffic traffic) {
        this.traffic = traffic;
    }
    
    public void copyTraffic(Traffic traffic) {
        this.traffic = new Traffic();
        this.traffic.setSize(traffic.getSize());
        this.traffic.setCount(traffic.getCount());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((producer == null) ? 0 : producer.hashCode());
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
        StatsProducer other = (StatsProducer) obj;
        if (producer == null) {
            if (other.producer != null)
                return false;
        } else if (!producer.equals(other.producer))
            return false;
        return true;
    }
}
