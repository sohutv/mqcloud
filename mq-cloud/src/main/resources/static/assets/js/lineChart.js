var drawLineChartCallback;
/**
 * 绘制曲线图
 * @param lineName
 * @returns
 */
function lineChart(lineName, callback, chartCallback, loadingEffect){
	// 默认不开启loading效果
	if (loadingEffect == undefined) {
		loadingEffect = false;
	}
	drawSearchArea(lineName, callback, chartCallback, loadingEffect);
}
/**
 * 绘制search区域
 * @param lineName
 * @returns
 */
function drawSearchArea(lineName, callback, chartCallback, loadingEffect){
	$.get(contextPath + '/line/'+lineName, null, function(data) {
		$("#"+lineName+"_search").html(data);
		if(callback){
			callback();
		}
		drawLineChartCallback = chartCallback;
		drawLineChart(lineName, loadingEffect);
	});
}
/**
 * 真正的绘制曲线
 * @param lineName
 * @returns
 */
function drawLineChart(lineName, loadingEffect){
	// 回调
	if(drawLineChartCallback){
		drawLineChartCallback();
	}
    post(contextPath + '/line/'+lineName+'/data', $("#"+lineName+"_searchForm").serialize(), function(data) {
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
                       return formatSize(this.value);
	                };
				} else {
					yAxis.labels.formatter = function() {
						   if(this.value >= 100000000){
							   return formatNum(this.value / 100000000) +'亿';
						   }
						   if(this.value >= 10000){
							   return formatNum(this.value / 10000) +'万';
						   }
						   if(this.value >= 1000){
							   return formatNum(this.value / 1000) +'千';
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
					div = "<div class='card'><div class='card-body'>";
					if (chart.hasOwnProperty('overview')) {
						div += "<div class='d-flex justify-content-center'><div class='card'><div class='card-body table-responsive small p-0 text-nowrap text-muted'>" + chart.overview + "</div></div></div>";
					}
					div += "<div class='sohu_hc' style='min-width:300px;height:"+chart.chart.height+"px' id='"+chart.chart.renderTo+"'/></div></div>";
				}
				divComponent.append(div);
			}
			new Highcharts.Chart(chart);
		}
	}, 'json', loadingEffect);
}