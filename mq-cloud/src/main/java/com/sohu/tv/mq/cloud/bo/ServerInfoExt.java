package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.util.MachineType;

/**
 * 服务与状态混合bo
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月18日
 */
public class ServerInfoExt extends ServerStatus {
    private String ip;
    private String host;
    // 逻辑cpu个数
    private int cpus;
    // cpu型号
    private String cpuModel;
    // ulimit
    private String ulimit;

    private Integer machineType;
    
    private String room;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public String getUlimit() {
        return ulimit;
    }

    public void setUlimit(String ulimit) {
        this.ulimit = ulimit;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getMachineType() {
        return machineType;
    }

    public void setMachineType(int machineType) {
        this.machineType = machineType;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    /***
     * 获取机器类型名称
     * 
     * @return
     */
    public String getMachineTypeName() {
        if (getMachineType() == null) {
            return "";
        }
        for (MachineType mt : MachineType.values()) {
            if (mt.getKey() == getMachineType()) {
                return mt.getValue();
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "ServerInfoExt [host=" + host + ", cpus=" + cpus + ", cpuModel=" + cpuModel + ", ulimit=" + ulimit
                + ", getCdate()=" + getCdate() + ", getCtime()=" + getCtime() + ", getCuser()=" + getCuser()
                + ", getCsys()=" + getCsys() + ", getCwio()=" + getCwio() + ", getcExt()=" + getcExt()
                + ", getCload1()=" + getCload1() + ", getCload5()=" + getCload5() + ", getCload15()=" + getCload15()
                + ", getTuse()=" + getTuse() + ", getTorphan()=" + getTorphan() + ", getTwait()=" + getTwait()
                + ", getMtotal()=" + getMtotal() + ", getMfree()=" + getMfree() + ", getMcache()=" + getMcache()
                + ", getMbuffer()=" + getMbuffer() + ", getMswap()=" + getMswap() + ", getNin()=" + getNin()
                + ", getNout()=" + getNout() + ", getDread()=" + getDread() + ", getDwrite()=" + getDwrite()
                + ", getDiops()=" + getDiops() + ", getDbusy()=" + getDbusy() + ", getDspace()=" + getDspace()
                + ", getMswapFree()=" + getMswapFree() + ", getNinExt()=" + getNinExt() + ", getNoutExt()="
                + getNoutExt() + ", getdExt()=" + getdExt() + ", machineType=" + machineType + "]";
    }
}
