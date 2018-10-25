package com.sohu.tv.mq.cloud.web.view.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.service.BrokerTrafficService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.view.SearchHeader;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.DateSearchField;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.HiddenSearchField;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.SearchField;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart.XAxis;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart.YAxis;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart.YAxisGroup;
import com.sohu.tv.mq.cloud.web.view.chart.LineChartData;

/**
 * broker流量数据
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月29日
 */
@Component
public class ClusterTrafficLineChartData implements LineChartData {

    // 搜索区域
    private SearchHeader searchHeader;

    public static final String DATE_FIELD = "date";
    public static final String CLUSTER_ID_FIELD = "clusterId";
    public static final String DATE_FIELD_TITLE = "日期";

    // x轴数据
    private List<String> xDataList;

    // x轴格式化后的数据
    private List<String> xDataFormatList;

    @Autowired
    private BrokerTrafficService brokerTrafficService;

    public ClusterTrafficLineChartData() {
        initSearchHeader();
    }

    /**
     * 初始化搜索数据
     */
    public void initSearchHeader() {
        searchHeader = new SearchHeader();
        List<SearchField> searchFieldList = new ArrayList<SearchHeader.SearchField>();

        // time
        DateSearchField dateSearchField = new DateSearchField();
        dateSearchField.setKey(DATE_FIELD);
        dateSearchField.setTitle(DATE_FIELD_TITLE);
        searchFieldList.add(dateSearchField);

        // hidden
        HiddenSearchField hiddenSearchField = new HiddenSearchField();
        hiddenSearchField.setKey(CLUSTER_ID_FIELD);
        searchFieldList.add(hiddenSearchField);

        searchHeader.setSearchFieldList(searchFieldList);

        // 初始化x轴数据，因为x轴数据是固定的
        xDataFormatList = new ArrayList<String>();
        xDataList = new ArrayList<String>();
        for (int i = 0; i < 23; ++i) {
            for (int j = 0; j < 60; ++j) {
                String hour = i < 10 ? "0" + i : "" + i;
                String ninutes = j < 10 ? "0" + j : "" + j;
                xDataList.add(hour + ninutes);
                xDataFormatList.add(hour + ":" + ninutes);
            }
        }
    }

    @Override
    public String getPath() {
        return "cluster";
    }

    @Override
    public String getPageTitle() {
        return "cluster流量";
    }

