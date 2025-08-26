package com.sohu.tv.mq.cloud.web.view.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.common.model.BrokerStoreStat;
import com.sohu.tv.mq.cloud.service.BrokerStoreStatService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.view.SearchHeader;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.DateSearchField;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.HiddenSearchField;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.SearchField;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart.Tip;
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
public class BrokerStoreStatLineChartData implements LineChartData {

    // 搜索区域
    private SearchHeader searchHeader;

    public static final String DATE_FIELD = "date";
    public static final String BROKER_IP_FIELD = "brokerIp";
    public static final String DATE_FIELD_TITLE = "日期";

    // x轴数据
    private List<String> xDataList;

    // x轴格式化后的数据
    private List<String> xDataFormatList;

    @Autowired
    private BrokerStoreStatService brokerStoreStatService;

    public BrokerStoreStatLineChartData() {
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
        hiddenSearchField.setKey(BROKER_IP_FIELD);
        searchFieldList.add(hiddenSearchField);

        searchHeader.setSearchFieldList(searchFieldList);

        // 初始化x轴数据，因为x轴数据是固定的
        xDataFormatList = new ArrayList<String>();
        xDataList = new ArrayList<String>();
        for (int i = 0; i < 24; ++i) {
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
        return "broker_store";
    }

    @Override
    public String getPageTitle() {
        return "broker存储耗时";
    }

    @Override
    public List<LineChart> getLineChartData(Map<String, Object> searchMap) {
        // 多个曲线图列表 - 单x多series双y轴
        List<LineChart> lineChartList = new ArrayList<LineChart>();
        
        // 解析参数
        Date date = getDate(searchMap, DATE_FIELD);
        String brokerIp = MapUtils.getString(searchMap, BROKER_IP_FIELD);
        if (brokerIp == null) {
            return lineChartList;
        }
        
        //获取流量
        Result<List<BrokerStoreStat>> result = brokerStoreStatService.query(brokerIp, date);
        if (!result.isOK()) {
            return lineChartList;
        }

        // 构造曲线图对象
        LineChart lineChart = new LineChart();
        lineChart.setChartId("brokerStoreTraffic");
        lineChart.setOneline(true);
        lineChart.setTickInterval(6);
        XAxis xAxis = new XAxis();
        xAxis.setxList(xDataFormatList);
        lineChart.setxAxis(xAxis);
        
        // 将list转为map方便数据查找
        Map<String, BrokerStoreStat> trafficMap = list2Map(result.getResult());
        // 填充y轴数据
        List<Number> avgList = new ArrayList<Number>();
        List<Number> percent90List = new ArrayList<Number>();
        List<Number> percent99List = new ArrayList<Number>();
        List<Number> maxList = new ArrayList<Number>();
        List<Number> totalCountList = new ArrayList<Number>();
        long max = 0;
        long maxTotal = 0;
        long totalCount = 0;
        for (String time : xDataList) {
            // avg
            setAvgCountData(trafficMap.get(time), avgList);
            
            // percent90
            setPercent90Data(trafficMap.get(time), percent90List);
            
            // percent99
            setPercent99Data(trafficMap.get(time), percent99List);

            // max
            long maxCost = setMaxData(trafficMap.get(time), maxList);
            if(max < maxCost) {
                max = maxCost;
            }
            
            // total
            long total = setTotalData(trafficMap.get(time), totalCountList);
            totalCount += total;
            if(maxTotal < total) {
                maxTotal = total;
            }
        }

        // 设置avg y轴
        List<YAxis> avgYAxisList = new ArrayList<YAxis>();
        avgYAxisList.add(getYAxis("avg", avgList));
        
        
        // 设置90%y轴
        List<YAxis> percent90YAxisList = new ArrayList<YAxis>();
        percent90YAxisList.add(getYAxis("90%", percent90List));
        
        // 设置99%y轴
        List<YAxis> percent99YAxisList = new ArrayList<YAxis>();
        percent99YAxisList.add(getYAxis("99%", percent99List));
        
        // 设置max y轴
        List<YAxis> maxYAxisList = new ArrayList<YAxis>();
        maxYAxisList.add(getYAxis("max", maxList));
        
        // 生成y轴数据组对象
        YAxisGroup costYAxisGroup = new YAxisGroup();
        costYAxisGroup.setGroupName("耗时[ms]");
        costYAxisGroup.setyAxisList(avgYAxisList);
        costYAxisGroup.getyAxisList().addAll(percent90YAxisList);
        costYAxisGroup.getyAxisList().addAll(percent99YAxisList);
        costYAxisGroup.getyAxisList().addAll(maxYAxisList);

        // 设置调用量 y轴
        List<YAxis> totalCountYAxisList = new ArrayList<YAxis>();
        totalCountYAxisList.add(getYAxis("写入", "次", totalCountList));
        
        // 生成y轴数据组对象
        YAxisGroup totalCountYAxisGroup = new YAxisGroup();
        totalCountYAxisGroup.setGroupName("次数");
        totalCountYAxisGroup.setOpposite(true);
        totalCountYAxisGroup.setyAxisList(totalCountYAxisList);

        // 设置双y轴
        List<YAxisGroup> yAxisGroupList = new ArrayList<YAxisGroup>();
        yAxisGroupList.add(costYAxisGroup);
        yAxisGroupList.add(totalCountYAxisGroup);
        lineChart.setyAxisGroupList(yAxisGroupList);

        lineChart.setHeight(500);
        lineChart.setOverview("<table class='table table-sm'><thead><tr>"
                + "<th>最大耗时</th><th>最大写入</th><th>总写入</th></tr></thead>"
                + "<tbody><tr>"
                + "<td title='"+max+"'>"+formatCount(max)+"ms</td>"
                + "<td title='"+maxTotal+"'>"+formatCount(maxTotal)+"次/分</td>"
                + "<td title='"+totalCount+"'>"+formatCount(totalCount)+"次</td>"
                + "</tr></tbody></table>");
        lineChartList.add(lineChart);
        
        return lineChartList;
    }
    
    private YAxis getYAxis(String name, List<?> data) {
        return getYAxis(name, "ms", data);
    }
    
    private YAxis getYAxis(String name, String tip, List<?> data) {
        YAxis yAxis = new YAxis();
        yAxis.setName(name);
        yAxis.setData(data);
        Tip tooltip = new Tip();
        tooltip.setValueSuffix(tip);
        yAxis.setTooltip(tooltip);
        return yAxis;
    }
    
    /**
     * 格式化数量
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
    
    private void setPercent99Data(BrokerStoreStat traffic, List<Number> sizeList) {
        if (traffic == null) {
            sizeList.add(0);
        } else {
            sizeList.add(traffic.getPercent99());
        }
    }
    
    private long setMaxData(BrokerStoreStat traffic, List<Number> list) {
        if (traffic == null) {
            list.add(0);
            return 0;
        } else {
            list.add(traffic.getMax());
            return traffic.getMax();
        }
    }
    
    private void setAvgCountData(BrokerStoreStat traffic, List<Number> list) {
        if (traffic == null) {
            list.add(0);
        } else {
            list.add(traffic.getAvg());
        }
    }

    private void setPercent90Data(BrokerStoreStat traffic, List<Number> list) {
        if (traffic == null) {
            list.add(0);
        } else {
            list.add(traffic.getPercent90());
        }
    }
    
    private long setTotalData(BrokerStoreStat traffic, List<Number> list) {
        if (traffic == null) {
            list.add(0);
            return 0;
        } else {
            list.add(traffic.getCount());
            return traffic.getCount();
        }
    }

    private Map<String, BrokerStoreStat> list2Map(List<BrokerStoreStat> list) {
        Map<String, BrokerStoreStat> map = new TreeMap<String, BrokerStoreStat>();
        if (list == null) {
            return map;
        }
        for (BrokerStoreStat traffic : list) {
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

    @Override
    public SearchHeader getSearchHeader() {
        return searchHeader;
    }
    
}
