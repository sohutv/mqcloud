package com.sohu.tv.mq.cloud.web.vo;
/**
 * server信息
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月18日
 */
public class ServerVO {
    private String ip;
    private String host;
    private int cpu;
    private float totalMemory;
    private float freeMemory;
    private String disk;
    private String io;
    private String net;
    private String establish;
    private String time;
    private float load;
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getCpu() {
        return cpu;
    }
    public void setCpu(int cpu) {
        this.cpu = cpu;
    }
    public float getTotalMemory() {
        return totalMemory;
    }
    public void setTotalMemory(float totalMemory) {
        this.totalMemory = totalMemory;
    }
    public float getFreeMemory() {
        return freeMemory;
    }
    public void setFreeMemory(float freeMemory) {
        this.freeMemory = freeMemory;
    }
    public String getDisk() {
        return disk;
    }
    public void setDisk(String disk) {
        this.disk = disk;
    }
    public String getIo() {
        return io;
    }
    public void setIo(String io) {
        this.io = io;
    }
    public String getNet() {
        return net;
    }
    public void setNet(String net) {
        this.net = net;
    }
    public String getEstablish() {
        return establish;
    }
    public void setEstablish(String establish) {
        this.establish = establish;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public float getLoad() {
        return load;
    }
    public void setLoad(float load) {
        this.load = load;
    }
}