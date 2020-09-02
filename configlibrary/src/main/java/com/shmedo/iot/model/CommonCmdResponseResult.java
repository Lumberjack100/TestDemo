package com.shmedo.iot.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/2 <br/>
 * 描述：     设置指令响应结果实体类
 */
public class CommonCmdResponseResult {
    private boolean isSucceed = false;

    private String reason = "";

    public boolean isSucceed() {
        return isSucceed;
    }

    public void setSucceed(boolean succeed) {
        isSucceed = succeed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
