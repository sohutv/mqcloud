package com.sohu.tv.mq.cloud.web.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.web.view.chart.LineChartData;

/**
 * 曲线图服务
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月29日
 */
@Service
public class LineChartService implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger logger = LoggerFactory.getLogger(LineChartService.class);

    private Map<String, LineChartData> lineChartMap = new HashMap<String, LineChartData>();

    /**
     * auto detect implimention of LineChartData
     * 容器初始化完成后发布ContextRefreshedEvent事件
     */
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, LineChartData> map = event.getApplicationContext().getBeansOfType(LineChartData.class);
        for (LineChartData data : map.values()) {
            lineChartMap.put(data.getPath(), data);
            logger.info("found lineChartData " + data.getPath());
        }
    }

    public LineChartData getLineChartData(String name) {
        return lineChartMap.get(name);
    }

    public Map<String, LineChartData> getLineChartDataMap() {
        return lineChartMap;
    }
}
