package com.sohu.tv.mq.cloud.web.controller;

import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.service.LineChartService;
import com.sohu.tv.mq.cloud.web.view.PageView;
import com.sohu.tv.mq.cloud.web.view.SearchHeader;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.SearchField;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.SearchFieldType;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.SelectSearchField;
import com.sohu.tv.mq.cloud.web.view.SearchHeader.SelectSearchField.KV;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart.XAxis;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart.YAxis;
import com.sohu.tv.mq.cloud.web.view.chart.LineChart.YAxisGroup;
import com.sohu.tv.mq.cloud.web.view.chart.LineChartData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 曲线请求处理控制器
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月29日
 */
@Controller
@RequestMapping("/line")
public class LineChartController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private LineChartService lineChartService;

    /**
     * 获取所有曲线页面
     * 
     * @param request
     * @param model
     * @param response
     * @param name
     * @return
     */
    @ResponseBody
    @RequestMapping
    public Result<Map<String, LineChartData>> getLineChartPage() {
        Map<String, LineChartData> map = lineChartService.getLineChartDataMap();
        if (map == null || map.size() == 0) {
            logger.error("cannot get any LineChart");
            return Result.getResult(Status.NO_RESULT);
        }
        return Result.getResult(map);
    }

    /**
     * 获取search头信息
     * 
     * @param request
     * @param model
     * @param response
     * @param name
     * @return
     */
    @RequestMapping("/{name}")
    public String getLineChartInfo(HttpServletRequest request, @PathVariable("name") String name,
            Map<String, Object> map) {
        String view = "inc/lineChart";
        LineChartData data = lineChartService.getLineChartData(name);
        if (data == null) {
            logger.error("cannot get lineChart:" + name);
            return view;
        }
        SearchHeader searchHeader = data.getSearchHeader();
        if (searchHeader == null) {
            return view;
        }
        List<SearchField> list = searchHeader.getSearchFieldList();
        if (list == null || list.size() == 0) {
            return view;
        }
        for (SearchField searchField : list) {
            String value = request.getParameter(searchField.getKey());
            if (StringUtils.isBlank(value)) {
                continue;
            }
            boolean set = false;
            if (SearchFieldType.SELECT.ordinal() == searchField.getType()) {
                SelectSearchField ss = (SelectSearchField) searchField;
                List<KV> kvList = ss.getKvList();
                for (KV kv : kvList) {
                    if (value.equals(kv.getV())) {
                        searchField.setValue(kv.getK());
                        set = true;
                    }
                }
            }
            if (!set) {
                searchField.setValue(value.trim());
            }
        }
        Result.setResult(map, data);
        return view;
    }

    /**
     * 获取曲线数据
     * 
     * @param request
     * @param model
     * @param response
     * @param name
     * @return
     */
    @ResponseBody
    @RequestMapping("/{name}/data")
    public Result<List<HighChartLine>> getLineChartData(HttpServletRequest request,
            @PathVariable("name") String name) {
        LineChartData data = lineChartService.getLineChartData(name);
        if (data == null) {
            logger.error("cannot get lineChart:" + name);
            return Result.getResult(Status.NO_RESULT);
        }
        Map<String, Object> searchMap = getSearchParam(data, request);
        List<LineChart> chartList = data.getLineChartData(searchMap);
        List<HighChartLine> rstList = new ArrayList<HighChartLine>(chartList.size());
        for (LineChart lineChart : chartList) {
            HighChartLine l = new HighChartLine(lineChart);
            rstList.add(l);
        }
        return Result.getResult(rstList);
    }

    /**
     * get search part param
     * 
     * @param data
     * @param request
     * @return
     */
    public Map<String, Object> getSearchParam(LineChartData data,
            HttpServletRequest request) {
        Map<String, Object> searchMap = new HashMap<String, Object>();
        SearchHeader sh = data.getSearchHeader();
        if (sh == null) {
            return searchMap;
        }
        List<SearchField> list = sh.getSearchFieldList();
        for (SearchField sf : list) {
            String v = request.getParameter(sf.getKey());
            if (sf.getType() == SearchFieldType.DATE.ordinal()) {
                if (v == null) {
                    v = DateUtil.formatYMD(new Date());
                } else {
                    v = v.replaceAll("-", "");
                }
            }
            if (StringUtils.isNotBlank(v)) {
                searchMap.put(sf.getKey(), v.trim());
            }
        }
        // 将request对象传递出去
        searchMap.put(PageView.REQUEST, request);
        return searchMap;
    }

    public static class HighChartLine {
        private Map<?, ?> dataMap;
        private Map<String, Object> chart = new HashMap<String, Object>();
        private Map<String, String> title = new HashMap<String, String>();
        private Map<String, Object> subtitle = new HashMap<String, Object>();
        private Map<String, Object> tooltip = new HashMap<String, Object>();
        private Map<String, Object> xAxis = new HashMap<String, Object>();
        private List<Map<String, ?>> yAxis = new ArrayList<Map<String, ?>>();
        private Map<String, Object> plotOptions = new HashMap<String, Object>();
        private List<YAxis> series = new ArrayList<YAxis>();
        private String url;
        private boolean oneline;
        private String urlTitle;
        private String overview;

        @SuppressWarnings({"rawtypes", "unchecked"})
        public HighChartLine(LineChart lineChart) {
            overview = lineChart.getOverview();
            oneline = lineChart.isOneline();
            chart.put("renderTo", lineChart.getChartId());
            chart.put("zoomType", "xy");
            chart.put("type", lineChart.getType());
            chart.put("height", lineChart.getHeight());

            title.put("text", lineChart.getTitle());

            if (lineChart.getSubTitle() != null) {
                subtitle.put("text", lineChart.getSubTitle());
            }
            
            if(lineChart.getDataMap() != null) {
                dataMap = lineChart.getDataMap();
            }
            
            XAxis x = lineChart.getxAxis();
            xAxis.put("categories", x.getxList());
            if (lineChart.getTickInterval() != 0) {
                xAxis.put("tickInterval", lineChart.getTickInterval());
            }

            for (YAxisGroup yAxisGroup : lineChart.getyAxisGroupList()) {
                Map yAxisMap = new HashMap();
                Map<String, String> tm = new HashMap<String, String>();
                tm.put("text", yAxisGroup.getGroupName());
                yAxisMap.put("title", tm);
                yAxisMap.put("opposite", yAxisGroup.isOpposite());
                yAxisMap.put("traffic", yAxisGroup.isTraffic());
                yAxis.add(yAxisMap);
            }

            if (lineChart.getBorderWidth() != 0) {
                chart.put("borderWidth", lineChart.getBorderWidth());
            }

            if (lineChart.getTip() != null) {
                tooltip.put("shared", lineChart.getTip().isShared());
                tooltip.put("useHTML", lineChart.getTip().isUseHTML());
                if (lineChart.getTip().getHeaderFormat() != null) {
                    tooltip.put("headerFormat", lineChart.getTip().getHeaderFormat());
                }
                if (lineChart.getTip().getPointFormat() != null) {
                    tooltip.put("pointFormat", lineChart.getTip().getPointFormat());
                }
                if (lineChart.getTip().getFooterFormat() != null) {
                    tooltip.put("footerFormat", lineChart.getTip().getFooterFormat());
                }
            } else {
                tooltip.put("shared", true);
            }

            for (int i = 0; i < lineChart.getyAxisGroupList().size(); ++i) {
                YAxisGroup yAxisGroup = lineChart.getyAxisGroupList().get(i);
                for (YAxis yaxis : yAxisGroup.getyAxisList()) {
                    yaxis.setyAxis(i);
                    series.add(yaxis);
                }
            }

            String url = lineChart.getUrl();
            if (url != null) {
                this.url = url;
            }
            if (lineChart.getUrlTitle() != null) {
                this.urlTitle = lineChart.getUrlTitle();
            }
            // 禁止显示点
            Map<String, Object> map = new HashMap<String, Object>();
            Map<String, Object> enabledMap = new HashMap<String, Object>();
            map.put("marker", enabledMap);
            enabledMap.put("enabled", false);
            map.put("turboThreshold", 0);
            plotOptions.put("series", map);
        }

        public Map<String, Object> getChart() {
            return chart;
        }

        public Map<String, String> getTitle() {
            return title;
        }

        public Map<String, Object> getSubtitle() {
            return subtitle;
        }

        public Map<String, Object> getPlotOptions() {
            return plotOptions;
        }

        public Map<String, Object> getTooltip() {
            return tooltip;
        }

        public Map<String, Object> getxAxis() {
            return xAxis;
        }

        public Map<?, ?> getDataMap() {
            return dataMap;
        }

        public List<Map<String, ?>> getyAxis() {
            return yAxis;
        }

        public List<YAxis> getSeries() {
            return series;
        }

        public String getUrlTitle() {
            return urlTitle;
        }

        public boolean isOneline() {
            return oneline;
        }

        public String getUrl() {
            return url;
        }

        public String getOverview() {
            return overview;
        }
    }
}
