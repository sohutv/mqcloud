var drawLineChartCallback;
/**
 * 绘制曲线图
 * @param lineName
 * @returns
 */
function lineChart(lineName, callback, chartCallback){
	drawSearchArea(lineName, callback, chartCallback);
}
/**
 * 绘制search区域
 * @param lineName
 * @returns
 */
function drawSearchArea(lineName, callback, chartCallback){
	$.get(contextPath + '/line/'+lineName, null, function(data) {
		$("#"+lineName+"_search").html(data);
		if(callback){
			callback();
		}
		drawLineChartCallback = chartCallback;
		drawLineChart(lineName);
	});
}
/**
 * 真正的绘制曲线
 * @param lineName
 * @returns
 */
function drawLineChart(lineName){
	// 回调
	if(drawLineChartCallback){
		drawLineChartCallback();
	}
    $.get(contextPath + '/line/'+lineName+'/data', $("#"+lineName+"_searchForm").serialize(), function(data) {
    	if(data.status != 200){
			alert(divComponent + " chart data err!");
			return;
		}
    	var divComponent = $("#" + lineName + "_lineChart");
    	if(data.result.length <= 0){
    		divComponent.html("<center>暂无数据</center>");
    		return;
    	}
    	divComponent.empty();
		for(var i = 0; i < data.result.length; ++i){
			var chart = data.result[i];
			if ("pstats" == lineName){
				delete chart.subtitle["useHTML"];
			}
			if(chart.url){
				url = chart.url;
				chart.plotOptions = {};
				chart.plotOptions.series = {};
				chart.plotOptions.series.point = {};
				chart.plotOptions.series.point.events = {};
				chart.plotOptions.series.point.events.click = function(){
					location.href=url+"?x="+this.x+"&y="+this.y+"&name="+this.series.name;
				};
			}
			for(var yAxisIdx in chart.yAxis){
				var yAxis = chart.yAxis[yAxisIdx];
				yAxis.min = 0;
				yAxis.labels = {};
				if(yAxis.traffic){
	                yAxis.labels.formatter = function() {
						   if(this.value >= 1073741824){
							   return format(this.value / 1073741824) +'g';
						   }
						   if(this.value >= 1048576){
							   return format(this.value / 1048576) +'m';
						   }
						   if(this.value >= 1024){
							   return format(this.value / 1024) +'k';
						   }
	                       return this.value;
	                };
				} else {
					yAxis.labels.formatter = function() {
						   if(this.value >= 100000000){
							   return format(this.value / 100000000) +'亿';
						   }
						   if(this.value >= 10000){
							   return format(this.value / 10000) +'万';
						   }
						   if(this.value >= 1000){
							   return format(this.value / 1000) +'千';
						   }
	                       return this.value;
	                };
				}
			}
			
			// 回调
			if(drawLineChartCallback){
				drawLineChartCallback(chart);
			}
			if ($("#"+chart.chart.renderTo).length == 0) {
				var div = "<div id='"+chart.chart.renderTo+"' style='float: left;margin: 10px;'/>";
				if(chart.oneline) {
					div = "<div style='clear:both'/><div class='sohu_hc' style='min-width:400px;height:"+chart.chart.height+"px' id='"+chart.chart.renderTo+"'/><div style='clear:both'/>";
				}
				divComponent.append(div);
			}
			new Highcharts.Chart(chart);
		}
	}, 'json');
}

/**
 * 保留一位小数，舍弃0
 * @param num
 * @returns
 */
function format(num){
	return parseFloat(num.toFixed(1));
}
