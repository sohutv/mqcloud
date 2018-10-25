/**
 * 获取url查询参数
 * @param name
 * @returns
 */
function getQueryString(name){
    var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if(r!=null)return  unescape(r[2]); return null;
}

function modalHide(timeout, fun){
	if(timeout && timeout > 0) {
		if(fun){
			setTimeout("modalHide(0,"+fun+")",timeout);
		} else {
			setTimeout("modalHide()",timeout);
		}
	} else {
		$('.modal').modal('hide');
		if(fun){
			fun();
		}
	}
}

function reload(timeout){
	if(timeout && timeout > 0) {
		setTimeout("reload()", timeout);
	} else {
		window.location.reload();
	}
}

function disable(id){
	$("#"+id).attr("data-loading-text", "提交中...");
//	$("#"+id).attr("disabled", "disabled");
	$("#"+id).button('loading');
}

function enable(id){
//	$("#"+id).removeAttr("disabled");
	$("#"+id).button('reset');
}

function formatDate(longDate) {
  if(!longDate){
	return "";
  }
  var d=new Date(parseInt(longDate));
  var m=parseInt(d.getMonth(),10)+1;
  var da=parseInt(d.getDate(),10);
  var hour = parseInt(d.getHours(),10);
  var min = parseInt(d.getMinutes(),10);
  var sec = parseInt(d.getSeconds(),10);
  if(m<10){m="0"+m;}
  if(da<10){da="0"+da;}
  if(hour < 10){hour = "0" + hour;}
  if(min < 10){min = "0" + min;}
  if(sec < 10){sec = "0" + sec;}
  return d.getFullYear()+"-"+m+"-"+da +" " + hour + ":" + min+":"+sec;
}

var spinner = null;
// loading
function loading(){
	$("#cover").show();
	spinner = new Spinner().spin(document.getElementById('cover'));
}
function stopLoading(){
	$("#cover").hide();
	spinner.stop();
}
// post
function post(url, data, callback, dataType){
	loading();
	if(dataType){
		$.post(
			url, 
			data,
	       	function(data){
				stopLoading();
				callback(data);
		    },
		    dataType
	    );
	} else {
		$.post(
			url, 
			data,
	       	function(data){
				stopLoading();
				callback(data);
		    }
	    );
	}
}


// 模仿grafana
function grafanaClick(chart){
	// 点击图例，只显示它，隐藏其它的
	chart.plotOptions.series.events = {};
	chart.plotOptions.series.events.legendItemClick = function() {
		var otherVisible = false;
		for(var i in this.chart.series){
			if(i != this.index){
				if(this.chart.series[i].visible) {
					otherVisible = true;
					break;
				}
			}
		}
		// 其它可见
		if(otherVisible){
			// 隐藏其它
			for(var i in this.chart.series){
				if(i != this.index){
					this.chart.series[i].hide();
				}
			}
			if(!this.visible){
				this.show();
			}
		} else {
			// 其它不可见，肯定当前可见：显示其它
			for(var i in this.chart.series){
				if(i != this.index){
					this.chart.series[i].show();
				}
			}
		}
		return false;
	}
}