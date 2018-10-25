package com.sohu.tv.mq.cloud.web.controller.admin;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.cloud.bo.ServerInfo;
import com.sohu.tv.mq.cloud.bo.ServerInfoExt;
import com.sohu.tv.mq.cloud.bo.ServerStatus;
import com.sohu.tv.mq.cloud.service.SSHTemplate;
import com.sohu.tv.mq.cloud.service.ServerDataService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;

/**
 * 服务器
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月18日
 */
@Controller
@RequestMapping("/admin/server")
public class AdminServerController extends AdminViewController {
    
    @Autowired
    private ServerDataService serverDataService;
    
    @Autowired
    private SSHTemplate sshTemplate;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    /**
     * 新增
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/add", method=RequestMethod.POST)
    public Result<?> add(@RequestParam("ip") String ip, Map<String, Object> map) {
        try {
            sshTemplate.validate(ip);
        } catch (Exception e) {
            logger.error("validate:{}", ip, e);
            return Result.getResult(Status.NOT_INIT_IP);
        }
        serverDataService.saveServerInfo(ip, "init");
        return Result.getOKResult();
    }
    
    /**
     * 列表
     * @param map
     * @return
     */
    @RequestMapping("/list")
    public String list(Map<String, Object> map) {
        setView(map, "list");
        List<ServerInfoExt> serverStatList = serverDataService.queryAllServer(DateUtil.formatYMDNow());
        setResult(map, serverStatList);
        setResult(map, "username", mqCloudConfigHelper.getServerUser());
        setResult(map, "password", mqCloudConfigHelper.getServerPassword());
        return view();
    }
    
