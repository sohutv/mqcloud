package com.sohu.tv.mq.cloud.web.view.chart;

import java.util.List;
import java.util.Map;

import com.sohu.tv.mq.cloud.web.view.PageView;
import com.sohu.tv.mq.cloud.web.view.SearchHeader;

/**
 * 曲线图数据接口
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月29日
 */
public interface LineChartData extends PageView {
    /**
     * 获取查询条件
     */
    public SearchHeader getSearchHeader();

    /**
     * 获取曲线图数据，如果只有一个曲线图，只返回list为1的结果即可
     * 
     * @param searchMap
     * @return
     */
    public List<LineChart> getLineChartData(Map<String, Object> searchMap);
}
