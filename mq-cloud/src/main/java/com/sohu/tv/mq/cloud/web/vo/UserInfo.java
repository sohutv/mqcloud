package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 外部登陆的用户
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月12日
 */
public class UserInfo {
    public static final String USER_INFO = "userInfo";
    // 用户信息
    private Result<User> userResult;
    // 登陆时间
    private long loginTime;
    // 登陆ip
    private String ip;
    // loginId: 代表从网关携带过来的身份凭证
    private String loginId;
    
    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public boolean isOK() {
        if(userResult == null) {
            return false;
        }
        return userResult.isOK();
    }
    
    public Exception getError() {
        if(userResult == null) {
            return null;
        }
        return userResult.getException();
    } 
    
    public User getUser() {
        if(userResult == null) {
            return null;
        }
        return userResult.getResult();
    }

    public void setUserResult(Result<User> userResult) {
        this.userResult = userResult;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "UserInfo [userResult=" + userResult + ", loginTime=" + loginTime + ", ip=" + ip + ", loginId=" + loginId
                + "]";
    }
}
