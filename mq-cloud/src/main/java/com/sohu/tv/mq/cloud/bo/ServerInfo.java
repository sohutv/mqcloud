package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.util.MachineType;

import java.util.Date;
import java.util.List;

/**
 * 服务器信息
 */
public class ServerInfo {
	private String ip;
	private String host;
	//逻辑cpu个数
	private int cpus;
	//nmon版本
	private String nmon;
	//cpu型号
	private String cpuModel;
	//内核版本
	private String kernel;
	//发行版本
	private String dist;
	//ulimit
	private String ulimit;
	// 机房
    private String room;
    // 机器类型
    private Integer machineType;
    // 机房颜色，额外添加字段
    private String roomColor;
	// 部署目录
	private List<String> deployDirs;

	// 收集时间-测试用
	private Date collectTime;
    
	public String getUlimit() {
		return ulimit;
	}
	public void setUlimit(String ulimit) {
		this.ulimit = ulimit;
	}
	public String getIp() {
		return ip;
	}
	public String getIpSub() {
        return ip.split("\\.", 3)[2];
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
	public int getCpus() {
		return cpus;
	}
	public void setCpus(int cpus) {
		this.cpus = cpus;
	}
	public String getNmon() {
		return nmon;
	}
	public void setNmon(String nmon) {
		this.nmon = nmon;
	}
	public String getCpuModel() {
		return cpuModel;
	}
	public void setCpuModel(String cpuModel) {
		this.cpuModel = cpuModel;
	}
	public String getKernel() {
		return kernel;
	}
	public void setKernel(String kernel) {
		this.kernel = kernel;
	}
	public String getDist() {
		return dist;
	}
	public void setDist(String dist) {
		this.dist = dist;
	}
    public String getRoom() {
        return room;
    }
    public void setRoom(String room) {
        this.room = room;
    }
    public Integer getMachineType() {
        return machineType;
    }
    public void setMachineType(Integer machineType) {
        this.machineType = machineType;
    }
    
    public String getRoomColor() {
        return roomColor;
    }
    public void setRoomColor(String roomColor) {
        this.roomColor = roomColor;
    }
    public boolean isPhysical() {
        return MachineType.PHYSICAL.getKey() == machineType;
    }
    
    public boolean isVirtual() {
        return MachineType.VIRTUAL.getKey() == machineType;
    }
    
    public boolean isDocker() {
        return MachineType.DOCKER.getKey() == machineType;
    }
    
    /***
     * 获取机器类型名称
     * 
     * @return
     */
    public String getMachineTypeName() {
        if (getMachineType() == null) {
            return MachineType.UNKNOWN.getValue();
        }
        for (MachineType mt : MachineType.values()) {
            if (mt.getKey() == getMachineType()) {
                return mt.getValue();
            }
        }
        return MachineType.UNKNOWN.getValue();
    }

	public List<String> getDeployDirs() {
		return deployDirs;
	}

	public void setDeployDirs(List<String> deployDirs) {
		this.deployDirs = deployDirs;
	}

	public Date getCollectTime() {
		return collectTime;
	}

	public void setCollectTime(Date collectTime) {
		this.collectTime = collectTime;
	}
}
