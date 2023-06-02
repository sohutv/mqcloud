package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.*;
import com.sohu.tv.mq.cloud.web.controller.param.ServerAlarmConfigParam;
import com.sohu.tv.mq.cloud.web.vo.MachineTypeVO;
import com.sohu.tv.mq.cloud.web.vo.ServerRoleVO;
import com.sohu.tv.mq.util.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 服务器
 * 
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

    @Autowired
    private ServerAlarmConfigService serverAlarmConfigService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private NameServerService nameServerService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private com.sohu.tv.mq.cloud.service.ControllerService controllerService;

    /**
     * 新增
     * 
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> add(@RequestParam("ip") String ip, @RequestParam("type") int type,
            @RequestParam("machineRoom") String room, Map<String, Object> map) {
        try {
            sshTemplate.validate(ip);
        } catch (Exception e) {
            logger.error("validate:{}", ip, e);
            return Result.getResult(Status.NOT_INIT_IP);
        }
        Result<?> rst = serverDataService.saveServerInfo(ip, "init", type, room);
        return Result.getWebResult(rst);
    }

    /**
     * 列表
     * 
     * @param map
     * @return
     */
    @RequestMapping("/list")
    public String list(Map<String, Object> map) {
        setView(map, "list");
        List<ServerInfoExt> serverStatList = serverDataService.queryAllServer(new Date());
        setResult(map, serverStatList);
        setResult(map, "username", mqCloudConfigHelper.getServerUser());
        setResult(map, "password", mqCloudConfigHelper.getServerPassword());
        setResult(map, "machineRoom", mqCloudConfigHelper.getMachineRoom());

        if (serverStatList.size() > 0) {
            Map<String, ServerRoleVO> serverRoleVOMap = new HashMap<>();
            // 获取broker信息
            Result<List<Broker>> brokerListResult = brokerService.queryAll();
            if (brokerListResult.isNotEmpty()) {
                brokerListResult.getResult().forEach(broker -> {
                    serverRoleVOMap.computeIfAbsent(broker.getIp(), k -> new ServerRoleVO()).addBroker(broker,
                            clusterService.getMQClusterById(broker.getCid()));
                });
            }
            // 获取nameserver信息
            Result<List<NameServer>> nameServerListResult = nameServerService.queryAll();
            if (nameServerListResult.isNotEmpty()) {
                nameServerListResult.getResult().forEach(nameServer -> {
                    serverRoleVOMap.computeIfAbsent(nameServer.getIp(), k -> new ServerRoleVO()).addNameServer(nameServer,
                            clusterService.getMQClusterById(nameServer.getCid()));
                });
            }
            // 获取proxy信息
            Result<List<Proxy>> proxyListResult = proxyService.queryAll();
            if (proxyListResult.isNotEmpty()) {
                proxyListResult.getResult().forEach(proxy -> {
                    serverRoleVOMap.computeIfAbsent(proxy.getIp(), k -> new ServerRoleVO()).addProxy(proxy,
                            clusterService.getMQClusterById(proxy.getCid()));
                });
            }
            // 获取controller信息
            Result<List<com.sohu.tv.mq.cloud.bo.Controller>> controllerListResult = controllerService.queryAll();
            if (controllerListResult.isNotEmpty()) {
                controllerListResult.getResult().forEach(controller -> {
                    serverRoleVOMap.computeIfAbsent(controller.getIp(), k -> new ServerRoleVO()).addController(controller,
                            clusterService.getMQClusterById(controller.getCid()));
                });
            }
            // 设置到最终返回中
            serverStatList.forEach(serverInfoExt -> {
                serverInfoExt.setServerRoleVO(serverRoleVOMap.get(serverInfoExt.getIp()));
            });
        }
        return view();
    }

    /**
     * 列表
     * 
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
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public Result<?> delete(@RequestParam(name = "ip", required = false) String ip) {
        if ("".equals(ip)) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Result<Integer> deleteResult = serverDataService.deleteServer(ip);
        return deleteResult;
    }

    /**
     * 修改
     * 
     * @param ip
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Result<?> update(@RequestParam(name = "ip") String ip,
            @RequestParam(name = "type") int type) {
        if ("".equals(ip) || type < 0) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Result<Integer> updateResult = serverDataService.updateServer(ip, type);
        return updateResult;
    }

    /**
     * 获取机器的详细报警配置
     * 
     * @param ip
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/alarm/config/detail", method = RequestMethod.GET)
    public Result<?> alarmConfigDetail(@RequestParam(name = "ip") String ip) {
        if (ip == "") {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Result<ServerAlarmConfig> configResult = serverAlarmConfigService.query(ip);
        return configResult;
    }

    /**
     * 修改报警配置
     * 
     * @param serverAlarmConfigParam
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/alarm/config/update", method = RequestMethod.POST)
    public Result<?> alarmConfigUpdate(@Valid ServerAlarmConfigParam serverAlarmConfigParam) {
        ServerAlarmConfig serverAlarmConfig = new ServerAlarmConfig();
        BeanUtils.copyProperties(serverAlarmConfigParam, serverAlarmConfig);
        String[] ipArr = serverAlarmConfigParam.getIpList().replace(" ", "").split(",");
        Result<Integer> updateResult = serverAlarmConfigService.update(serverAlarmConfig, Arrays.asList(ipArr));
        return updateResult;
    }

    /**
     * 获取机器类型
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/type", method = RequestMethod.GET)
    public Result<?> type() {
        return Result.getResult(getMachineTypeVO());
    }

    /**
     * 将enum转VO
     * 
     * @return
     */
    private List<MachineTypeVO> getMachineTypeVO() {
        List<MachineTypeVO> result = new ArrayList<MachineTypeVO>(MachineType.values().length);
        for (MachineType mt : MachineType.values()) {
            result.add(new MachineTypeVO(mt));
        }
        return result;
    }

    /**
     * 服务器信息概览
     * 
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping("/overview")
    public String overview(@RequestParam("ip") String ip,
            @RequestParam(value = "date", required = false) String date,
            Map<String, Object> map) {
        Date queryDate = null;
        if (date == null) {
            queryDate = new Date();
        } else {
            queryDate = DateUtil.parse(DateUtil.YMD_DASH, date);
        }
        // 获取服务器静态信息
        ServerInfo info = serverDataService.queryServerInfo(ip);
        if (info != null) {
            map.put("info", info);
            // 解析ulimit
            String ulimit = info.getUlimit();
            if (!StringUtils.isEmpty(ulimit)) {
                String[] tmp = ulimit.split(";");
                if (tmp.length == 2) {
                    String[] a = tmp[0].split(",");
                    if (a != null && a.length == 2) {
                        if ("f".equals(a[0])) {
                            map.put("file", a[1]);
                        }
                    }
                    a = tmp[1].split(",");
                    if (a != null && a.length == 2) {
                        if ("p".equals(a[0])) {
                            map.put("process", a[1]);
                        }
                    }
                }
            }
        }
        // 获取服务器状态
        List<ServerStatus> list = serverDataService.queryServerStat(ip, queryDate);
        map.put("date", date);
        setDataToMap(list, map);
        return adminViewModule() + "/overview";
    }

    /**
     * 服务器信息快速预览
     * 
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping("/preview")
    public String preview(@RequestParam("ip") String ip, Map<String, Object> map) {
        // 获取server信息
        ip = ip.split(":")[0];
        ServerInfo serverInfo = serverDataService.queryServerInfo(ip);
        if (serverInfo == null) {
            return adminViewModule() + "/preview";
        }
        setResult(map, "server", serverInfo);
        // 获取统计信息
        String time = DateUtil.getFormat(DateUtil.HHMM).format(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
        List<ServerStatus> list = serverDataService.queryServerStatByIp(ip, new Date(), time);
        ServerMetic serverMetic = new ServerMetic(list);
        setResult(map, serverMetic);
        return adminViewModule() + "/preview";
    }

    public void setDataToMap(List<ServerStatus> list, Map<String, Object> map) {
        // x轴坐标
        List<String> xAxis = new ArrayList<String>();

        // 1分钟最大load
        float maxLoad1 = 0;
        // load1总量
        double totalLoad1 = 0;

        // 最大user
        float maxUser = 0;
        // 最大sys
        float maxSys = 0;
        // 最大wio
        float maxWa = 0;

        // 当前可用内存
        float curFree = 0;
        // 最大内存使用量
        float maxUse = 0;
        // 最大内存cache量
        float maxCache = 0;
        // 最大内存buffer量
        float maxBuffer = 0;
        // 最大swap使用量
        float maxSwapUse = 0;

        // 最大网络流入速度
        float maxNetIn = 0;
        // 最大网络流出速度
        float maxNetOut = 0;
        // 最大连接ESTABLISHED数
        int maxConn = 0;
        // 最大连接TIME_WAIT数
        int maxWait = 0;
        // 最大连接ORPHAN数
        int maxOrphan = 0;

        // 最大读取速率
        float maxRead = 0;
        // 最大写入速率
        float maxWrite = 0;
        // 最繁忙程度
        float maxBusy = 0;
        // 最大iops量
        float maxIops = 0;

        // load serie
        Series<Float> load1Serie = new Series<Float>("1-min");
        Series<Float> load5Serie = new Series<Float>("5-min");
        Series<Float> load15Serie = new Series<Float>("15-min");

        // cpu serie
        Series<Float> userSerie = new Series<Float>("user");
        Series<Float> sysSerie = new Series<Float>("sys");
        Series<Float> waSerie = new Series<Float>("wa");

        // memory serie
        Series<Float> totalSerie = new Series<Float>("total");
        Series<Float> useSerie = new Series<Float>("use");
        useSerie.setType("area");
        Series<Float> cacheSerie = new Series<Float>("cache");
        cacheSerie.setType("area");
        Series<Float> bufferSerie = new Series<Float>("buffer");
        bufferSerie.setType("area");
        Series<Float> swapSerie = new Series<Float>("total");
        Series<Float> swapUseSerie = new Series<Float>("use");

        // net serie
        Series<Float> netInSerie = new Series<Float>("in");
        Series<Float> netOutSerie = new Series<Float>("out");

        // tcp serie
        Series<Integer> establishedSerie = new Series<Integer>("established");
        Series<Integer> twSerie = new Series<Integer>("time wait");
        Series<Integer> orphanSerie = new Series<Integer>("orphan");

        // disk serie
        Series<Float> readSerie = new Series<Float>("read");
        readSerie.setType("column");
        Series<Float> writeSerie = new Series<Float>("write");
        writeSerie.setType("column");
        Series<Float> busySerie = new Series<Float>("busy");
        busySerie.setYAxis(1);
        Series<Float> iopsSerie = new Series<Float>("iops");
        iopsSerie.setYAxis(2);

        for (int i = 0; i < list.size(); ++i) {
            ServerStatus ss = list.get(i);
            // x axis
            xAxis.add(ss.getCtime().substring(0, 2) + ":" + ss.getCtime().substring(2));
            // load相关
            load1Serie.addData(ss.getCload1());
            load5Serie.addData(ss.getCload5());
            load15Serie.addData(ss.getCload15());
            maxLoad1 = getBigger(maxLoad1, ss.getCload1());
            totalLoad1 += ss.getCload1();
            // cpu相关
            userSerie.addData(ss.getCuser());
            sysSerie.addData(ss.getCsys());
            waSerie.addData(ss.getCwio());
            maxUser = getBigger(maxUser, ss.getCuser());
            maxSys = getBigger(maxSys, ss.getCsys());
            maxWa = getBigger(maxWa, ss.getCwio());
            // memory相关
            totalSerie.addData(ss.getMtotal());
            float use = ss.getMtotal() - ss.getMfree() - ss.getMcache() - ss.getMbuffer();
            useSerie.addData(use);
            cacheSerie.addData(ss.getMcache());
            bufferSerie.addData(ss.getMbuffer());
            maxUse = getBigger(maxUse, use);
            maxCache = getBigger(maxCache, ss.getMcache());
            maxBuffer = getBigger(maxBuffer, ss.getMbuffer());
            if (i == list.size() - 1) {
                curFree = ss.getMtotal() - use;
            }
            // swap相关
            swapSerie.addData(ss.getMswap());
            float swapUse = ss.getMswap() - ss.getMswapFree();
            swapUse = floor(swapUse);
            swapUseSerie.addData(swapUse);
            maxSwapUse = getBigger(maxSwapUse, swapUse);
            // net相关
            netInSerie.addData(ss.getNin());
            netOutSerie.addData(ss.getNout());
            maxNetIn = getBigger(maxNetIn, ss.getNin());
            maxNetOut = getBigger(maxNetOut, ss.getNout());
            // tcp相关
            establishedSerie.addData(ss.getTuse());
            twSerie.addData(ss.getTwait());
            orphanSerie.addData(ss.getTorphan());
            maxConn = getBigger(maxConn, ss.getTuse());
            maxWait = getBigger(maxWait, ss.getTwait());
            maxOrphan = getBigger(maxOrphan, ss.getTorphan());
            // disk相关
            readSerie.addData(ss.getDread());
            writeSerie.addData(ss.getDwrite());
            busySerie.addData(ss.getDbusy());
            iopsSerie.addData(ss.getDiops());
            maxRead = getBigger(maxRead, ss.getDread());
            maxWrite = getBigger(maxWrite, ss.getDwrite());
            maxBusy = getBigger(maxBusy, ss.getDbusy());
            maxIops = getBigger(maxIops, ss.getDiops());
        }
        // x axis
        map.put("xAxis", JSONUtil.toJSONString(xAxis));
        // load
        map.put("load1", JSONUtil.toJSONString(load1Serie));
        map.put("load5", JSONUtil.toJSONString(load5Serie));
        map.put("load15", JSONUtil.toJSONString(load15Serie));
        map.put("maxLoad1", String.valueOf(maxLoad1));
        map.put("avgLoad1", format(totalLoad1, list.size()));
        // cpu
        map.put("user", JSONUtil.toJSONString(userSerie));
        map.put("sys", JSONUtil.toJSONString(sysSerie));
        map.put("wa", JSONUtil.toJSONString(waSerie));
        map.put("maxUser", String.valueOf(maxUser));
        map.put("maxSys", String.valueOf(maxSys));
        map.put("maxWa", String.valueOf(maxWa));
        // memory
        map.put("mtotal", JSONUtil.toJSONString(totalSerie));
        map.put("muse", JSONUtil.toJSONString(useSerie));
        map.put("mcache", JSONUtil.toJSONString(cacheSerie));
        map.put("mbuffer", JSONUtil.toJSONString(bufferSerie));
        map.put("curFree", format(curFree, 1024));
        map.put("maxUse", format(maxUse, 1024));
        map.put("maxCache", format(maxCache, 1024));
        map.put("maxBuffer", format(maxBuffer, 1024));
        // swap
        map.put("mswap", JSONUtil.toJSONString(swapSerie));
        map.put("mswapUse", JSONUtil.toJSONString(swapUseSerie));
        map.put("maxSwap", String.valueOf(maxSwapUse));
        // net
        map.put("nin", JSONUtil.toJSONString(netInSerie));
        map.put("nout", JSONUtil.toJSONString(netOutSerie));
        map.put("maxNetIn", format(maxNetIn, 1024));
        map.put("maxNetOut", format(maxNetOut, 1024));
        // tcp
        map.put("testab", JSONUtil.toJSONString(establishedSerie));
        map.put("twait", JSONUtil.toJSONString(twSerie));
        map.put("torph", JSONUtil.toJSONString(orphanSerie));
        map.put("maxConn", maxConn);
        map.put("maxWait", maxWait);
        map.put("maxOrphan", maxOrphan);
        // disk
        map.put("dread", JSONUtil.toJSONString(readSerie));
        map.put("dwrite", JSONUtil.toJSONString(writeSerie));
        map.put("dbusy", JSONUtil.toJSONString(busySerie));
        map.put("diops", JSONUtil.toJSONString(iopsSerie));
        map.put("maxRead", format(maxRead, 1024));
        map.put("maxWrite", format(maxWrite, 1024));
        map.put("maxBusy", String.valueOf(maxBusy));
        map.put("maxIops", String.valueOf(maxIops));
    }

    private String format(double a, int b) {
        if (b <= 0) {
            return "0";
        }
        return new DecimalFormat("0.0").format(a / b);
    }

    private float getBigger(float a, float b) {
        if (a > b) {
            return a;
        }
        return b;
    }

    private int getBigger(int a, int b) {
        if (a > b) {
            return a;
        }
        return b;
    }

    /**
     * 保留一位小数，四舍五入
     * 
     * @param v
     * @return
     */
    private float floor(float v) {
        return (float) (Math.round(v * 10)) / 10;
    }

    /**
     * 保留一位小数，四舍五入
     * 
     * @param v
     * @return
     */
    private String floor(double v) {
        double tmp = (double) (Math.round(v * 10)) / 10;
        if (tmp * 10 % 10 == 0) {
            return String.valueOf((long) tmp);
        }
        return String.valueOf(tmp);
    }

    @Override
    public String viewModule() {
        return "server";
    }

    /**
     * 获取服务器cpu各个核状态
     * 
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping("/cpu")
    public String cpu(@RequestParam("ip") String ip,
            @RequestParam("date") String date,
            Map<String, Object> map) {
        Date queryDate = DateUtil.parse(DateUtil.YMD_DASH, date);
        List<ServerStatus> list = serverDataService.queryServerStat(ip, queryDate);
        Map<String, CpuChart> subcpuMap = new TreeMap<String, CpuChart>();
        // x轴坐标
        List<String> xAxis = new ArrayList<String>();
        for (ServerStatus ss : list) {
            String subcpuString = ss.getcExt();
            String[] subCpuArray = subcpuString.split(";");
            xAxis.add(ss.getCtime());
            for (String subcpu : subCpuArray) {
                if (StringUtils.isEmpty(subcpu)) {
                    continue;
                }
                String[] cpu = subcpu.split(",");
                CpuChart cpuChart = subcpuMap.get(cpu[0]);
                if (cpuChart == null) {
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
        // x axis
        map.put("xAxis", JSONUtil.toJSONString(xAxis));
        map.put("cpu", subcpuMap.values());
        return adminViewModule() + "/cpu";
    }

    /**
     * 获取服务器各网卡状态
     * 
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping("/net")
    public String net(@RequestParam("ip") String ip,
            @RequestParam("date") String date,
            Map<String, Object> map) {
        Date queryDate = DateUtil.parse(DateUtil.YMD_DASH, date);
        List<ServerStatus> list = serverDataService.queryServerStat(ip, queryDate);
        Map<String, NetChart> subnetMap = new TreeMap<String, NetChart>();
        // x轴坐标
        List<String> xAxis = new ArrayList<String>();
        for (ServerStatus ss : list) {
            xAxis.add(ss.getCtime());
            addNetMap(ss.getNinExt(), subnetMap, true);
            addNetMap(ss.getNoutExt(), subnetMap, false);
        }
        // x axis
        map.put("xAxis", JSONUtil.toJSONString(xAxis));
        map.put("net", subnetMap.values());
        return adminViewModule() + "/net";
    }

    /**
     * parse net to map
     * 
     * @param netString
     * @param subnetMap
     * @param isIn
     */
    private void addNetMap(String netString, Map<String, NetChart> subnetMap, boolean isIn) {
        String[] subnetArray = netString.split(";");
        for (String subnet : subnetArray) {
            if (StringUtils.isEmpty(subnet)) {
                continue;
            }
            String[] net = subnet.split(",");
            NetChart netChart = subnetMap.get(net[0]);
            if (netChart == null) {
                netChart = new NetChart(net[0]);
                subnetMap.put(net[0], netChart);
            }
            float v = NumberUtils.toFloat(net[1]);
            if (isIn) {
                netChart.addInSeries(v);
                netChart.addTotalIn(v);
                netChart.setMaxIn(floor(v / 1024));
            } else {
                netChart.addOutSeries(v);
                netChart.addTotalOut(v);
                netChart.setMaxOut(floor(v / 1024));
            }
        }
    }

    /**
     * 获取硬盘各分区状态
     * 
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping("/disk")
    public String disk(@RequestParam("ip") String ip,
            @RequestParam("date") String date,
            Map<String, Object> map) {
        Date queryDate = DateUtil.parse(DateUtil.YMD_DASH, date);
        List<ServerStatus> list = serverDataService.queryServerStat(ip, queryDate);
        DiskChart readChart = new DiskChart();
        DiskChart writeChart = new DiskChart();
        DiskChart busyChart = new DiskChart();
        DiskChart iopsChart = new DiskChart();
        DiskChart spaceChart = new DiskChart();
        // x轴坐标
        List<String> xAxis = new ArrayList<String>();
        for (ServerStatus ss : list) {
            xAxis.add(ss.getCtime());
            // 解析use
            String dext = ss.getdExt();
            if (!StringUtils.isEmpty(dext)) {
                String[] items = dext.split(";");
                if (items != null) {
                    for (String item : items) {
                        String[] sds = item.split("=");
                        if (sds.length == 2) {
                            if ("DISKXFER".equals(sds[0])) {
                                addToChart(sds[1], iopsChart);
                            } else if ("DISKREAD".equals(sds[0])) {
                                addToChart(sds[1], readChart);
                            } else if ("DISKWRITE".equals(sds[0])) {
                                addToChart(sds[1], writeChart);
                            } else if ("DISKBUSY".equals(sds[0])) {
                                addToChart(sds[1], busyChart);
                            }
                        }
                    }
                }
            }
            // 解析space
            String space = ss.getDspace();
            addToChart(space, spaceChart);
        }
        // x axis
        map.put("xAxis", JSONUtil.toJSONString(xAxis));
        map.put("read", readChart);
        map.put("write", writeChart);
        map.put("busy", busyChart);
        map.put("iops", iopsChart);
        map.put("space", spaceChart);
        return adminViewModule() + "/disk";
    }

    private void addToChart(String line, DiskChart chart) {
        String[] parts = line.split(",");
        for (String part : parts) {
            if (StringUtils.isEmpty(part)) {
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
    public class NetChart {
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
            if (this.maxIn < in) {
                this.maxIn = in;
            }
        }

        public float getMaxOut() {
            return maxOut;
        }

        public void setMaxOut(float out) {
            if (this.maxOut < out) {
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
            return format(totalIn / 1024, inSeries.getData().size());
        }

        public String getAvgOut() {
            return format(totalOut / 1024, outSeries.getData().size());
        }
    }

    /**
     * disk chart
     */
    public class DiskChart {
        private float max;
        private float total;
        private Map<String, Series<Float>> seriesMap = new TreeMap<String, Series<Float>>();

        public void addSeries(String partition, float d) {
            Series<Float> series = seriesMap.get(partition);
            if (series == null) {
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
            if (this.max < max) {
                this.max = max;
            }
        }

        public String getAvg() {
            Collection<Series<Float>> coll = seriesMap.values();
            int size = 0;
            if (coll != null) {
                for (Series<Float> series : coll) {
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
    public class CpuChart {
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
            if (this.maxUser < user) {
                this.maxUser = user;
            }
        }

        public float getMaxSys() {
            return maxSys;
        }

        public void setMaxSys(float sys) {
            if (this.maxSys < sys) {
                this.maxSys = sys;
            }
        }

        public float getMaxWa() {
            return maxWa;
        }

        public void setMaxWa(float wa) {
            if (this.maxWa < wa) {
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
     * 
     * @param <T>
     */
    public class Series<T> {
        private String name;
        private List<T> data = new ArrayList<T>();
        private String type = "spline";
        private int yAxis;

        public String toJson() {
            return JSONUtil.toJSONString(this);
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

    public class ServerMetic {
        private QuotaMetric<Float> load1 = new QuotaMetric<>();
        private QuotaMetric<Float> load5 = new QuotaMetric<>();
        private QuotaMetric<Float> load15 = new QuotaMetric<>();
        private QuotaMetric<Float> cpuUser = new QuotaMetric<>();
        private QuotaMetric<Float> cpuSys = new QuotaMetric<>();
        private QuotaMetric<Float> cpuWio = new QuotaMetric<>();
        private QuotaMetric<Float> memUsePercent = new QuotaMetric<>();
        private QuotaMetric<Float> swapUsePercent = new QuotaMetric<>();
        private QuotaMetric<Float> netIn = new QuotaMetric<>();
        private QuotaMetric<Float> netOut = new QuotaMetric<>();
        private QuotaMetric<Integer> tcpEstablished = new QuotaMetric<>();
        private QuotaMetric<Integer> tcpWait = new QuotaMetric<>();
        private QuotaMetric<Float> ioRead = new QuotaMetric<>();
        private QuotaMetric<Float> ioWrite = new QuotaMetric<>();
        private QuotaMetric<Float> ioBusy = new QuotaMetric<>();
        private QuotaMetric<Float> iops = new QuotaMetric<>();

        public ServerMetic(List<ServerStatus> list) {
            for (ServerStatus ss : list) {
                // load
                load1.add(ss.getCload1());
                load5.add(ss.getCload5());
                load15.add(ss.getCload15());
                // cpu
                cpuUser.add(ss.getCuser());
                cpuSys.add(ss.getCsys());
                cpuWio.add(ss.getCwio());
                // memory
                memUsePercent.add((ss.getMtotal() - ss.getMfree()) / ss.getMtotal() * 100);
                swapUsePercent.add((ss.getMswap() - ss.getMswapFree()) / ss.getMswap() * 100);
                // net
                netIn.add(ss.getNin());
                netOut.add(ss.getNout());
                // tcp
                tcpEstablished.add(ss.getTuse());
                tcpWait.add(ss.getTwait());
                // io
                ioRead.add(ss.getDread());
                ioWrite.add(ss.getDwrite());
                ioBusy.add(ss.getDbusy());
                iops.add(ss.getDiops());
            }
        }

        public QuotaMetric<Float> getLoad1() {
            return load1;
        }

        public void setLoad1(QuotaMetric<Float> load1) {
            this.load1 = load1;
        }

        public QuotaMetric<Float> getLoad5() {
            return load5;
        }

        public void setLoad5(QuotaMetric<Float> load5) {
            this.load5 = load5;
        }

        public QuotaMetric<Float> getLoad15() {
            return load15;
        }

        public void setLoad15(QuotaMetric<Float> load15) {
            this.load15 = load15;
        }

        public QuotaMetric<Float> getCpuUser() {
            return cpuUser;
        }

        public void setCpuUser(QuotaMetric<Float> cpuUser) {
            this.cpuUser = cpuUser;
        }

        public QuotaMetric<Float> getCpuSys() {
            return cpuSys;
        }

        public void setCpuSys(QuotaMetric<Float> cpuSys) {
            this.cpuSys = cpuSys;
        }

        public QuotaMetric<Float> getCpuWio() {
            return cpuWio;
        }

        public void setCpuWio(QuotaMetric<Float> cpuWio) {
            this.cpuWio = cpuWio;
        }

        public QuotaMetric<Float> getMemUsePercent() {
            return memUsePercent;
        }

        public void setMemUsePercent(QuotaMetric<Float> memUsePercent) {
            this.memUsePercent = memUsePercent;
        }

        public QuotaMetric<Float> getSwapUsePercent() {
            return swapUsePercent;
        }

        public void setSwapUsePercent(QuotaMetric<Float> swapUsePercent) {
            this.swapUsePercent = swapUsePercent;
        }

        public QuotaMetric<Float> getNetIn() {
            return netIn;
        }

        public void setNetIn(QuotaMetric<Float> netIn) {
            this.netIn = netIn;
        }

        public QuotaMetric<Float> getNetOut() {
            return netOut;
        }

        public void setNetOut(QuotaMetric<Float> netOut) {
            this.netOut = netOut;
        }

        public QuotaMetric<Integer> getTcpEstablished() {
            return tcpEstablished;
        }

        public void setTcpEstablished(QuotaMetric<Integer> tcpEstablished) {
            this.tcpEstablished = tcpEstablished;
        }

        public QuotaMetric<Integer> getTcpWait() {
            return tcpWait;
        }

        public void setTcpWait(QuotaMetric<Integer> tcpWait) {
            this.tcpWait = tcpWait;
        }

        public QuotaMetric<Float> getIoRead() {
            return ioRead;
        }

        public void setIoRead(QuotaMetric<Float> ioRead) {
            this.ioRead = ioRead;
        }

        public QuotaMetric<Float> getIoWrite() {
            return ioWrite;
        }

        public void setIoWrite(QuotaMetric<Float> ioWrite) {
            this.ioWrite = ioWrite;
        }

        public QuotaMetric<Float> getIoBusy() {
            return ioBusy;
        }

        public void setIoBusy(QuotaMetric<Float> ioBusy) {
            this.ioBusy = ioBusy;
        }

        public QuotaMetric<Float> getIops() {
            return iops;
        }

        public void setIops(QuotaMetric<Float> iops) {
            this.iops = iops;
        }
    }

    /**
     * 指标度量
     * 
     * @author yongfeigao
     * @date 2020年4月22日
     * @param <T>
     */
    public class QuotaMetric<T extends Number> {
        private List<T> quotaList = new ArrayList<>();

        public void add(T t) {
            quotaList.add(t);
        }

        public double getTotal() {
            double total = 0;
            for (T t : quotaList) {
                total += t.doubleValue();
            }
            return total;
        }

        public String getAvgString() {
            return floor(getAvg());
        }

        private double getAvg() {
            return getTotal() / quotaList.size();
        }

        public String getAvgFormat() {
            return format(getAvg());
        }

        public String format(double v) {
            if (v >= 1048576) {
                return floor(v / 1048576F) + "g";
            }
            if (v >= 1024) {
                return floor(v / 1024F) + "m";
            }
            return floor(v) + "k";
        }

        public String getMaxFormat() {
            return format(getMax());
        }

        public String getMaxString() {
            return floor(getMax());
        }

        private double getMax() {
            double max = 0;
            for (T t : quotaList) {
                double tmp = t.doubleValue();
                if (tmp > max) {
                    max = tmp;
                }
            }
            return max;
        }
    }
}
