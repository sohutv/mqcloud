package com.sohu.tv.mq.cloud.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
/**
 * 压缩工具
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月22日
 */
public class CompressUtil {

    public static final String UTF8 = "UTF-8";
    
    /**
     * 将字符串压缩后编码为base64
     * @param obj
     * @return
     * @throws IOException
     */
    public static String compress(String str) throws IOException {
        byte[] compressedObj = compress(str.getBytes(UTF8));
        return Base64.encodeBase64String(compressedObj);
    }
    
    /**
     * 将base64字符串解码后进行解压缩
     * @param base64Str
     * @return
     * @throws IOException
     */
    public static String uncompress(String base64Str) throws IOException {
        byte[] compressedObj = uncompress(Base64.decodeBase64(base64Str));
        return new String(compressedObj, UTF8);
    }
    
    /**
     * 压缩
     * @param bytes
     * @return
     * @throws IOException
     */
    public static byte[] compress(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(bytes);
        gzip.close();
        return out.toByteArray();
    }
 
    /**
     * 解压缩
     * @param bytes
     * @return
     * @throws IOException
     */
    public static byte[] uncompress(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        GZIPInputStream gunzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n = -1;
        while ((n = gunzip.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

}
