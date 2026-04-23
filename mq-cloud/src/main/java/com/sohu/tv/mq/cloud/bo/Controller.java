package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * Controller
 * 
 * @author yongfeigao
 * @date 2023年05月22日
 */
public class Controller extends DeployableComponent {

    private boolean leader;
    private String group;

    @Override
    public String getComponentName() {
        return "controller";
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "Controller " + super.toString();
    }
}
