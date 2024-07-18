package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 数据迁移参数
 *
 * @author yongfeigao
 * @date 2024年07月09日
 */
public class DataMigrationParam {
    @NotBlank
    private String sourceIp;
    @NotBlank
    private String sourcePath;
    @NotBlank
    private String destIp;
    @NotBlank
    private String destPath;

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public String getDestPath() {
        return destPath;
    }

    public void setDestPath(String destPath) {
        this.destPath = destPath;
    }

    @Override
    public String toString() {
        return "DataMigrationParam{" +
                "sourceIp='" + sourceIp + '\'' +
                ", sourcePath='" + sourcePath + '\'' +
                ", destIp='" + destIp + '\'' +
                ", destPath='" + destPath + '\'' +
                '}';
    }
}
