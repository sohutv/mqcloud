package com.sohu.tv.mq.cloud.web.view.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.ConsumerTraffic;
import com.sohu.tv.mq.cloud.bo.TopicTopology;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.bo.Traffic;
import com.sohu.tv.mq.cloud.service.ConsumerTrafficService;
import com.sohu.tv.mq.cloud.service.TopicTrafficService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.WebUtil;
import com.sohu.tv.mq.cloud.web.view.SearchHeader;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.DateSearchField;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.HiddenSearchField;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.SearchField;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart.XAxis;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart.YAxis;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart.YAxisGroup;
import com.sohu.tv.mq.cloud.web.view.chart.LineChartData;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * topic消费者流量数据
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月29日
 */
@Component
public class ConsumeTrafficLineChartData implements LineChartData {

    // 搜索区域
    private SearchHeader searchHeader;

    public static final String DATE_FIELD = "date";
    public static final String TID_FIELD = "tid";
    public static final String DATE_FIELD_TITLE = "日期";

    @Autowired
    private TopicTrafficService topicTrafficService;

    @Autowired
    private ConsumerTrafficService consumerTrafficService;
    
    @Autowired
    private UserService userService;

    public ConsumeTrafficLineChartData() {
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
        hiddenSearchField.setKey(TID_FIELD);
        searchFieldList.add(hiddenSearchField);

        searchHeader.setSearchFieldList(searchFieldList);
    }

    @Override
    public String getPath() {
        return "consume";
    }

    @Override
    public String getPageTitle() {
        return "topic消费流量";
    }

    @Override
    public List<LineChart> getLineChartData(Map<String, Object> searchMap) {
        List<LineChart> lineChartList = new ArrayList<LineChart>();
        
        // 解析参数
        Date date = getDate(searchMap, DATE_FIELD);
        String dateStr = DateUtil.formatYMD(date);
        Long tid = getLongValue(searchMap, TID_FIELD);
        
        // 获取request
        HttpServletRequest request = (HttpServletRequest) searchMap.get(REQUEST);
        // 获取用户信息
        UserInfo userInfo = (UserInfo) WebUtil.getAttribute(request, UserInfo.USER_INFO);
        // 查询topic的消费者
        Result<TopicTopology> topicTopologyResult = userService.queryTopicTopology(userInfo.getUser(), tid);
        if(topicTopologyResult.isNotOK()) {
            return lineChartList;
        }
        TopicTopology topicTopology = topicTopologyResult.getResult();
        
        if (tid == null || tid <= 0) {
            return lineChartList;
        }
        //获取topic流量
        Result<List<TopicTraffic>> result = topicTrafficService.query(tid, dateStr);
        if (!result.isOK()) {
            return lineChartList;
        }

        // 将list转为map方便数据查找
        Map<String, Traffic> trafficMap = list2Map(result.getResult());
        
        // 生成消费者数据
        LineChart lineChart2 = getConsumerLineChart(searchMap, dateStr, topicTopology, trafficMap);
        if (lineChart2 != null) {
            lineChartList.add(lineChart2);
        }
        return lineChartList;
    }

    private long setCountData(Traffic traffic, List<Number> countList) {
        if (traffic == null) {
            countList.add(0);
            return 0;
        } else {
            countList.add(traffic.getCount());
            return traffic.getCount();
        }
    }

    private Map<String, Traffic> list2Map(List<? extends Traffic> list) {
        Map<String, Traffic> map = new TreeMap<String, Traffic>();
        if (list == null) {
            return map;
        }
        for (Traffic traffic : list) {
            map.put(traffic.getCreateTime(), traffic);
        }
        return map;
    }

    private Map<Long, List<ConsumerTraffic>> list2ConsumerMap(List<ConsumerTraffic> list) {
        Map<Long, List<ConsumerTraffic>> map = new HashMap<Long, List<ConsumerTraffic>>();
        if (list == null) {
            return map;
        }
        for (ConsumerTraffic traffic : list) {
            List<ConsumerTraffic> mapList = map.get(traffic.getConsumerId());
            if (mapList == null) {
                mapList = new ArrayList<ConsumerTraffic>();
                map.put(traffic.getConsumerId(), mapList);
            }
            mapList.add(traffic);
        }
        return map;
    }
    
    /**
     * 生成消费者图表
     * 
     * @param searchMap
     * @param date
     * @param topic
     * @return
     */
    private LineChart getConsumerLineChart(Map<String, Object> searchMap, String date, TopicTopology topicTopology, 
            Map<String, Traffic> producerTrafficMap) {
        List<Consumer> consumerList = topicTopology.getConsumerList();
        List<Long> idList = new ArrayList<Long>();
        for(Consumer consumer : consumerList) {
            idList.add(consumer.getId());
        }
        Result<List<ConsumerTraffic>> listResult = consumerTrafficService.query(idList, date);
        if (!listResult.isOK()) {
            //return null;
        }
        // 构造曲线图对象
        LineChart lineChart = new LineChart();
        lineChart.setChartId("consumer");
        lineChart.setTitle(topicTopology.getTopic().getName() + "消费情况");
        lineChart.setOneline(true);
        lineChart.setTickInterval(6);
        
        List<String> xList = new ArrayList<String>();
        for(String time : producerTrafficMap.keySet()) {
            xList.add(time.substring(0, 2) + ":" + time.substring(2));
        }
        
        XAxis xAxis = new XAxis();
        xAxis.setxList(xList);
        lineChart.setxAxis(xAxis);

        // 设置y轴列表
        List<YAxis> countYAxisList = new ArrayList<YAxis>();
        // 生成y轴数据组
        YAxisGroup countYAxisGroup = new YAxisGroup();
        countYAxisGroup.setGroupName("消息量");
        countYAxisGroup.setyAxisList(countYAxisList);
        // 设置y轴
        List<YAxisGroup> yAxisGroupList = new ArrayList<YAxisGroup>();
        yAxisGroupList.add(countYAxisGroup);
        lineChart.setyAxisGroupList(yAxisGroupList);

        Map<Long, List<ConsumerTraffic>> map = list2ConsumerMap(listResult.getResult());
        for (Consumer consumer : consumerList) {
            List<ConsumerTraffic> list = map.get(consumer.getId());
            // 将list转为map方便数据查找
            Map<String, Traffic> trafficMap = list2Map(list);
            // 填充y轴数据
            List<Number> countList = new ArrayList<Number>();
            for (String time : producerTrafficMap.keySet()) {
                setCountData(trafficMap.get(time), countList);
            }
            YAxis countYAxis = new YAxis();
            countYAxis.setName(consumer.getName());
            countYAxis.setData(countList);
            countYAxisList.add(countYAxis);
        }
        
        // 设置生产者数据
        List<Number> producerCountList = new ArrayList<Number>();
        for (String time : producerTrafficMap.keySet()) {
            setCountData(producerTrafficMap.get(time), producerCountList);
        }
        YAxis producerCountYAxis = new YAxis();
        producerCountYAxis.setName("生产者");
        producerCountYAxis.setData(producerCountList);
        countYAxisList.add(producerCountYAxis);
        return lineChart;
    }
    
    /**
     * 获取长整型数据
     * 
     * @param searchMap
     * @param key
     * @return
     */
    protected Long getLongValue(Map<String, Object> searchMap, String key) {
        if (searchMap == null) {
            return null;
        }
        Object obj = searchMap.get(key);
        if (obj == null) {
            return null;
        }
        return NumberUtils.toLong(obj.toString());
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
