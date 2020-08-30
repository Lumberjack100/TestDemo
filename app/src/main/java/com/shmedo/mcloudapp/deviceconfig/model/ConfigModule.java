package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/27 <br/>
 * 描述：   配置模块
 */
public class ConfigModule {

    private int iconResId;
    private int cmdID;
    private String name;
    private String desc;

    public ConfigModule(int iconResId, String name, String desc) {
        this.iconResId = iconResId;
        this.name = name;
        this.desc = desc;
    }

    public ConfigModule(int iconResId, int cmdID, String name, String desc) {
        this.iconResId = iconResId;
        this.cmdID = cmdID;
        this.name = name;
        this.desc = desc;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getCmdID() {
        return cmdID;
    }

    public void setCmdID(int cmdID) {
        this.cmdID = cmdID;
    }
}