    /**
     * 列表
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/all")
    public Result<?> all(Map<String, Object> map) {
        List<ServerInfo> serverInfoList = serverDataService.queryAllServerInfo();
        return Result.getResult(serverInfoList);
    }

    /**
     * 删除
     * 
     * @param ip
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Result<?> delete(@RequestParam(name = "ip", required = false) String ip) {
        if ("".equals(ip)) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Result<Integer> deleteResult = serverDataService.deleteServer(ip);
        return deleteResult;
    }
    
    /**
     * 服务器信息概览
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping("/overview")
    public String overview(@RequestParam("ip") String ip, 
            @RequestParam(value="date", required=false) String date, 
            Map<String, Object> map) {
        if(date == null) {
            date = DateUtil.formatYMDNow();
        }
        //获取服务器静态信息
        ServerInfo info = serverDataService.queryServerInfo(ip);
        if(info != null) {
            map.put("info", info);
            //解析ulimit
            String ulimit = info.getUlimit();
            if(!StringUtils.isEmpty(ulimit)) {
                String[] tmp = ulimit.split(";");
                if(tmp.length ==2) {
                    String[] a = tmp[0].split(",");
                    if(a != null && a.length == 2) {
                        if("f".equals(a[0])) {
                            map.put("file", a[1]);
                        }
                    }
                    a = tmp[1].split(",");
                    if(a != null && a.length == 2) {
                        if("p".equals(a[0])) {
                            map.put("process", a[1]);
                        }
                    }
                }
            }
        }
        //获取服务器状态
        List<ServerStatus> list = serverDataService.queryServerStat(ip, date);
        //x轴坐标
        List<String> xAxis = new ArrayList<String>();
        
        //1分钟最大load
        float maxLoad1 = 0;
        //load1总量
        double totalLoad1 = 0;
        
        //最大user
        float maxUser = 0;
        //最大sys
        float maxSys = 0;
        //最大wio
        float maxWa = 0;
        
        //当前可用内存
        float curFree = 0;
        //最大内存使用量
        float maxUse = 0;
        //最大内存cache量
        float maxCache = 0;
        //最大内存buffer量
        float maxBuffer = 0;
        //最大swap使用量
        float maxSwapUse = 0;
        
        //最大网络流入速度
        float maxNetIn = 0;
        //最大网络流出速度
        float maxNetOut = 0;
        //最大连接ESTABLISHED数
        int maxConn = 0;
        //最大连接TIME_WAIT数
        int maxWait = 0;
        //最大连接ORPHAN数
        int maxOrphan = 0;
        
        //最大读取速率
        float maxRead = 0;
        //最大写入速率
        float maxWrite = 0;
        //最繁忙程度
        float maxBusy = 0;
        //最大iops量
        float maxIops = 0;
        
        //load serie
        Series<Float> load1Serie = new Series<Float>("1-min");
        Series<Float> load5Serie = new Series<Float>("5-min");
        Series<Float> load15Serie = new Series<Float>("15-min");
        
        //cpu serie
        Series<Float> userSerie = new Series<Float>("user");
        Series<Float> sysSerie = new Series<Float>("sys");
        Series<Float> waSerie = new Series<Float>("wa");
        
        //memory serie
        Series<Float> totalSerie = new Series<Float>("total");
        Series<Float> useSerie = new Series<Float>("use");
        useSerie.setType("area");
        Series<Float> cacheSerie = new Series<Float>("cache");
        cacheSerie.setType("area");
        Series<Float> bufferSerie = new Series<Float>("buffer");
        bufferSerie.setType("area");
        Series<Float> swapSerie = new Series<Float>("total");
        Series<Float> swapUseSerie = new Series<Float>("use");
        
        //net serie
        Series<Float> netInSerie = new Series<Float>("in");
        Series<Float> netOutSerie = new Series<Float>("out");
        
        //tcp serie
        Series<Integer> establishedSerie = new Series<Integer>("established");
        Series<Integer> twSerie = new Series<Integer>("time wait");
        Series<Integer> orphanSerie = new Series<Integer>("orphan");
        
        //disk serie
        Series<Float> readSerie = new Series<Float>("read");
        readSerie.setType("column");
        Series<Float> writeSerie = new Series<Float>("write");
        writeSerie.setType("column");
        Series<Float> busySerie = new Series<Float>("busy");
        busySerie.setYAxis(1);
        Series<Float> iopsSerie = new Series<Float>("iops");
        iopsSerie.setYAxis(2);
        
        for(int i = 0; i < list.size(); ++i) {
            ServerStatus ss = list.get(i);
            //x axis
            xAxis.add(ss.getCtime().substring(0, 2) + ":" + ss.getCtime().substring(2));
            //load相关
            load1Serie.addData(ss.getCload1());
            load5Serie.addData(ss.getCload5());
            load15Serie.addData(ss.getCload15());
            maxLoad1 = getBigger(maxLoad1, ss.getCload1());
            totalLoad1 += ss.getCload1();
            //cpu相关
            userSerie.addData(ss.getCuser());
            sysSerie.addData(ss.getCsys());
            waSerie.addData(ss.getCwio());
            maxUser = getBigger(maxUser, ss.getCuser());
            maxSys = getBigger(maxSys, ss.getCsys());
            maxWa = getBigger(maxWa, ss.getCwio());
            //memory相关
            totalSerie.addData(ss.getMtotal());
            float use = ss.getMtotal()-ss.getMfree()-ss.getMcache()-ss.getMbuffer();
            useSerie.addData(use);
            cacheSerie.addData(ss.getMcache());
            bufferSerie.addData(ss.getMbuffer());
            maxUse = getBigger(maxUse, use);
            maxCache = getBigger(maxCache, ss.getMcache());
            maxBuffer = getBigger(maxBuffer, ss.getMbuffer());
            if(i == list.size() - 1) {
                curFree = ss.getMtotal() - use;
            }
            //swap相关
            swapSerie.addData(ss.getMswap());
            float swapUse = ss.getMswap() - ss.getMswapFree();
            swapUse = floor(swapUse);
            swapUseSerie.addData(swapUse);
            maxSwapUse = getBigger(maxSwapUse, swapUse);
            //net相关
            netInSerie.addData(ss.getNin());
            netOutSerie.addData(ss.getNout());
            maxNetIn = getBigger(maxNetIn, ss.getNin());
            maxNetOut = getBigger(maxNetOut, ss.getNout());
            //tcp相关
            establishedSerie.addData(ss.getTuse());
            twSerie.addData(ss.getTwait());
            orphanSerie.addData(ss.getTorphan());
            maxConn = getBigger(maxConn, ss.getTuse());
            maxWait = getBigger(maxWait, ss.getTwait());
            maxOrphan = getBigger(maxOrphan, ss.getTorphan());
            //disk相关
            readSerie.addData(ss.getDread());
            writeSerie.addData(ss.getDwrite());
            busySerie.addData(ss.getDbusy());
            iopsSerie.addData(ss.getDiops());
            maxRead = getBigger(maxRead, ss.getDread());
            maxWrite = getBigger(maxWrite, ss.getDwrite());
            maxBusy = getBigger(maxBusy, ss.getDbusy());
            maxIops = getBigger(maxIops, ss.getDiops());
        }
        //x axis
        map.put("xAxis", JSON.toJSONString(xAxis));
        //load
        map.put("load1", JSON.toJSONString(load1Serie));
        map.put("load5", JSON.toJSONString(load5Serie));
        map.put("load15", JSON.toJSONString(load15Serie));
        map.put("maxLoad1", maxLoad1);
        map.put("avgLoad1", format(totalLoad1, list.size()));
        //cpu
        map.put("user", JSON.toJSONString(userSerie));
        map.put("sys", JSON.toJSONString(sysSerie));
        map.put("wa", JSON.toJSONString(waSerie));
        map.put("maxUser", maxUser);
        map.put("maxSys", maxSys);
        map.put("maxWa", maxWa);
        //memory
        map.put("mtotal", JSON.toJSONString(totalSerie));
        map.put("muse", JSON.toJSONString(useSerie));
        map.put("mcache", JSON.toJSONString(cacheSerie));
        map.put("mbuffer", JSON.toJSONString(bufferSerie));
        map.put("curFree", format(curFree, 1024));
        map.put("maxUse", format(maxUse, 1024));
        map.put("maxCache", format(maxCache, 1024));
        map.put("maxBuffer", format(maxBuffer, 1024));
        //swap
        map.put("mswap", JSON.toJSONString(swapSerie));
        map.put("mswapUse", JSON.toJSONString(swapUseSerie));
        map.put("maxSwap", maxSwapUse);
        //net
        map.put("nin", JSON.toJSONString(netInSerie));
        map.put("nout", JSON.toJSONString(netOutSerie));
        map.put("maxNetIn", format(maxNetIn, 1024));
        map.put("maxNetOut", format(maxNetOut, 1024));
        //tcp
        map.put("testab", JSON.toJSONString(establishedSerie));
        map.put("twait", JSON.toJSONString(twSerie));
        map.put("torph", JSON.toJSONString(orphanSerie));
        map.put("maxConn", maxConn);
        map.put("maxWait", maxWait);
        map.put("maxOrphan", maxOrphan);
        //disk
        map.put("dread", JSON.toJSONString(readSerie));
        map.put("dwrite", JSON.toJSONString(writeSerie));
        map.put("dbusy", JSON.toJSONString(busySerie));
        map.put("diops", JSON.toJSONString(iopsSerie));
        map.put("maxRead", format(maxRead, 1024));
        map.put("maxWrite", format(maxWrite, 1024));
        map.put("maxBusy", maxBusy);
        map.put("maxIops", maxIops);
        map.put("date", date);
        
        return adminViewModule() + "/overview";
    }
    
    private String format(double a, int b) {
        if(b <= 0) {
            return "0";
        }
        return new DecimalFormat("0.0").format(a/b);
    }
    
    private float getBigger(float a, float b) {
        if(a > b) {
            return a;
        }
        return b;
    }
    
    private int getBigger(int a, int b) {
        if(a > b) {
            return a;
        }
        return b;
    }
    
    /**
     * 保留一位小数，四舍五入
     * @param v
     * @return
     */
    private float floor(float v) {
        return new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
    }
    
