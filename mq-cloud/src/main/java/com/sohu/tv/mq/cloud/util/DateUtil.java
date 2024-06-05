package com.sohu.tv.mq.cloud.util;

import org.apache.commons.lang3.math.NumberUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 线程安全的日期工具类
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月26日
 */
public class DateUtil {
    public static final String HHMM = "HHmm";
    public static final String YMD = "yyyyMMdd";
    public static final String YMDH = "yyyyMMddHH";
    public static final String HHMM_COLON = "HH:mm";
    public static final String YMDHM = "yyyyMMddHHmm";
    public static final String YMD_DASH = "yyyy-MM-dd";
    public static final String YMDHMS = "yyyyMMddHHmmss";
    public static final String YMDHM_DASH = "yyyy-MM-dd-HH-mm";
    public static final String YMD_DASH_DOT_H = "yyyy-MM-dd.HH";
    public static final String YMDHMS_DOT_SSS = "yyyyMMddHHmmss.SSS";
    public static final String YMD_BLANK_HM_COLON = "yyyyMMdd HH:mm";
    public static final String YMD_BLANK_HMS_COLON = "yyyyMMdd HH:mm:ss";
    public static final String YMD_DASH_BLANK_HMS_COLON = "yyyy-MM-dd HH:mm:ss";
    public static final String YMD_BLANK_HMS_COLON_DOT_SSS = "yyyyMMdd HH:mm:ss.SSS";
    public static final String YMD_DASH_HMS_COLON_DOT_SSS = "yyyy-MM-dd HH:mm:ss.SSS";

    private static ThreadLocal<Map<String, SimpleDateFormat>> threadLocal = new ThreadLocal<Map<String, SimpleDateFormat>>();

    /**
     * 获取当前时间的格式化字符串
     * @param pattern
     * @return 格式化字符串
     */
    public static String getFormatNow(String pattern) {
        return getFormat(pattern, Locale.getDefault()).format(new Date());
    }

    /**
     * 根据特定格式获取SimpleDateFormat
     * @param pattern
     * @return SimpleDateFormat
     */
    public static SimpleDateFormat getFormat(String pattern) {
        return getFormat(pattern, Locale.getDefault());
    }

    /**
     * 根据特定格式获取SimpleDateFormat
     * @param pattern
     * @param locale
     * @return
     */
    public static SimpleDateFormat getFormat(String pattern,
            Locale locale) {
        Map<String, SimpleDateFormat> formatMap = threadLocal.get();
        SimpleDateFormat format = null;
        if (formatMap == null) {
            formatMap = new HashMap<String, SimpleDateFormat>();
            format = new SimpleDateFormat(pattern, locale);
            formatMap.put(pattern, format);
            threadLocal.set(formatMap);
        } else {
            format = formatMap.get(pattern);
            if (format == null) {
                format = new SimpleDateFormat(pattern, locale);
                formatMap.put(pattern, format);
            }
        }
        return format;
    }

    /**
     * 采用 @DateUtil.YMD 格式的SimpleDateFormat解析字符串
     * @param str
     * @return Date
     */
    public static Date parseYMD(String str) {
        return parse(YMD, str);
    }

    /**
     * 采用 @DateUtil.YMD 格式的SimpleDateFormat格式化日期
     * @param date
     * @return String
     */
    public static String formatYMD(Date date) {
        return getFormat(YMD).format(date);
    }
    
    public static int format(Date date) {
        return NumberUtils.toInt(formatYMD(date));
    }
    
    /**
     * 采用 @DateUtil.YMD_DASH 格式的SimpleDateFormat解析字符串
     * @param str
     * @return Date
     */
    public static Date parse(String formator, String str) {
        try {
            return getFormat(formator).parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算两个日期之间的天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int daysBetween(Date date1, Date date2) {
        return (int) ((date1.getTime() - date2.getTime()) / (1000 * 60 * 60 * 24L));
    }
}