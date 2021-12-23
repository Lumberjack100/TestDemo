package com.shmedo.mcloudapp.user.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/19 <br/>
 * 描述：    公司简单信息实体
 */
public class BasicCompanyInfo {
    /**
     * "companyID": 138,
     * "companyName": "上海米度测控科技有限公司"
     */
    private int companyID;
    private String companyName;
    private boolean isChecked = false;

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