    @Override
    public String viewModule() {
        return "server";
    }
    
    /**
     * 获取服务器cpu各个核状态
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping("/cpu")
    public String cpu(@RequestParam("ip") String ip, 
            @RequestParam("date") String date, 
            Map<String, Object> map) {
        List<ServerStatus> list = serverDataService.queryServerStat(ip, date);
        Map<String, CpuChart> subcpuMap = new TreeMap<String, CpuChart>();
        //x轴坐标
        List<String> xAxis = new ArrayList<String>();
        for(ServerStatus ss : list) {
            String subcpuString = ss.getcExt();
            String[] subCpuArray = subcpuString.split(";");
            xAxis.add(ss.getCtime());
            for(String subcpu : subCpuArray) {
                if(StringUtils.isEmpty(subcpu)) {
                    continue;
                }
                String[] cpu = subcpu.split(",");
                CpuChart cpuChart = subcpuMap.get(cpu[0]);
                if(cpuChart == null) {
                    cpuChart = new CpuChart(cpu[0]);
                    subcpuMap.put(cpu[0], cpuChart);
                }
                float user = NumberUtils.toFloat(cpu[1]);
                float sys = NumberUtils.toFloat(cpu[2]);
                float wa = NumberUtils.toFloat(cpu[3]);
                cpuChart.addUserSeries(user);
                cpuChart.addSysSeries(sys);
                cpuChart.addWaSeries(wa);
                cpuChart.setMaxUser(user);
                cpuChart.setMaxSys(sys);
                cpuChart.setMaxWa(wa);
                cpuChart.addUser(user);
                cpuChart.addSys(sys);
                cpuChart.addWa(wa);
            }
        }
        //x axis
        map.put("xAxis", JSON.toJSONString(xAxis));
        map.put("cpu", subcpuMap.values());
        return adminViewModule() + "/cpu";
    }
    
    /**
     * 获取服务器各网卡状态
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping("/net")
    public String net(@RequestParam("ip") String ip, 
            @RequestParam("date") String date, 
            Map<String, Object> map) {
        List<ServerStatus> list = serverDataService.queryServerStat(ip, date);
        Map<String, NetChart> subnetMap = new TreeMap<String, NetChart>();
        //x轴坐标
        List<String> xAxis = new ArrayList<String>();
        for(ServerStatus ss : list) {
            xAxis.add(ss.getCtime());
            addNetMap(ss.getNinExt(), subnetMap, true);
            addNetMap(ss.getNoutExt(), subnetMap, false);
        }
        //x axis
        map.put("xAxis", JSON.toJSONString(xAxis));
        map.put("net", subnetMap.values());
        return adminViewModule() + "/net";
    }
    
    /**
     * parse net to map
     * @param netString
     * @param subnetMap
     * @param isIn
     */
    private void addNetMap(String netString, Map<String, NetChart> subnetMap, boolean isIn) {
        String[] subnetArray = netString.split(";");
        for(String subnet : subnetArray) {
            if(StringUtils.isEmpty(subnet)) {
                continue;
            }
            String[] net = subnet.split(",");
            NetChart netChart = subnetMap.get(net[0]);
            if(netChart == null) {
                netChart = new NetChart(net[0]);
                subnetMap.put(net[0], netChart);
            }
            float v = NumberUtils.toFloat(net[1]);
            if(isIn) {
                netChart.addInSeries(v);
                netChart.addTotalIn(v);
                netChart.setMaxIn(v);
            }else {
                netChart.addOutSeries(v);
                netChart.addTotalOut(v);
                netChart.setMaxOut(v);
            }
        }
    }
    
