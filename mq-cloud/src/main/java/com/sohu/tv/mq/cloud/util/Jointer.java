package com.sohu.tv.mq.cloud.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * 拼接工具 refrence from com.google.common.base.Joiner
 * 
 * @author yongfeigao
 * @date 2019年7月10日
 */
public class Jointer {
    // 拼接分隔符
    private final String separator;

    public static final Jointer BY_COMMA = new Jointer(",");

    public static final Jointer BY_SEMICOLON = new Jointer(";");

    public static final String BLANK = "";

    private Jointer(String separator) {
        this.separator = separator;
    }

    /**
     * 遍历collection的对象获取其joinerValue进行拼接
     * @param collection
     * @param joinerValue
     * @return String
     */
    public <E> String join(Collection<E> collection, JointerValue<E> joinerValue) {
        if (collection == null || collection.isEmpty()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        Iterator<E> iterator = collection.iterator();
        buffer.append(joinerValue.getValue(iterator.next()));
        while (iterator.hasNext()) {
            buffer.append(separator);
            buffer.append(joinerValue.getValue(iterator.next()));
        }
        return buffer.toString();
    }

    /**
     * 拼接返回值
     * 
     * @author yongfeigao
     * @date 2019年7月11日
     * @param <T>
     */
    public interface JointerValue<T> {
        
        /**
         * 返回打算拼接的字符串
         * @param t
         * @return
         */
        public String getValue(T t);
    }
}
