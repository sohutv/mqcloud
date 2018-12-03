package com.sohu.tv.mq.cloud.util;

/**
 * 分隔工具
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月27日
 */
public class SplitUtil {

    /**
     * 每maxSize个字符，加一个\n
     * 
     * @param str
     * @param maxSize
     * @return
     */
    public static String getNewLine(String str, int maxSize) {
        return split(str, "\\n", maxSize);
    }

    public static String getBRLine(String str, int maxSize) {
        return split(str, "<br>", maxSize).replaceAll("\n", "<br>");
    }

    /**
     * 每maxSize个字符，加一个splitor
     * 
     * @param str
     * @param maxSize
     * @return
     */
    public static String split(String str, String splitor, int maxSize) {
        if (str.length() <= maxSize) {
            return str;
        }
        int count = str.length() / maxSize;
        if (str.length() % maxSize != 0) {
            count += 1;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            int end = (i + 1) * maxSize;
            if (end > str.length()) {
                end = str.length();
            }
            sb.append(str.substring(i * maxSize, end));
            if (i < count - 1) {
                sb.append(splitor);
            }
        }
        return sb.toString();
    }
}
