package com.shmedo.mcloudapp.deviceconfig.model.params;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：    指令详细参数值实体类
 */
public class CmdParam {


    /**
     * id : 13
     * cmdID : 24
     * parameterChnName : 固件HTTP地址
     * parameterEngName : url
     * parameterType : String
     * defaultValue : null
     */

    private int id;
    private int cmdID;
    private String parameterChnName;
    private String parameterEngName;
    private String parameterType;
    private Object defaultValue;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCmdID() {
        return cmdID;
    }

    public void setCmdID(int cmdID) {
        this.cmdID = cmdID;
    }

    public String getParameterChnName() {
        return parameterChnName;
    }

    public void setParameterChnName(String parameterChnName) {
        this.parameterChnName = parameterChnName;
    }

    public String getParameterEngName() {
        return parameterEngName;
    }

    public void setParameterEngName(String parameterEngName) {
        this.parameterEngName = parameterEngName;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
