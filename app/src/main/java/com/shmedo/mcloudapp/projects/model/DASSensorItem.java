package com.shmedo.mcloudapp.projects.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/12 <br/>
 * 描述：     TODO
 */
public class DASSensorItem {
    private int resId;
    private boolean isRemoveState=false;

    public DASSensorItem(int resId) {
        this.resId = resId;
    }



    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public boolean isRemoveState() {
        return isRemoveState;
    }

    public void setRemoveState(boolean removeState) {
        isRemoveState = removeState;
    }
}
