package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.Range;

/**
 * 分页对象
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
}
