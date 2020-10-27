package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.Range;

/**
 * 分页对象
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月9日
 */
public class PaginationParam {
    // 当前页
    @Range(min = 1, max = 10000)
    private int currentPage = 1;
    // 一页多少条
    private int numOfPage = 10;
    // 总页数
    private int totalPages;

    // 计算后的起始偏移量
    private int begin;

    // 计算后的结束偏移量
    private int end;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getNumOfPage() {
        return numOfPage;
    }

    public void setNumOfPage(int numOfPage) {
        this.numOfPage = numOfPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * 计算分页数据
     * 
     * @param totalNum 总数
     */
    public void caculatePagination(int totalNum) {
        begin = Math.min((currentPage - 1) * numOfPage, totalNum);
        end = Math.min(begin + numOfPage, totalNum);
        totalPages = (totalNum + numOfPage - 1) / numOfPage;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }
}
