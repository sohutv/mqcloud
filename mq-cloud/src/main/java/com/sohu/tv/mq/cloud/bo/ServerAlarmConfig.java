package com.sohu.tv.mq.cloud.bo;

/**
 * 预警配置项
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年10月30日
 */
public class ServerAlarmConfig {
    // ip，
    private String ip;
    // 内存使用率
    private int memoryUsageRate;
    // 一分钟load
    private int load1;
    // tcp estab连接数
    private int connect;
    // tcp time wait连接数
    private int wait;
    // 磁盘io速率 交互次数/s
    private int iops;
    // 磁盘io带宽使用百分比
    private int iobusy;
    // 处理器使用率
    private int cpuUsageRate;
    // 入网流量
    private int netIn;
    // 出网流量
    private int netOut;
    //磁盘使用率
    private int ioUsageRate;
    
    public int getNetIn() {
        return netIn;
    }

    public void setNetIn(int netIn) {
        this.netIn = netIn;
    }

    public int getNetOut() {
        return netOut;
    }

    public void setNetOut(int netOut) {
        this.netOut = netOut;
    }

    public int getMemoryUsageRate() {
        return memoryUsageRate;
    }

    public void setMemoryUsageRate(int memoryUsageRate) {
        this.memoryUsageRate = memoryUsageRate;
    }

    public int getLoad1() {
        return load1;
    }

    public void setLoad1(int load1) {
        this.load1 = load1;
    }

    public int getConnect() {
        return connect;
    }

    public void setConnect(int connect) {
        this.connect = connect;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public int getIops() {
        return iops;
    }

    public void setIops(int iops) {
        this.iops = iops;
    }

    public int getIobusy() {
        return iobusy;
    }

    public void setIobusy(int iobusy) {
        this.iobusy = iobusy;
    }

    public int getCpuUsageRate() {
        return cpuUsageRate;
    }

    public void setCpuUsageRate(int cpuUsageRate) {
        this.cpuUsageRate = cpuUsageRate;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getIoUsageRate() {
        return ioUsageRate;
    }

    public void setIoUsageRate(int ioUsageRate) {
        this.ioUsageRate = ioUsageRate;
    }
    
    @Override
    public String toString() {
        return "ServerAlarmConfig [ip="+ip+", memoryUsageRate=" + memoryUsageRate + ", load1=" + load1 
                + ", connect=" + connect + ", wait=" + wait + ", iops=" + iops + ", iobusy=" + iobusy
                + ", cpuUseRate=" + cpuUsageRate + ", netIn=" + netIn + ", netOut=" + netOut
                + ", ioUsageRate=" + ioUsageRate + "]";
    }
}
