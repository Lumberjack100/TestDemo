package com.shmedo.mcloudapp.deviceconfig.model.params;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：    指令透传接口入参
 */
public class DispatchRawCmdParam {
    private String cmdContent;//透传内容，须以$cmd=开头
    private List<String> deviceTokenList;//设备SN号列表

    public String getCmdContent() {
        return cmdContent;
    }

    public void setCmdContent(String cmdContent) {
        this.cmdContent = cmdContent;
    }

    public List<String> getDeviceTokenList() {
        return deviceTokenList;
    }

    public void setDeviceTokenList(List<String> deviceTokenList) {
        this.deviceTokenList = deviceTokenList;
    }
}
