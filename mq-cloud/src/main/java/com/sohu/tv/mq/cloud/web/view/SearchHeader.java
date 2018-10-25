package com.sohu.tv.mq.cloud.web.view;

import java.util.List;

/**
 * 查询区域
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月29日
 */
public class SearchHeader {
    // 提示
    private String tip;

    // 查询选项列表
    private List<SearchField> searchFieldList;

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public List<SearchField> getSearchFieldList() {
        return searchFieldList;
    }

    public void setSearchFieldList(List<SearchField> searchFieldList) {
        this.searchFieldList = searchFieldList;
    }

    /**
     * 查询的字段
     */
    public static abstract class SearchField {
        // 查询的这个组件显示的名字
        private String title;
        // 查询的这个组件的key 用于后台搜索条件
        private String key;
        // 默认值
        private Object value;
        // 是否显示
        private boolean hidden;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public boolean isHidden() {
            return hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }

        public abstract int getType();
    }

    /**
     * 查询类型
     */
    public static enum SearchFieldType {
        INPUT(1), SELECT(2), DATE(3), DATE_BETWEEN(4),
        ;
        private int type;

        private SearchFieldType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    /**
     * 输入框，比如查询vid
     */
    public static class InputSearchField extends SearchField {
        public int getType() {
            return SearchFieldType.INPUT.ordinal();
        }
    }

    /**
     * 隐藏域
     */
    public static class HiddenSearchField extends InputSearchField {

        @Override
        public boolean isHidden() {
            return true;
        }
    }

    /**
     * 下拉框，比如选择时间
     */
    public static class SelectSearchField extends SearchField {
        // 下拉列表的值与显示的值
        private List<KV> kvList;

        public List<KV> getKvList() {
            return kvList;
        }

        public void setKvList(List<KV> kvList) {
            this.kvList = kvList;
        }

        public int getType() {
            return SearchFieldType.SELECT.ordinal();
        }

        public static class KV {
            private String k;
            private Object v;

            public String getK() {
                return k;
            }

            public void setK(String k) {
                this.k = k;
            }

            public Object getV() {
                return v;
            }

            public void setV(Object v) {
                this.v = v;
            }
        }
    }

    /**
     * 日期组件
     */
    public static class DateSearchField extends SearchField {
        // 日期组件显示多少天之前
        private int daysBefore;
        
        public int getDaysBefore() {
            return daysBefore;
        }

        public void setDaysBefore(int daysBefore) {
            this.daysBefore = daysBefore;
        }

        public int getType() {
            return SearchFieldType.DATE.ordinal();
        }
    }

    /**
     * 日期间隔
     */
    public static class DateBetweenSearchField extends SearchField {
        // 起始日期 格式yyyy-MM-dd
        private String begin;
        // 结束日期 格式yyyy-MM-dd
        private String end;

        public String getBegin() {
            return begin;
        }

        public void setBegin(String begin) {
            this.begin = begin;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public int getType() {
            return SearchFieldType.DATE_BETWEEN.ordinal();
        }
    }
}
