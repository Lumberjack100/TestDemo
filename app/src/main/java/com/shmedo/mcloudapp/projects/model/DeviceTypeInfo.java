package com.shmedo.mcloudapp.projects.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/18 <br/>
 * 描述：  设备类型
 */
public class DeviceTypeInfo {
    private String name;//类型名称
    private int num;//设备数量
    private boolean isChecked = false;

    public DeviceTypeInfo(String name, int num) {
        this.name = name;
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
