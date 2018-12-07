/*
 * Jquery Multiselect插件 中文叫列表多选插件
 * 使用例子:
 * $('table').multiSelect({
 *  actcls: 'active',
 *  selector: 'tbody tr',
 *  callback: false
 * });
 */
(function ($) {
    $.fn.multiSelect = function (options) {
        $.fn.multiSelect.init($(this), options);
    };

    $.extend($.fn.multiSelect, {
        defaults: {
            actcls: 'selected', //选中样式
            selector: 'tr', //选择的行元素
            except: ['tbody'], //选中后不去除多选效果的元素队列
            statics: ['.static'], //被排除行元素条件
            callback: false //选中回调
        },
        first: null, //按shift时，用于记住第一个点击的item
        last: null, //最后点击的item
        init: function (scope, options) {
            this.scope = scope;
            this.options = $.extend({}, this.defaults, options);
            this.initEvent();
        },
        checkStatics: function (dom) {
            for (var i in this.options.statics) {
                if (dom.is(this.options.statics[i])) {
                    return true;
                }
            }
        },
        initEvent: function () {
            var self = this,
                scope = self.scope,
                options = self.options,
                callback = options.callback,
                actcls = options.actcls;

            scope.on('click.mSelect', options.selector, function (e) {
                if (!e.shiftKey && self.checkStatics($(this))) {
                    return;
                }

                if ($(this).hasClass(actcls)) {
                    $(this).removeClass(actcls);
                } else {
                    $(this).addClass(actcls);
                }

                if (e.shiftKey && self.last) {
                    if (!self.first) {
                        self.first = self.last;
                    }
                    var start = self.first.index();
                    var end = $(this).index();
                    if (start > end) {
                        var temp = start;
                        start = end;
                        end = temp;
                    }
                    $(options.selector, scope).removeClass(actcls).slice(start, end + 1).each(function () {
                        if (!self.checkStatics($(this))) {
                            $(this).addClass(actcls);
                        }
                    });
                    window.getSelection ? window.getSelection().removeAllRanges() : document.selection.empty();
                } else if (!e.ctrlKey && !e.metaKey) {
                    $(this).siblings().removeClass(actcls);
                }
                self.last = $(this);
                $.isFunction(callback) && callback($(options.selector + '.' + actcls, scope));
            });

            /**
             * 清楚shift按住的状态
             */
            $(document).on('keyup.mSelect', function (e) {
                if (e.keyCode == 16) {
                    self.first = null;
                }
            });
        }
    });
})(jQuery);