    @Override
    public List<LineChart> getLineChartData(Map<String, Object> searchMap) {
        // 多个曲线图列表 - 单x多series双y轴
        List<LineChart> lineChartList = new ArrayList<LineChart>();
        
        // 解析参数
        Date date = getDate(searchMap, DATE_FIELD);
        String dateStr = DateUtil.formatYMD(date);
        int clusterId = getIntValue(searchMap, CLUSTER_ID_FIELD);
        
        //获取流量
        Result<List<BrokerTraffic>> result = brokerTrafficService.queryClusterTraffic(clusterId, dateStr);
        if (!result.isOK()) {
            return lineChartList;
        }

        Date dayBefore = new Date(date.getTime() - 24*60*60*1000);
        //获取前一天流量
        Result<List<BrokerTraffic>> resultDayBefore = brokerTrafficService.queryClusterTraffic(clusterId, 
                DateUtil.formatYMD(dayBefore));
        
        // 构造曲线图对象
        LineChart lineChart = new LineChart();
        lineChart.setChartId("clusterTraffic");
//        lineChart.setTitle(MQCluster.getMQClusterById(clusterId).getClusterName()+"流量情况");
        lineChart.setOneline(true);
        lineChart.setTickInterval(6);
        XAxis xAxis = new XAxis();
        xAxis.setxList(xDataFormatList);
        lineChart.setxAxis(xAxis);
        
        // 将list转为map方便数据查找
        Map<String, BrokerTraffic> trafficMap = list2Map(result.getResult());
        Map<String, BrokerTraffic> trafficMapDayBefore = list2Map(
                resultDayBefore.isOK() ? resultDayBefore.getResult() : null);
        // 填充y轴数据
        List<Number> putCountList = new ArrayList<Number>();
        List<Number> getCountList = new ArrayList<Number>();
        List<Number> putSizeList = new ArrayList<Number>();
        List<Number> getSizeList = new ArrayList<Number>();
        List<Number> putCountListDayBefore = new ArrayList<Number>();
        List<Number> getCountListDayBefore = new ArrayList<Number>();
        List<Number> putSizeListDayBefore = new ArrayList<Number>();
        List<Number> getSizeListDayBefore = new ArrayList<Number>();
        long maxPutCount = 0;
        long maxGetCount = 0;
        long maxPutSize = 0;
        long maxGetSize = 0;
        long maxPutCountDayBefore = 0;
        long maxGetCountDayBefore = 0;
        long maxPutSizeDayBefore = 0;
        long maxGetSizeDayBefore = 0;
        long totalGetCount = 0;
        long totalPutCount = 0;
        long totalPutSize = 0;
        long totalGetSize = 0;
        long totalPutCountDayBefore = 0;
        long totalGetCountDayBefore = 0;
        long totalPutSizeDayBefore = 0;
        long totalGetSizeDayBefore = 0;
        for (String time : xDataList) {
            // put count
            long putCount = setPutCountData(trafficMap.get(time), putCountList);
            totalPutCount += putCount;
            if (maxPutCount < putCount) {
                maxPutCount = putCount;
            }
            // get count
            long getCount = setGetCountData(trafficMap.get(time), getCountList);
            totalGetCount += getCount;
            if (maxGetCount < getCount) {
                maxGetCount = getCount;
            }
            // put size
            long putSize = setPutSizeData(trafficMap.get(time), putSizeList);
            totalPutSize += putSize;
            if(maxPutSize < putSize) {
                maxPutSize = putSize;
            }
            // get size
            long getSize = setGetSizeData(trafficMap.get(time), getSizeList);
            totalGetSize += getSize;
            if(maxGetSize < getSize) {
                maxGetSize = getSize;
            }
            // get count
            long getCountDayBefore = setGetCountData(trafficMapDayBefore.get(time), getCountListDayBefore);
            totalGetCountDayBefore += getCountDayBefore;
            if(maxGetCountDayBefore < getCountDayBefore) {
                maxGetCountDayBefore = getCountDayBefore;
            }
            // put count
            long putCountDayBefore = setPutCountData(trafficMapDayBefore.get(time), putCountListDayBefore);
            totalPutCountDayBefore += putCountDayBefore;
            if(maxPutCountDayBefore < putCountDayBefore) {
                maxPutCountDayBefore = putCountDayBefore;
            }
            // get size
            long getSizeDayBefore = setGetSizeData(trafficMapDayBefore.get(time), getSizeListDayBefore);
            totalGetSizeDayBefore += getSizeDayBefore;
            if(maxGetSizeDayBefore < getSizeDayBefore) {
                maxGetSizeDayBefore = getSizeDayBefore;
            }
            // put size
            long putSizeDayBefore = setPutSizeData(trafficMapDayBefore.get(time), putSizeListDayBefore);
            totalPutSizeDayBefore += putSizeDayBefore;
            if(maxPutSizeDayBefore < putSizeDayBefore) {
                maxPutSizeDayBefore = putSizeDayBefore;
            }
        }

        String curDate = DateUtil.formatYMD(date);
        String curDateBefore = DateUtil.formatYMD(dayBefore);
        // 设置今日put消息量y轴
        List<YAxis> putCountYAxisList = new ArrayList<YAxis>();
        YAxis putCountYAxis = new YAxis();
        putCountYAxis.setName(curDate + "-生产量");
        putCountYAxis.setData(putCountList);
        putCountYAxisList.add(putCountYAxis);
        
        YAxis putCountYAxisDayBefore = new YAxis();
        putCountYAxisDayBefore.setName(curDateBefore + "-生产量");
        putCountYAxisDayBefore.setData(putCountListDayBefore);
        putCountYAxisList.add(putCountYAxisDayBefore);
        
        // 设置get消息量y轴
        List<YAxis> getCountYAxisList = new ArrayList<YAxis>();
        YAxis getCountYAxis = new YAxis();
        getCountYAxis.setName(curDate + "-消费量");
        getCountYAxis.setData(getCountList);
        getCountYAxisList.add(getCountYAxis);
        
        YAxis getCountYAxisDayBefore = new YAxis();
        getCountYAxisDayBefore.setName(curDateBefore + "-消费量");
        getCountYAxisDayBefore.setData(getCountListDayBefore);
        getCountYAxisList.add(getCountYAxisDayBefore);

        // 生成y轴数据组对象
        YAxisGroup countYAxisGroup = new YAxisGroup();
        countYAxisGroup.setGroupName("消息量");
        countYAxisGroup.setyAxisList(putCountYAxisList);
        countYAxisGroup.getyAxisList().addAll(getCountYAxisList);

        // 设置put消息大小y轴
        List<YAxis> putSizeYAxisList = new ArrayList<YAxis>();
        YAxis putSizeYAxis = new YAxis();
        putSizeYAxis.setName(curDate + "-生产大小");
        putSizeYAxis.setData(putSizeList);
        putSizeYAxisList.add(putSizeYAxis);
        
        YAxis putSizeYAxisDayBefore = new YAxis();
        putSizeYAxisDayBefore.setName(curDateBefore + "-生产大小");
        putSizeYAxisDayBefore.setData(putSizeListDayBefore);
        putSizeYAxisList.add(putSizeYAxisDayBefore);
        
        List<YAxis> getSizeYAxisList = new ArrayList<YAxis>();
        YAxis getSizeYAxis = new YAxis();
        getSizeYAxis.setName(curDate + "-消费大小");
        getSizeYAxis.setData(getSizeList);
        getSizeYAxisList.add(getSizeYAxis);
        
        YAxis getSizeYAxisDayBefore = new YAxis();
        getSizeYAxisDayBefore.setName(curDateBefore + "-消费大小");
        getSizeYAxisDayBefore.setData(getSizeListDayBefore);
        getSizeYAxisList.add(getSizeYAxisDayBefore);

        // 生成y轴数据组对象
        YAxisGroup sizeYAxisGroup = new YAxisGroup();
        sizeYAxisGroup.setGroupName("消息大小");
        sizeYAxisGroup.setOpposite(true);
        sizeYAxisGroup.setTraffic(true);
        sizeYAxisGroup.setyAxisList(putSizeYAxisList);
        sizeYAxisGroup.getyAxisList().addAll(getSizeYAxisList);

        // 设置双y轴
        List<YAxisGroup> yAxisGroupList = new ArrayList<YAxisGroup>();
        yAxisGroupList.add(countYAxisGroup);
        yAxisGroupList.add(sizeYAxisGroup);
        lineChart.setyAxisGroupList(yAxisGroupList);

        lineChart.setHeight(500);
        lineChart.setSubTitle("<table cellspacing='0' cellpadding='0' style='background-color: #f5f5f5'><thead><tr>"
                + "<td>日期</td><td>来源</td><td>消息量峰值</td><td>消息总量</td><td>消息大小峰值</td><td>消息总大小</td></tr></thead>"
                + "<tbody><tr><td rowspan=2>"+curDate+"</td><td>生产</td>"
                + "<td>"+formatCount(maxPutCount)+"/分</td>"
                + "<td>"+formatCount(totalPutCount)+"</td>"
                + "<td>"+formatSize(maxPutSize)+"/分</td>"
                + "<td>"+formatSize(totalPutSize)+"</td>"
                + "</tr><tr><td>消费</td>"
                + "<td>"+formatCount(maxGetCount)+"/分</td>"
                + "<td>"+formatCount(totalGetCount)+"</td>"
                + "<td>"+formatSize(maxGetSize)+"/分</td>"
                + "<td>"+formatSize(totalGetSize)+"</td>"
                + "</tr><tr><td rowspan=2>"+curDateBefore+"</td><td>生产</td>"
                + "<td>"+formatCount(maxPutCountDayBefore)+"/分</td>"
                + "<td>"+formatCount(totalPutCountDayBefore)+"</td>"
                + "<td>"+formatSize(maxPutSizeDayBefore)+"/分</td>"
                + "<td>"+formatSize(totalPutSizeDayBefore)+"</td>"
                + "</tr><tr><td>消费</td>"
                + "<td>"+formatCount(maxGetCountDayBefore)+"/分</td>"
                + "<td>"+formatCount(totalGetCountDayBefore)+"</td>"
                + "<td>"+formatSize(maxGetSizeDayBefore)+"/分</td>"
                + "<td>"+formatSize(totalGetSizeDayBefore)+"</td>"
                + "</tr></tbody></table>");
        lineChartList.add(lineChart);
        
        return lineChartList;
    }
    
