package com.sohu.tv.mq.cloud.web.view.data;

import com.sohu.tv.mq.cloud.bo.ConsumerClientMetrics;
import com.sohu.tv.mq.cloud.service.ConsumerClientMetricsService;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;

/**
 * 消费者客户端状态数据
 *
 * @author yongfeigao
 * @date 2023/9/27
 */
@Component
public class ConsumerStatsLineChartData implements LineChartData {
    private Logger logger = LoggerFactory.getLogger(getClass());

    // 搜索区域
    private SearchHeader searchHeader;

    public static final String DATE_FIELD = "date";
    public static final String CONSUMER_FIELD = "consumerGroupStats";
    public static final String DATE_FIELD_TITLE = "日期";

    // x轴数据
    private List<String> xDataList;

    // x轴格式化后的数据
    private List<String> xDataFormatList;

    @Autowired
    private ConsumerClientMetricsService consumerClientMetricsService;

    public ConsumerStatsLineChartData() {
        initSearchHeader();
    }

    /**
     * 初始化搜索数据
     */
    public void initSearchHeader() {
        searchHeader = new SearchHeader();
        List<SearchField> searchFieldList = new ArrayList<SearchField>();

        // time
        DateSearchField dateSearchField = new DateSearchField();
        dateSearchField.setKey(DATE_FIELD);
        dateSearchField.setTitle(DATE_FIELD_TITLE);
        dateSearchField.setDaysBefore(10);
        searchFieldList.add(dateSearchField);

        // hidden
        HiddenSearchField hiddenSearchField = new HiddenSearchField();
        hiddenSearchField.setKey(CONSUMER_FIELD);
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
        return "cstats";
    }

