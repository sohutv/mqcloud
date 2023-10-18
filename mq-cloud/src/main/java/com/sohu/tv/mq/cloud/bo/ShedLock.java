package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.util.DateUtil;

import java.util.Date;

/**
 * 任务执行锁
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/21
 */
public class ShedLock {
    private String name;
    private Date lockUntil;
    private Date lockedAt;
    private String lockedBy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLockUntil() {
        return lockUntil;
    }

    public String getLockUntilFormat() {
        if (getLockUntil() == null) {
            return null;
        }
        try {
            return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(getLockUntil());
        } catch (Exception e) {
            return String.valueOf(getLockUntil());
        }
    }

    public void setLockUntil(Date lockUntil) {
        this.lockUntil = lockUntil;
    }

    public Date getLockedAt() {
        return lockedAt;
    }

    public String getLockedAtFormat() {
        if (getLockedAt() == null) {
            return null;
        }
        try {
            return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(getLockedAt());
        } catch (Exception e) {
            return String.valueOf(getLockedAt());
        }
    }

    public void setLockedAt(Date lockedAt) {
        this.lockedAt = lockedAt;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    @Override
    public String toString() {
        return "ShedLock{" +
                "name='" + name + '\'' +
                ", lockUntil='" + lockUntil + '\'' +
                ", lockedAt='" + lockedAt + '\'' +
                ", lockedBy='" + lockedBy + '\'' +
                '}';
    }
}