    /**
     * 获取硬盘各分区状态
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping("/disk")
    public String disk(@RequestParam("ip") String ip, 
            @RequestParam("date") String date, 
            Map<String, Object> map) {
        List<ServerStatus> list = serverDataService.queryServerStat(ip, date);
        DiskChart readChart = new DiskChart();
        DiskChart writeChart = new DiskChart();
        DiskChart busyChart = new DiskChart();
        DiskChart iopsChart = new DiskChart();
        DiskChart spaceChart = new DiskChart();
        //x轴坐标
        List<String> xAxis = new ArrayList<String>();
        for(ServerStatus ss : list) {
            xAxis.add(ss.getCtime());
            //解析use
            String dext = ss.getdExt();
            if(!StringUtils.isEmpty(dext)) {
                String[] items = dext.split(";");
                if(items != null) {
                    for(String item : items) {
                        String[] sds = item.split("=");
                        if(sds.length == 2) {
                            if("DISKXFER".equals(sds[0])) {
                                addToChart(sds[1], iopsChart);
                            } else if("DISKREAD".equals(sds[0])) {
                                addToChart(sds[1], readChart);
                            } else if("DISKWRITE".equals(sds[0])) {
                                addToChart(sds[1], writeChart);
                            } else if("DISKBUSY".equals(sds[0])) {
                                addToChart(sds[1], busyChart);
                            }
                        }
                    }
                }
            }
            //解析space
            String space = ss.getDspace();
            addToChart(space, spaceChart);
        }
        //x axis
        map.put("xAxis", JSON.toJSONString(xAxis));
        map.put("read", readChart);
        map.put("write", writeChart);
        map.put("busy", busyChart);
        map.put("iops", iopsChart);
        map.put("space", spaceChart);
        return adminViewModule() + "/disk";
    }
    
    private void addToChart(String line, DiskChart chart) {
        String[] parts = line.split(",");
        for(String part : parts) {
            if(StringUtils.isEmpty(part)) {
                continue;
            }
            String[] values = part.split(":");
            float d = NumberUtils.toFloat(values[1]);
            chart.addSeries(values[0], d);
            chart.setMax(d);
            chart.addTotal(d);
        }
    }
    
    /**
     * net chart
     */
    public class NetChart{
        private String name;
        private Series<Float> inSeries = new Series<Float>("in");
        private Series<Float> outSeries = new Series<Float>("out");
        private float maxIn;
        private float maxOut;
        private float totalIn;
        private float totalOut;
        public NetChart(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Series<Float> getInSeries() {
            return inSeries;
        }
        public void addInSeries(float d) {
            this.inSeries.addData(d);
        }
        public Series<Float> getOutSeries() {
            return outSeries;
        }
        public void addOutSeries(float d) {
            this.outSeries.addData(d);
        }
        public float getMaxIn() {
            return maxIn;
        }
        public void setMaxIn(float in) {
            if(this.maxIn < in) {
                this.maxIn = in;
            }
        }
        public float getMaxOut() {
            return maxOut;
        }
        public void setMaxOut(float out) {
            if(this.maxOut < out) {
                this.maxOut = out;
            }
        }
        public void addTotalIn(float in) {
            this.totalIn += in;
        }
        public void addTotalOut(float out) {
            this.totalOut += out;
        }
        public String getAvgIn() {
            return format(totalIn, inSeries.getData().size());
        }
        public String getAvgOut() {
            return format(totalOut, outSeries.getData().size());
        }
    }
    
