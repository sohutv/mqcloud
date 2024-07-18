package com.sohu.tv.mq.cloud.bo;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页日志
 */
public class PageLog {

    private List<String> content;

    private String error;

    // 文件偏移量（行数）
    private int offset;

    // 下次读取的偏移量
    private int nextOffset;

    // 读取的行数
    private int size;

    public List<String> getContent() {
        return content;
    }

    public int getContentSize() {
        return content == null ? 0 : content.size();
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public void addContent(String content) {
        if (this.content == null) {
            this.content = new ArrayList<>();
        }
        this.content.add(content);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isMore() {
        return nextOffset > offset;
    }

    public int getNextOffset() {
        return nextOffset;
    }

    public void setNextOffset(int nextOffset) {
        this.nextOffset = nextOffset;
    }
}