    /**
     * 格式化消息数量
     * @param maxCount
     * @return
     */
    private String formatCount(long maxCount) {
        String maxCountShow = "";
        if(maxCount > 100000000) {
            maxCountShow = String.format("%.2f", maxCount / 100000000F) + "亿("+maxCount+")";
        } else if(maxCount > 10000) {
            maxCountShow = String.format("%.2f", maxCount / 10000F) + "万("+maxCount+")";
        } else {
            maxCountShow = String.valueOf(maxCount);
        }
        return maxCountShow;
    }
    
    /**
     * 格式化消息大小
     * @param maxSize
     * @return
     */
    private String formatSize(long maxSize) {
        String maxSizeShow = "";
        if(maxSize > 1073741824) {
            maxSizeShow = String.format("%.2f", maxSize / 1073741824F) + "G";
        } else if(maxSize > 1048576) {
            maxSizeShow = String.format("%.2f", maxSize / 1048576F) + "M";
        } else if (maxSize > 1024) {
            maxSizeShow = String.format("%.2f", maxSize / 1024F) + "K";
        } else {
            maxSizeShow = maxSize + "B";
        }
        return maxSizeShow;
    }

    private long setPutSizeData(BrokerTraffic traffic, List<Number> sizeList) {
        if (traffic == null) {
            sizeList.add(0);
            return 0;
        } else {
            sizeList.add(traffic.getPutSize());
            return traffic.getPutSize();
        }
    }
    
