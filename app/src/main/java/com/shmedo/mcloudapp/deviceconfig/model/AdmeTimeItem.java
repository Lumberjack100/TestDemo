package com.shmedo.mcloudapp.deviceconfig.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/8 <br/>
 * 描述：     ADME 执行机构定点测量时间实体
 */
public class AdmeTimeItem {

    private String time;
    private boolean isAddButton = false;

    public AdmeTimeItem(String time, boolean isAddButton) {
        this.time = time;
        this.isAddButton = isAddButton;
    }

    public String getTime() {
        return TextUtils.isEmpty(time) ? "" : time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isAddButton() {
        return isAddButton;
    }

    public void setAddButton(boolean addButton) {
        isAddButton = addButton;
    }
}
