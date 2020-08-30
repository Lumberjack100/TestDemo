package com.shmedo.mcloudapp.deviceconfig.model.params;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：     下发指令接口入参
 */
public class DispatchCmdParam {

    /**
     * companyID : 1
     * cmdID : 6
     * deviceIDList : [1262]
     * parameterValueList : null
     */

    private int companyID;//当前公司ID
    private int cmdID;//命令ID
    private List<CmdIDValueParam> parameterValueList;//指令参数值，如果该指令没有参数，则本参数为空或者null即可
    private List<Integer> deviceIDList;//设备ID列表

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public int getCmdID() {
        return cmdID;
    }

    public void setCmdID(int cmdID) {
        this.cmdID = cmdID;
    }

    public List<CmdIDValueParam> getParameterValueList() {
        return parameterValueList;
    }

    public void setParameterValueList(List<CmdIDValueParam> parameterValueList) {
        this.parameterValueList = parameterValueList;
    }

    public List<Integer> getDeviceIDList() {
        return deviceIDList;
    }

    public void setDeviceIDList(List<Integer> deviceIDList) {
        this.deviceIDList = deviceIDList;
    }
}
