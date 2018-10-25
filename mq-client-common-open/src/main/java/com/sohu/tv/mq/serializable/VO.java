package com.sohu.tv.mq.serializable;

import java.io.Serializable;

/**
 * Created by yijunzhang on 14-4-2.
 */
public class VO<T> implements Serializable {
    private static final long serialVersionUID = -9122648735953131186L;
    private T value;

    public VO(T value) {
        this.value = value;
    }

    public VO() {
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "VO{" +
                "value=" + value +
                '}';
    }
}
