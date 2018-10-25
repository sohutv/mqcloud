;(function () {
    var option = {
        title: "消息通知", //标题
        width: 250,
        msgData: [], //消息数据
        noticeData: [], //提醒数据
        msgUnReadData: 0, //消息未读数
        noticeUnReadData: 0, //提醒未读数
        elem: "body",
        allRead: null, //全部已读点击事件
        msgClick: null, //消息点击回调事件
        noticeClick: null, //提醒点击回调事件
        msgShow: 5, //消息展示条数
        noticeShow: 5, //提醒展示条数
        height: 350,
        getNodeHtml: null, //消息提醒自定义显示html
        allMsg: null //所有的消息，包含历史消息
    };

    var api = {
        nodeList: [],
        tp: null,
        config: function(ops) {
            $.extend(true, option, ops);
            this.nodeList = [];
            return this;
        },
        /**
         * 初始化
         * @param ops
         * @returns {api}
         */
        init: function(ops) {

            ops !== undefined && this.config(ops);

            if (option.elem === undefined || typeof option.elem !== "string") {
                throw "option.elem is undefined";
            }

            var clazz = $(option.elem).attr("class");
            $(option.elem).attr("class", clazz?clazz:"" + " message-bell");
            
            this.bellDraw();
            this.listener();

            return this;
        },
        /**
         * 消息按钮渲染
         * @param timestamp
         * @returns {*}
         */
        bellDraw: function () {
            var bellHtml = "<span data-type='1' class='message-bell-btn' "+ (option.msgUnReadData > 0?("title='"+ option.msgUnReadData + "条新消息'"):"") +">" +
                "<i class='fa fa-bell-o '></i>"+ (option.msgUnReadData > 0?"<span class='bell-dot'></span>":"") +"</span>";
            $(option.elem).html(bellHtml);
        },
        /**
         * 事件监听
         * @param timestamp
         * @returns {*}
         */
        listener: function() {
            var that = this;
            
            $(option.elem).click(function(e) {
            	var bt = $(".message-bell-btn");
            	if ($(".message-frame").length > 0) {
                    $(".message-frame").remove();
                }
                var type = bt.attr("data-type");
                that.tp = type;
                that.contentListDraw(type);
                that.caculatePosition(bt.get(0));
            });
            
            /**
             * 铃铛按钮点击
             */
            $(option.elem).on("click", ".message-bell-btn", function() {
                if ($(".message-frame").length > 0) {
                    $(".message-frame").remove();
                }
                var type = $(this).attr("data-type");
                that.tp = type;
                that.contentListDraw(type);
                that.caculatePosition(this);
            });

            /**
             * 消息点击
             */
            $(document).on("click", ".message-content-list li", function() {
                if ($(this).find(".badge-dot").length > 0) {
                    var type = that.tp;
                    if (type == 1) {
                        (option.msgClick&&typeof option.msgClick == "function")?option.msgClick(this):"";

                    } else if (type == 2) {
                        (option.noticeClick&&typeof option.noticeClick == "function")?option.noticeClick(this):"";
                    }

                    if ($(this).find(".badge-dot").length > 0) {
                        if (type == 1) {
                            option.msgUnReadData>0?option.msgUnReadData -= 1:"";
                        } else if (type == 2) {
                            option.noticeUnReadData>0?option.noticeUnReadData -= 1:"";
                        }
                        $(this).find(".badge-dot").remove();
                    }
                }

            });

            /**
             * 全部已读
             */
            $(document).on("click", ".message-btn-header", function() {
                var type = that.tp;
                if (option.allRead && typeof option.allRead == "function") {
                    option.allRead(type, that);
                }
            });

            /**
             * 加载更多
             */
            $(document).on("click", ".message-footer", function() {
                var ct = $(".message-content-list li").length;
                var dataType = that.tp;
                var arr = dataType==1?option.msgData:option.noticeData;
                var count = 0;
                for (var i=ct; i<arr.length; i++) {
                    if (arr[i]) {
                        var node = that.getNode(arr[i], dataType);
                        that.nodeList.push(node);
                        count ++;
                    }

                    if (count > option.msgShow) {
                        break;
                    }
                }

                var html = that.getNodelistHtml();

                $(".message-content-list").html(html);

                if (arr.length <= that.nodeList.length) {
                    $(this).hide();
                }
            });

            /**
             * 点击移除
             */
            $(document).click(function(e){
                var src = e.target;

                var arr = $(".message-frame *");

                var flag = true;

                if(src.className && src.className.match("message-bell-btn")
                    || src.parentElement && src.parentElement.className.match("message-bell-btn")){
                    flag = false;
                }else{
                    if (arr && arr.length > 0) {
                        for (var i = 0; i < arr.length; i++) {
                            if (src && src === arr[i]) {
                                flag = false;
                            }
                        }
                    }

                    if (src.className && src.className.match("message-frame")) {
                        flag = false;
                    }
                }

                if (flag) {
                    $(".message-frame").remove();
                    that.nodeList = [];
                    that.tp = null;
                }


            });
        },
        /**
         * 计算时间
         * @param timestamp
         * @returns {*}
         */
        caculateDate: function(timestamp) {
            if (!timestamp || isNaN(timestamp)) {
                throw "time error";
            }
            var createDate = new Date(timestamp);
            var now = new Date();

            var date3 = now - createDate;

            var days = Math.floor(date3/(24*3600*1000));

            if (days && days > 0 && days <= 1) {
                return days + "天前";
            } else if (days && days > 1) {
                return createDate.toLocaleDateString();
            }

            var hours = Math.floor(date3/(3600*1000));

            if (hours && hours > 0) {
                return hours + "小时前";
            }

            var min = Math.floor(date3/(60*1000));
            if (min && min > 0) {
                return min + "分钟前";
            }

            return "刚刚";
        },
        /**
         * 消息列表渲染
         * @param type
         */
        contentListDraw: function(type) {
            var fr = "";
            if ($(".message-frame").length == 0) {
                fr += "<div class='message-frame animated fadeIn' style='width: "+ (option.width?option.width:"350") +"px' ></div>";
                $(option.elem).after(fr);
            }

            this.nodeList = [];

            var div = "<div class='message-frame-header'><span style='height:40px;line-height:40px;text-align:center;width:64px;display: inline-block;'>";
            if(option.allMsg){
            	div += "<a href='#' onclick="+option.allMsg+">全部消息</a>";
            } else {
            	div += "消息";
            }
            div += "</span>";
            if(option.msgData && option.msgData.length > 0){
            	div += "<button class='message-btn message-btn-blue message-btn-header' data-type='"+ type +"'>全部已读</button>"
            }
            div += "</div><div class='message-content' style='max-height:"+ (option.height?option.height+"px":"350px") +"'>";
            if (type == 1) {
                if (option.msgData && option.msgData.length > 0) {
                    div += "<ul class='message-content-list'>";
                    for (var i=0; i < (option.msgData.length<option.msgShow?option.msgData.length:option.msgShow); i++) {
                        var node = this.getNode(option.msgData[i], type);
                        this.nodeList.push(node);
                    }
                    var html = this.getNodelistHtml();
                    div += html;
                    div += "</ul>";
                } else {
                    div += "<div class='message-none-msg'>无消息</div>";
                }
                div += "</div>";

                if (this.nodeList.length < option.msgData.length) {
                    div += "<div class='message-footer' data-type='"+ type +"'><span>加载更多</span></div>";
                }
            } else if (type == 2) {
                if (option.noticeData && option.noticeData.length > 0) {
                    div += "<ul class='message-content-list'>";
                    for (var i=0; i < (option.noticeData.length<option.msgShow?option.noticeData.length:option.msgShow); i++) {
                        var node = this.getNode(option.noticeData[i], type);
                        this.nodeList.push(node);
                    }
                    var html = this.getNodelistHtml();
                    div += html;
                    div += "</ul>";
                } else {
                    div += "<div class='message-none-msg'>无提醒</div>";
                }
                div += "</div>";

                if (this.nodeList.length < option.noticeData.length) {
                    div += "<div class='message-footer' data-type='"+ type +"'><span>加载更多</span></div>";
                }
            }


            $(".message-frame").html(div);
        },
        /**
         * 更新（附带刷新）
         * @param opt
         * @param notify
         * @param title
         * @param mes
         */
        update: function(opt, notify, title, mes) {
            this.refresh(this.tp, opt);
            if (notify) {
                this.messageNotify(title, mes);
            }
        },
        /**
         * 刷新
         * @param type
         * @param opt
         */
        refresh: function(type, opt) {
            this.config(opt);
            this.bellDraw();
            if (type) {
                this.contentListDraw(type);
                this.caculatePosition($(".message-bell-btn[data-type='"+ type +"']").get(0));
            }
        },
        /**
         * 计算位置
         * @param elemt
         */
        caculatePosition: function(elemt) {
            //计算位置
            var screenX = window.innerWidth; //屏幕X
            var screenY = window.innerHeight; //屏幕Y

            var objRect = elemt.getBoundingClientRect();

            var objX = objRect.left; //按钮的左边距原点的距离
            var objY = objRect.bottom; //按钮底距顶部的距离

            var editPx = 10;

            var divWidth = $(".message-frame").outerWidth();
            var divHeight = $(".message-frame").outerHeight();

            while (objX + divWidth > screenX) {
                objX -= editPx;
            }
            var divX = objX;

            while (objY + divHeight > screenY) {
                objY -= editPx;
            }

            var divY = objY;

            //$(".message-frame").css('position', "fixed").css('left', divX).css('top', divY);
            //-------
        },
        /**
         * 每条消息组装
         * @param obj
         * @param type
         * @returns {{type: *}}
         */
        getNode: function(obj, type) {
            var node = {
                type: type,
                isRead: true
            };
            var nd = (option.getNodeHtml&&typeof option.getNodeHtml == "function")?option.getNodeHtml(obj, node):null;

            nd?$.extend(true, node, nd):node;

            var html = "<li data='"+obj.id+"'>";
                if (!node.isRead) {
                    html += "<div class='message-content-list-item-detail message-content-list-item-dot'><span class='badge-dot'></span></div>"
                }
                html += "<div class='message-content-list-item-content'>"+ node.html + "</div>";
                html += "</li>";
            node.html = html;
            return node;
        },
        /**
         * 组装消息列表的html
         * @returns {string}
         */
        getNodelistHtml: function() {
            var list = this.nodeList;
            var html = "";
            for (var i=0; i<list.length; i++) {
                html += list[i].html;
            }

            return html;
        },
        /**
         * chrome等浏览器消息通知
         * @param title
         * @param msg
         */
        messageNotify: function(title, msg) {
            var Notification = window.Notification || window.mozNotification || window.webKitNotification;
            if (Notification && Notification.permission !== "granted") {
                Notification.requestPermission(function (status) {
                    if (Notification.permission !== status) {
                        Notification.permission = status;
                    }
                });
            } else if (Notification && Notification.permission === "granted") {
                var options = {
                    dir: "rtl",
                    lang: "zh-CN",
                    body: msg
                };
                var not = new Notification(title, options);
            }


        }
    };

    window.MessagePlugin = api;
    window.MessagePlugin.option = option;
})();