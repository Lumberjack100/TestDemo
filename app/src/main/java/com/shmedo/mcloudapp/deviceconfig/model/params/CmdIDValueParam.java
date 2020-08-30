package com.shmedo.mcloudapp.deviceconfig.model.params;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：     下发指令接口中的指令参数值实体类
 */
public class CmdIDValueParam {
    private int parameterID;//
    private String parameterValue;//

    public int getParameterID() {
        return parameterID;
    }

    public void setParameterID(int parameterID) {
        this.parameterID = parameterID;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }
}