    /**
     * disk chart
     */
    public class DiskChart{
        private float max;
        private float total;
        private Map<String, Series<Float>> seriesMap = new TreeMap<String, Series<Float>>();
        public void addSeries(String partition, float d) {
            Series<Float> series = seriesMap.get(partition);
            if(series == null) {
                series = new Series<Float>(partition);
                seriesMap.put(partition, series);
            }
            series.addData(d);
        }
        public Collection<Series<Float>> getSeries() {
            return seriesMap.values();
        }
        public float getMax() {
            return max;
        }
        public void setMax(float max) {
            if(this.max < max) {
                this.max = max;
            }
        }
        public String getAvg() {
            Collection<Series<Float>> coll = seriesMap.values();
            int size = 0;
            if(coll != null) {
                for(Series<Float> series : coll) {
                    size += series.getData().size();
                }
            }
            return format(total, size);
        }
        public void addTotal(float total) {
            this.total += total;
        }
    }
    
    /**
     * cpu chart
     */
    public class CpuChart{
        private String name;
        private Series<Float> userSeries = new Series<Float>("user");
        private Series<Float> sysSeries = new Series<Float>("sys");
        private Series<Float> waSeries = new Series<Float>("wa");
        private float maxUser;
        private float maxSys;
        private float maxWa;
        private float totalUser;
        private float totalSys;
        private float totalWa;
        public CpuChart(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public float getMaxUser() {
            return maxUser;
        }
        public void setMaxUser(float user) {
            if(this.maxUser < user) {
                this.maxUser = user;
            }
        }
        public float getMaxSys() {
            return maxSys;
        }
        public void setMaxSys(float sys) {
            if(this.maxSys < sys) {
                this.maxSys = sys;
            }
        }
        public float getMaxWa() {
            return maxWa;
        }
        public void setMaxWa(float wa) {
            if(this.maxWa < wa) {
                this.maxWa = wa;
            }
        }
        public String getAvgUser() {
            return format(totalUser, userSeries.getData().size());
        }
        public String getAvgSys() {
            return format(totalSys, sysSeries.getData().size());
        }
        public String getAvgWa() {
            return format(totalWa, waSeries.getData().size());
        }
        public void addUser(float user) {
            this.totalUser += user;
        }
        public void addSys(float sys) {
            this.totalSys += sys;
        }
        public void addWa(float wa) {
            this.totalWa += wa;
        }
        public Series<Float> getUserSeries() {
            return userSeries;
        }
        public void addUserSeries(Float v) {
            this.userSeries.addData(v);
        }
        public Series<Float> getSysSeries() {
            return sysSeries;
        }
        public void addSysSeries(Float v) {
            this.sysSeries.addData(v);
        }
        public Series<Float> getWaSeries() {
            return waSeries;
        }
        public void addWaSeries(Float v) {
            this.waSeries.addData(v);
        }
    }
    
    /**
     * Highchars Series
     * @param <T> 
     */
    public class Series<T>{
        private String name;
        private List<T> data = new ArrayList<T>();
        private String type = "spline";
        private int yAxis;
        public String toJson() {
            return JSON.toJSONString(this);
        }
        public Series(String name) {
            this.name = name;
        }
        public int getYAxis() {
            return yAxis;
        }
        public void setYAxis(int yAxis) {
            this.yAxis = yAxis;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void addData(T d) {
            data.add(d);
        }
        public List<T> getData() {
            return data;
        }
        @Override
        public String toString() {
            return "Serie [name=" + name + ", data=" + data + ", type=" + type
                    + ", yAxis=" + yAxis + "]";
        }
    }
}
