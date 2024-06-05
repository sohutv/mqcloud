package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.task.server.data.Disk.DiskUsage;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务器状态
 */
public class ServerStatus {
	private String cdate;
	private String ctime;
	private float cuser;
	private float csys;
	private float cwio;
	private String cExt;
	private float cload1;
	private float cload5;
	private float cload15;
	private float mtotal;
	private float mfree;
	private float mcache;
	private float mbuffer;
	private float mswap;
	private float mswapFree;
	private float nin;
	private float nout;
	private int tuse;
	private int torphan;
	private int twait;
	private String ninExt;
	private String noutExt;
	private float dread;
	private float dwrite;
	private float diops;
	private float dbusy;
	private String dExt;
	private String dspace;
	public String getCdate() {
		return cdate;
	}
	public void setCdate(String cdate) {
		this.cdate = cdate;
	}
	public String getCtime() {
		return ctime;
	}
	public void setCtime(String ctime) {
		this.ctime = ctime;
	}
	public float getCuser() {
		return cuser;
	}
	public void setCuser(float cuser) {
		this.cuser = cuser;
	}
	public float getCsys() {
		return csys;
	}
	public void setCsys(float csys) {
		this.csys = csys;
	}
	public float getCwio() {
		return cwio;
	}
	public void setCwio(float cwio) {
		this.cwio = cwio;
	}
	public String getcExt() {
		return cExt;
	}
	public void setcExt(String cExt) {
		this.cExt = cExt;
	}
	public float getCload1() {
		return cload1;
	}
	public void setCload1(float cload1) {
		this.cload1 = cload1;
	}
	public float getCload5() {
		return cload5;
	}
	public void setCload5(float cload5) {
		this.cload5 = cload5;
	}
	public float getCload15() {
		return cload15;
	}
	public int getTuse() {
		return tuse;
	}
	public void setTuse(int tuse) {
		this.tuse = tuse;
	}
	public int getTorphan() {
		return torphan;
	}
	public void setTorphan(int torphan) {
		this.torphan = torphan;
	}
	public int getTwait() {
		return twait;
	}
	public void setTwait(int twait) {
		this.twait = twait;
	}
	public void setCload15(float cload15) {
		this.cload15 = cload15;
	}
	public float getMtotal() {
		return mtotal;
	}
	public void setMtotal(float mtotal) {
		this.mtotal = mtotal;
	}
	public float getMfree() {
		return mfree;
	}
	public void setMfree(float mfree) {
		this.mfree = mfree;
	}
	public float getMcache() {
		return mcache;
	}
	public void setMcache(float mcache) {
		this.mcache = mcache;
	}
	public float getMbuffer() {
		return mbuffer;
	}
	public void setMbuffer(float mbuffer) {
		this.mbuffer = mbuffer;
	}
	public float getMswap() {
		return mswap;
	}
	public void setMswap(float mswap) {
		this.mswap = mswap;
	}
	public float getNin() {
		return nin;
	}
	public void setNin(float nin) {
		this.nin = nin;
	}
	public float getNout() {
		return nout;
	}
	public void setNout(float nout) {
		this.nout = nout;
	}
	public float getDread() {
		return dread;
	}
	public void setDread(float dread) {
		this.dread = dread;
	}
	public float getDwrite() {
		return dwrite;
	}
	public void setDwrite(float dwrite) {
		this.dwrite = dwrite;
	}
	public float getDiops() {
		return diops;
	}
	public void setDiops(float diops) {
		this.diops = diops;
	}
	public float getDbusy() {
		return dbusy;
	}
	public void setDbusy(float dbusy) {
		this.dbusy = dbusy;
	}
	public String getDspace() {
		return dspace;
	}
	public void setDspace(String dspace) {
		this.dspace = dspace;
	}
	public float getMswapFree() {
		return mswapFree;
	}
	public void setMswapFree(float mswapFree) {
		this.mswapFree = mswapFree;
	}
	public String getNinExt() {
		return ninExt;
	}
	public void setNinExt(String ninExt) {
		this.ninExt = ninExt;
	}
	public String getNoutExt() {
		return noutExt;
	}
	public void setNoutExt(String noutExt) {
		this.noutExt = noutExt;
	}
	public String getdExt() {
		return dExt;
	}
	public void setdExt(String dExt) {
		this.dExt = dExt;
	}

	public DiskUsage getDiskUsage(String dir) {
		if (dspace == null) {
			return null;
		}
		String[] mountItems = dspace.split(",");
		String[] destMountItem = null;
		int mountDirLength = 0;
		for (String mountItem : mountItems) {
			String[] items = mountItem.split(":");
			if (items.length == 5) {
				// 选择最长的目录
				if (dir.startsWith(items[2]) && items[2].length() > mountDirLength) {
					mountDirLength = items[2].length();
					destMountItem = items;
				}
			}
		}
		if (destMountItem == null) {
			return null;
		}
		return buildDiskUsage(destMountItem);
	}

	public List<DiskUsage> getDiskUsage() {
		if (dspace == null) {
			return null;
		}
		List<DiskUsage> diskUsageList = new ArrayList<>();
		String[] mountItems = dspace.split(",");
		for (String mountItem : mountItems) {
			String[] items = mountItem.split(":");
			diskUsageList.add(buildDiskUsage(items));
		}
		return diskUsageList;
	}

	private DiskUsage buildDiskUsage(String[] items) {
		DiskUsage diskUsage = new DiskUsage();
		diskUsage.setName(items[0]);
		diskUsage.setValue(NumberUtils.toFloat(items[1].split("%")[0]));
		if (items.length == 5) {
			diskUsage.setMount(items[2]);
			diskUsage.setSize(NumberUtils.toInt(items[3]));
			diskUsage.setUsed(NumberUtils.toInt(items[4]));
		}
		return diskUsage;
	}

}