    private long setGetSizeData(BrokerTraffic traffic, List<Number> sizeList) {
        if (traffic == null) {
            sizeList.add(0);
            return 0;
        } else {
            sizeList.add(traffic.getGetSize());
            return traffic.getGetSize();
        }
    }
    
    private long setPutCountData(BrokerTraffic traffic, List<Number> countList) {
        if (traffic == null) {
            countList.add(0);
            return 0;
        } else {
            countList.add(traffic.getPutCount());
            return traffic.getPutCount();
        }
    }

    private long setGetCountData(BrokerTraffic traffic, List<Number> countList) {
        if (traffic == null) {
            countList.add(0);
            return 0;
        } else {
            countList.add(traffic.getGetCount());
            return traffic.getGetCount();
        }
    }

    private Map<String, BrokerTraffic> list2Map(List<BrokerTraffic> list) {
        Map<String, BrokerTraffic> map = new TreeMap<String, BrokerTraffic>();
        if (list == null) {
            return map;
        }
        for (BrokerTraffic traffic : list) {
            map.put(traffic.getCreateTime(), traffic);
        }
        return map;
    }
    
    /**
     * 获取数据
     * 
     * @param searchMap
     * @param key
     * @return
     */
    protected String getValue(Map<String, Object> searchMap, String key) {
        if (searchMap == null) {
            return null;
        }
        Object obj = searchMap.get(key);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    /**
     * 获取日期数据
     * 
     * @param searchMap
     * @param key
     * @return
     */
    protected Date getDate(Map<String, Object> searchMap, String key) {
        if (searchMap == null) {
            return new Date();
        }
        Object obj = searchMap.get(key);
        if (obj == null) {
            return new Date();
        }
        String date = obj.toString();
        if (!StringUtils.isEmpty(date)) {
            return DateUtil.parseYMD(date);
        }
        return new Date();
    }

    @Override
    public SearchHeader getSearchHeader() {
        return searchHeader;
    }
    
    /**
     * 获取数据
     * 
     * @param searchMap
     * @param key
     * @return
     */
    protected int getIntValue(Map<String, Object> searchMap, String key) {
        if (searchMap == null) {
            return 0;
        }
        Object obj = searchMap.get(key);
        if (obj == null) {
            return 0;
        }
        return NumberUtils.toInt(obj.toString());
    }

}
