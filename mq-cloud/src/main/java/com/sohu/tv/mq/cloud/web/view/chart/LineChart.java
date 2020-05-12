package com.sohu.tv.mq.cloud.web.view.chart;

import java.util.List;
import java.util.Map;

/**
 * 曲线图对象
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月29日
 */
public class LineChart {
    // 曲线类型
    private String type = "spline";
    // 曲线图id，同一个页面的多张图不可相同
    private String chartId;
    // 曲线图的名称，用于显示
    private String title;
    // 曲线图的副名称
    private String subTitle;
    // 高度
    private int height = 400;
    // 曲线提示对象
    private Tip tip;
    // 曲线图的x轴对象
    private XAxis xAxis;
    // 曲线图的一组y轴数据，如果只有一条曲线，该组中的list为1
    private List<YAxisGroup> yAxisGroupList;
    // 点击曲线图的点跳到的url，点击后会给该url附加上x=&y=&name=的数据
    private String url;
    // 鼠标移到该点时，提示该点跳转的名字
    private String urlTitle;
    // 曲线边框宽度，默认带边框
    private int borderWidth = 1;
    // 是否单独占一行显示
    private boolean oneline;
    // 设置x轴步长
    private int tickInterval;
    
    private Map<?, ?> dataMap;

    public String getChartId() {
        return chartId;
    }

    public void setChartId(String chartId) {
        this.chartId = chartId;
    }

    public String getUrlTitle() {
        return urlTitle;
    }

    public void setUrlTitle(String urlTitle) {
        this.urlTitle = urlTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTickInterval() {
        return tickInterval;
    }

    public void setTickInterval(int tickInterval) {
        this.tickInterval = tickInterval;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public Tip getTip() {
        return tip;
    }

    public void setTip(Tip tip) {
        this.tip = tip;
    }

    public XAxis getxAxis() {
        return xAxis;
    }

    public void setxAxis(XAxis xAxis) {
        this.xAxis = xAxis;
    }

    public List<YAxisGroup> getyAxisGroupList() {
        return yAxisGroupList;
    }

    public void setyAxisGroupList(List<YAxisGroup> yAxisGroupList) {
        this.yAxisGroupList = yAxisGroupList;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOneline() {
        return oneline;
    }

    public void setOneline(boolean oneline) {
        this.oneline = oneline;
    }

    public Map<?, ?> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<?, ?> dataMap) {
        this.dataMap = dataMap;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * X轴对象
     */
    public static class XAxis {
        // x轴的刻度列表
        private List<String> xList;
        
        public List<String> getxList() {
            return xList;
        }

        public void setxList(List<String> xList) {
            this.xList = xList;
        }
    }

    /**
     * 一组Y轴数据
     */
    public static class YAxisGroup {
        // 整个y轴组的名字
        private String groupName;
        // 各个y轴数据
        private List<YAxis> yAxisList;

        // 可选的（如果为true y轴在右边显示）
        private boolean opposite;

        // 是否启用流量单位格式化
        private boolean traffic;

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<YAxis> getyAxisList() {
            return yAxisList;
        }

        public void setyAxisList(List<YAxis> yAxisList) {
            this.yAxisList = yAxisList;
        }

        public boolean isOpposite() {
            return opposite;
        }

        public void setOpposite(boolean opposite) {
            this.opposite = opposite;
        }

        public boolean isTraffic() {
            return traffic;
        }

        public void setTraffic(boolean traffic) {
            this.traffic = traffic;
        }

    }

    /**
     * Y轴对象，注意，x轴的xList与y轴yList顺序需要一一对应
     */
    public static class YAxis {
        // y轴的的类型名
        private String name;
        // y轴数据
        private List<Object> data;
        // 颜色
        private String color;
        // 对应的y轴
        private int yAxis;

        // 默认是否可见
        private boolean visible = true;
        
        private Tip tooltip;

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Object> getData() {
            return data;
        }

        public int getyAxis() {
            return yAxis;
        }

        public void setyAxis(int yAxis) {
            this.yAxis = yAxis;
        }

        @SuppressWarnings("unchecked")
        public void setData(List<?> data) {
            this.data = (List<Object>) data;
        }

        public Tip getTooltip() {
            return tooltip;
        }

        public void setTooltip(Tip tooltip) {
            this.tooltip = tooltip;
        }

    }
    

    /**
     * 曲线数据提示对象
     */
    public static class Tip {
        // 是否多条曲线的数据一起提示
        private boolean shared = true;
        // 默认为xAxis+YAxis.name+YAxis数据
        private String headerFormat;
        private String pointFormat;
        private String footerFormat;
        // message是否使用html格式
        private boolean useHTML;
        private String valueSuffix;

        public boolean isShared() {
            return shared;
        }

        public void setShared(boolean shared) {
            this.shared = shared;
        }

        public String getPointFormat() {
            return pointFormat;
        }

        public String getHeaderFormat() {
            return headerFormat;
        }

        public void setHeaderFormat(String headerFormat) {
            this.headerFormat = headerFormat;
        }

        public String getFooterFormat() {
            return footerFormat;
        }

        public void setFooterFormat(String footerFormat) {
            this.footerFormat = footerFormat;
        }

        public void setPointFormat(String pointFormat) {
            this.pointFormat = pointFormat;
        }

        public boolean isUseHTML() {
            return useHTML;
        }

        public void setUseHTML(boolean useHTML) {
            this.useHTML = useHTML;
        }

        public String getValueSuffix() {
            return valueSuffix;
        }

        public void setValueSuffix(String valueSuffix) {
            this.valueSuffix = valueSuffix;
        }
    }
}
