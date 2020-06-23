package com.sohu.tv.mq.cloud.bo;

/**
 * broker配置
 * 
 * @author yongfeigao
 * @date 2020年5月18日
 */
public class BrokerConfig {
    private int id;
    // group id
    private int gid;
    // 属性名
    private String key;
    // 属性值
    private String value;
    // 描述
    private String desc;
    // 提示
    private String tip;
    // 是否可以动态修改
    private boolean dynamicModify;
    // 可选值
    private String option;
    // 线上值
    private String onlineValue;
    // 顺序
    private int order;
    // 是否必选
    private boolean required;
    // 是否可选
    private boolean canSelect = true;

    public boolean isCanSelect() {
        return canSelect;
    }

    public void setCanSelect(boolean canSelect) {
        this.canSelect = canSelect;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public boolean isDynamicModify() {
        return dynamicModify;
    }

    public void setDynamicModify(boolean dynamicModify) {
        this.dynamicModify = dynamicModify;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getOnlineValue() {
        return onlineValue;
    }
    
    public String getOnlineValueNotNull() {
        return onlineValue == null ? "" : onlineValue;
    }

    public void setOnlineValue(String onlineValue) {
        this.onlineValue = onlineValue;
    }
    
    public String getKeyHtml() {
        if (onlineValue == null || value == null || value.contains("核数")) {
            return key;
        }
        if (!onlineValue.equals(value)) {
            return "<b>" + key + "</b>";
        }
        return key;
    }
    
    public boolean isValueChanged() {
        if (onlineValue == null || value == null || value.contains("核数")) {
            return false;
        }
        if (!onlineValue.equals(value)) {
            return true;
        }
        return false;
    }

    public String getOnlineValueHtml() {
        if (option == null || onlineValue == null) {
            return getInputOnlineValue();
        }
        if (!option.contains(onlineValue)) {
            return getInputOnlineValue();
        }
        StringBuilder sb = new StringBuilder();
        boolean exist = false;
        String[] options = option.split(";");
        for (String op : options) {
            String[] kv = op.split(":");
            if (kv.length == 2) {
                if (kv[0].equals(onlineValue)) {
                    exist = true;
                    sb.append("<input type='radio' data='" + onlineValue + "' dynamic=" + dynamicModify + " id='" + key
                            + "' name='" + key + "' value='" + kv[0] + "' checked='checked'><span>" + kv[1] + "</span>");
                } else {
                    sb.append("<input type='radio' data='" + onlineValue + "' dynamic=" + dynamicModify + " id='" + key
                            + "' name='" + key + "' value='" + kv[0] + "'><span>" + kv[1] + "</span>");
                }
            }
        }
        if (exist) {
            return sb.toString();
        }
        return getInputOnlineValue();
    }

    private String getInputOnlineValue() {
        
        return "<input type='text' style='width:100%;' data='" + getOnlineValueNotNull() + "' dynamic=" + dynamicModify 
                + " id='" + key + "' name='" + key + "' value='" + getOnlineValueNotNull() + "'"
                + ("brokerName".equals(key) ? "placeholder='格式参考: broker-a'" : "")
                + ">";
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "BrokerConfig [id=" + id + ", gid=" + gid + ", key=" + key + ", value=" + value + ", desc=" + desc
                + ", tip=" + tip + ", dynamicModify=" + dynamicModify + ", option=" + option + ", onlineValue="
                + onlineValue + ", order=" + order + "]";
    }
}