    @Override
    public String getPageTitle() {
        return "消费者客户端统计";
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LineChart> getLineChartData(Map<String, Object> searchMap) {
        List<LineChart> lineChartList = new ArrayList<LineChart>();

        // 解析参数
        Date date = getDate(searchMap, DATE_FIELD);
        String consumer = searchMap.get(CONSUMER_FIELD).toString();

        //获取总体统计
        Result<List<ConsumerClientMetrics>> result = consumerClientMetricsService.query(consumer, date);
        if (result.isEmpty()) {
            return lineChartList;
        }
        // 转为map
        Map<String, Map<String, ConsumerClientMetrics>> totalMap = list2Map(consumer, date, result.getResult());

        int index = 1;
        for (String client : totalMap.keySet()) {
            long clientTotalCount = 0;
            // 构造曲线图对象
            LineChart lineChart = new LineChart();
            lineChart.setChartId("total" + index++);
            lineChart.setTitle(client + "每分钟耗时");
            lineChart.setOneline(true);
            lineChart.setTickInterval(6);

            XAxis xAxis = new XAxis();
            xAxis.setxList(xDataFormatList);
            lineChart.setxAxis(xAxis);

            // 设置耗时y轴列表
            List<YAxis> yAxisList = new ArrayList<YAxis>();
            // 生成耗时y轴数据组
            YAxisGroup costYAxisGroup = new YAxisGroup();
            costYAxisGroup.setGroupName("耗时(ms)");
            costYAxisGroup.setyAxisList(yAxisList);

            // 设置调用量y轴列表
            List<YAxis> countYAxisList = new ArrayList<YAxis>();
            // 生成调用量y轴数据组对象
            YAxisGroup countYAxisGroup = new YAxisGroup();
            countYAxisGroup.setGroupName("次数");
            countYAxisGroup.setOpposite(true);
            countYAxisGroup.setyAxisList(countYAxisList);

            // 设置调用量y轴
            YAxis countYaxis = new YAxis();
            countYaxis.setName("调用量");
            countYaxis.setColor("#0099FF");
            List<Map<String, Object>> countDataList = new ArrayList<Map<String, Object>>();
            countYaxis.setData(countDataList);
            countYAxisList.add(countYaxis);

            // 设置y轴
            List<YAxisGroup> yAxisGroupList = new ArrayList<YAxisGroup>();
            yAxisGroupList.add(costYAxisGroup);
            yAxisGroupList.add(countYAxisGroup);
            lineChart.setyAxisGroupList(yAxisGroupList);

            YAxis maxYaxis = new YAxis();
            maxYaxis.setName("max");
            maxYaxis.setColor("green");
            List<Number> maxDataList = new ArrayList<Number>();
            maxYaxis.setData(maxDataList);
            yAxisList.add(maxYaxis);

            YAxis avgYaxis = new YAxis();
            avgYaxis.setName("avg");
            avgYaxis.setColor("#28a745");
            List<Number> avgDataList = new ArrayList<Number>();
            avgYaxis.setData(avgDataList);
            yAxisList.add(avgYaxis);

            long maxCount = 0;
            int maxCost = 0;
            Map<String, ConsumerClientMetrics> timeMap = totalMap.get(client);
            for (int i = 0; i < xDataList.size(); ++i) {
                String time = xDataList.get(i);
                ConsumerClientMetrics metrics = timeMap.get(time);
                Map<String, Object> countMap = new HashMap<String, Object>();
                if (metrics == null) {
                    maxDataList.add(0);
                    avgDataList.add(0);
                    countMap.put("y", 0);
                    countDataList.add(countMap);
                } else {
                    maxDataList.add(metrics.getMax());
                    avgDataList.add(metrics.getAvg());

                    countMap.put("y", metrics.getCount());
                    countMap.put("id", metrics.getId());
                    countDataList.add(countMap);

                    if (maxCount < metrics.getCount()) {
                        maxCount = metrics.getCount();
                    }
                    clientTotalCount += metrics.getCount();

                    if (metrics.getException() != null) {
                        Map<String, Object> markerMap = new HashMap<String, Object>();
                        markerMap.put("symbol", "circle");
                        markerMap.put("fillColor", "red");
                        markerMap.put("enabled", true);
                        countMap.put("marker", markerMap);
                        countMap.put("dt", metrics.getException());
                    }
                }
            }
            lineChart.setSubTitle("总调用量: " + formatCount(clientTotalCount) + " 最大调用量: " + maxCount + "次/分钟 单次最大耗时: " + maxCost + "ms");
            lineChartList.add(lineChart);
        }

        return lineChartList;
    }

    /**
     * 格式化消息数量
     *
     * @param maxCount
     * @return
     */
    private String formatCount(long maxCount) {
        String maxCountShow = "";
        if (maxCount > 100000000) {
            maxCountShow = String.format("%.2f", maxCount / 100000000F) + "亿(" + maxCount + ")";
        } else if (maxCount > 10000) {
            maxCountShow = String.format("%.2f", maxCount / 10000F) + "万(" + maxCount + ")";
        } else {
            maxCountShow = String.valueOf(maxCount);
        }
        return maxCountShow;
    }

    private Map<String/*client*/, Map<String/*time*/, ConsumerClientMetrics>> list2Map(String consumer, Date date,
                                                                                       List<ConsumerClientMetrics> list) {
        Map<String, Map<String, ConsumerClientMetrics>> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        int searchedDate = DateUtil.format(date);
        for (ConsumerClientMetrics metrics : list) {
            Map<String, ConsumerClientMetrics> timeMap = map.get(metrics.getClient());
            if (timeMap == null) {
                timeMap = new HashMap<>();
                map.put(metrics.getClient(), timeMap);
            }
            int statTime = metrics.getStatTime();
            long t = statTime * 60000L;
            Date statDate = new Date(t);
            // 只统计搜索日期的数据
            if (searchedDate != DateUtil.format(statDate)) {
                continue;
            }
            String key = DateUtil.getFormat(DateUtil.HHMM).format(statDate);
            timeMap.put(key, metrics);
        }
        // 查询当天的数据，不必补充23:59的数据
        if (searchedDate == DateUtil.format(new Date())) {
            return map;
        }
        boolean lastDataExist = true;
        for (Map<String, ConsumerClientMetrics> timeMap : map.values()) {
            if (!timeMap.containsKey("2359")) {
                lastDataExist = false;
                break;
            }
        }
        if (lastDataExist) {
            return map;
        }
        // 补充23:59的数据
        int lastStatTime = 0;
        try {
            Date dt = DateUtil.getFormat(DateUtil.YMDHM).parse(searchedDate+"2359");
            lastStatTime = (int) (dt.getTime() / 60000);
        } catch (ParseException e) {
            logger.warn("parse date error", e);
            return map;
        }
        Result<List<ConsumerClientMetrics>> listResult = consumerClientMetricsService.query(consumer, lastStatTime);
        if (listResult.isEmpty()) {
            return map;
        }
        listResult.getResult().stream().forEach(totalStat -> {
            Map<String, ConsumerClientMetrics> timeMap = map.get(totalStat.getClient());
            if(timeMap != null && !timeMap.containsKey("2359")) {
                timeMap.put("2359", totalStat);
            }
        });
        return map;
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

}
