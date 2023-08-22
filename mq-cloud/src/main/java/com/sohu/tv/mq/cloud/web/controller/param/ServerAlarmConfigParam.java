package com.sohu.tv.mq.cloud.web.controller.param;

import org.apache.commons.lang3.StringUtils;

/**
 * 服务器预警配置项
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年10月31日
 */
public class ServerAlarmConfigParam {

    private String ipList;

    private String cpuUsageRate;

    private String load1;

    private String memoryUsageRate;

    private String iobusy;

    private String iops;

    private String connect;

    private String wait;

    private String netIn;

    private String netOut;

    private String ioUsageRate;

    public int getNetIn() {
        return string2Int(netIn);
    }

    public void setNetIn(String netIn) {
        this.netIn = netIn;
    }

    public int getNetOut() {
        return string2Int(netOut);
    }

    public void setNetOut(String netOut) {
        this.netOut = netOut;
    }

    public int getCpuUsageRate() {
        return string2Int(cpuUsageRate);
    }

    public void setCpuUsageRate(String cpuUsageRate) {
        this.cpuUsageRate = cpuUsageRate;
    }

    public int getLoad1() {
        return string2Int(load1);
    }

    public void setLoad1(String load1) {
        this.load1 = load1;
    }

    public int getMemoryUsageRate() {
        return string2Int(memoryUsageRate);
    }

    public void setMemoryUsageRate(String memoryUsageRate) {
        this.memoryUsageRate = memoryUsageRate;
    }

    public int getIobusy() {
        return string2Int(iobusy);
    }

    public void setIobusy(String iobusy) {
        this.iobusy = iobusy;
    }

    public int getIops() {
        return string2Int(iops);
    }

    public void setIops(String iops) {
        this.iops = iops;
    }

    public int getConnect() {
        return string2Int(connect);
    }

    public void setConnect(String connect) {
        this.connect = connect;
    }

    public int getWait() {
        return string2Int(wait);
    }

    public void setWait(String wait) {
        this.wait = wait;
    }

    public String getIpList() {
        return ipList;
    }

    public void setIpList(String ipList) {
        this.ipList = ipList;
    }

    public int getIoUsageRate() {
        return string2Int(ioUsageRate);
    }

    public void setIoUsageRate(String ioUsageRate) {
        this.ioUsageRate = ioUsageRate;
    }

    /**
     * 类型转换
     * 
     * @param arg
     * @return
     */
    private int string2Int(String arg) {
        return StringUtils.isBlank(arg) ? 0 : Integer.parseInt(arg);
    }
}
