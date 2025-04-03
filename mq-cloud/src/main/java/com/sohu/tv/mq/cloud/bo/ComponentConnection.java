package com.sohu.tv.mq.cloud.bo;

/**
 * RocketMQ组件链接
 *
 * @Auther: yongfeigao
 * @Date: 2025/03/04
 */
public class ComponentConnection implements Comparable<ComponentConnection> {

    // 地址
    private String addr;

    // 名字
    private String name;

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return addr.split(":")[0];
    }

    @Override
    public int compareTo(ComponentConnection o) {
        if (name == null || o.name == null) {
            if (name != null) {
                return 1;
            }
            if (o.name != null) {
                return -1;
            }
            return addr.compareTo(o.addr);
        }
        int result = name.compareTo(o.name);
        if (result != 0) {
            return result;
        }
        return addr.compareTo(o.addr);
    }
}
