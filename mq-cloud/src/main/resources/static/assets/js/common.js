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

function modalHideId(id, timeout, fun){
	if(timeout && timeout > 0) {
		if(fun){
			setTimeout("modalHideId('"+id+"',0,"+fun+")",timeout);
		} else {
			setTimeout("modalHideId('"+id+"')",timeout);
		}
	} else {
		$('#' + id).modal('hide');
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

function toMyAuditPage() {
	toPage(contextPath+"/audit/list", 2000);
}

function toPage(url, timeout){
	if(timeout && timeout > 0) {
		setTimeout("toPage('" + url + "')", timeout);
	} else {
		window.location.href = url;
	}
}

function disable(id){
	$("#"+id).attr("data-text", $("#"+id).html()).prop("disabled", true).html("<i class='fas fa-spinner fa-spin'></i>");
}

function enable(id){
	$("#"+id).prop("disabled", false).html($("#"+id).attr("data-text"));
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
	$("#overlay").show();
	spinner = new Spinner().spin(document.getElementById('overlay'));
}

function stopLoading() {
	if (spinner) {
		$("#overlay").hide();
		spinner.stop();
	}
}
// post
let tabLoadingStartTime = 0;
function post(url, data, callback, dataType, loadingEffect){
	// 默认开启loading特效
	if (loadingEffect == undefined) {
		loadingEffect = true;
	}
	if (loadingEffect) {
		tabLoadingStartTime = new Date().getTime();
	}
	if(!dataType){
		dataType = "html";
	}
	$.ajax({
	    type: 'POST',
	    url: url,
	    data: data,
	    success: function(data) {
			let cost = new Date().getTime() - tabLoadingStartTime;
			tabLoadingStartTime = 0;
			if (cost >= 200 && cost < 500) {
				setTimeout(() => {
					stopLoading();
					callback(data);
				}, 500)
			} else {
				stopLoading();
				callback(data);
			}
	    },
		error : function(XMLHttpRequest, err, e){
			stopLoading();
			toastr.error("网络异常("+XMLHttpRequest.status+"):"+err); 
		},
		dataType: dataType
	});
	// 超过200ms，则开启loading至少500ms
	setTimeout(() => {
		if (tabLoadingStartTime != 0) {
			loading();
		}
	}, 200);
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

/**
 * 格式化时间戳
 * @param longDate
 * @returns
 */
function formatDateYMDHM(longDate) {
	if (!longDate) {
		return "";
	}
	var d = new Date(parseInt(longDate));
	var m = parseInt(d.getMonth(), 10) + 1;
	var da = parseInt(d.getDate(), 10);
	var hour = parseInt(d.getHours(), 10);
	var min = parseInt(d.getMinutes(), 10);
	var sec = parseInt(d.getSeconds(), 10);
	if (m < 10) {
		m = "0" + m;
	}
	if (da < 10) {
		da = "0" + da;
	}
	if (hour < 10) {
		hour = "0" + hour;
	}
	if (min < 10) {
		min = "0" + min;
	}
	if (sec < 10) {
		sec = "0" + sec;
	}
	return d.getFullYear() + "-" + m + "-" + da + " " + hour + ":" + min + ":" + sec;
}


/**
 * 解析格式化字符串到时间戳
 * @param formatDate yyyy-MM-dd HH:mm:ss
 * @returns
 */
function parseDate(formatDate) {
	if (!formatDate) {
		return "";
	}
	var date = Date.parse(formatDate.replace(/-/g,'/'));
	return date;
}

/**
 * 格式化大小
 * @param value
 * @returns
 */
function formatSize(value) {
	if (value >= 1073741824) {
		return formatNum(value / 1073741824) + 'GB';
	}
	if (value >= 1048576) {
		return formatNum(value / 1048576) + 'MB';
	}
	if (value >= 1024) {
		return formatNum(value / 1024) + 'KB';
	}
	if(value == 0) {
		return "0B";
	}
	return value + "B";
}

/**
 * 保留一位小数，舍弃0
 * 
 * @param num
 * @returns
 */
function formatNum(num){
	return parseFloat(num.toFixed(1));
}

function isMobile() {
	return window.screen.width <= 768 && window.screen.height <= 1024;
}

function logout(){
	$.get(contextPath + '/user/logout',function(data){
		if(data.status == 200){
			toastr.success("退出登录成功，欢迎再次使用MQCloud！");
			reload(2000);
		} else {
			toastr.error("退出登录失败，" + data.message);
		}
	},'json');
}

function readAll() {
	$.post(contextPath + '/user/message/read/all', function (data) {
		if (data.status == 200) {
			$("#msgCount").html("");
		}
	}, 'json');
}

function storageSet(key, value, expires) {
	let storages = {}
	storages[key] = {
		value: value,
		expires: (expires * 1000) + new Date().getTime()
	}
	localStorage.setItem(key, JSON.stringify(storages))
}

function storageGet(key) {
	const storages = JSON.parse(localStorage.getItem(key))
	if (!storages) {
		return null
	}
	if (new Date().getTime() > storages[key].expires) {
		storageRemove(key)
		return null
	}
	return storages[key].value
}

function storageRemove(key) {
	localStorage.removeItem(key)
}

function getUnreadMsgCount() {
	if (window.location.pathname.indexOf(contextPath + "/admin/") == 0) {
		return;
	}
	// 获取用户未读消息
	if (window.location.pathname != contextPath + "/login" && window.location.pathname != contextPath + "/register"
		&& window.location.pathname != contextPath + "/user/guide") {
		$.get(contextPath + '/user/message/count', function(data){
			if (data.status == 200) {
				if (data.result > 0) {
					$("#msgCount").html(data.result);
					if (window.location.pathname == contextPath + "/user/message/list") {
						readAll();
					}
				}
			} else {
				toastr.error("获取消息失败！" + data.message);
			}
		}, 'json');
	}
}

$(function () {
	$("[data-toggle='tooltip']").tooltip({boundary: 'window'});
	$("[data-toggle='modal']").tooltip({boundary: 'window'});
	$(".back-to-top").click(function(){
		$(".modal.show").animate({scrollTop:0},300);
		$("body , html").animate({scrollTop:0},300);
	});
	getUnreadMsgCount();
	$(document).on('mouseup touchend', function (e) {
		var container = $('.bootstrap-datetimepicker-widget');
		if (!container.is(e.target) && container.has(e.target).length === 0) {
			container.parent().datetimepicker('hide');
		}
	});
});