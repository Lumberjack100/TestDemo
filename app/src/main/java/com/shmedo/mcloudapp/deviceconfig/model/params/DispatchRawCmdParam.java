package com.shmedo.mcloudapp.deviceconfig.model.params;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：    指令透传接口入参
 */
public class DispatchRawCmdParam {
    private int companyID;//当前公司ID
    private String content;//透传内容，须以$cmd=开头
    private List<Integer> deviceIDList;//设备ID列表

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Integer> getDeviceIDList() {
        return deviceIDList;
    }

    public void setDeviceIDList(List<Integer> deviceIDList) {
        this.deviceIDList = deviceIDList;
    }
}
