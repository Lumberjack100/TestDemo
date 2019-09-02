package com.shmedo.mcloudapp.entity.parameter;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.parameter
 * 文件名:   SystemParameter
 * 创建者:   dpc
 * 创建时间:  2019/4/15 11:23
 * 描述：    系统列表参数
 */
public class SystemParameter {
    private String companyID;


    public SystemParameter() {
    }


    public SystemParameter(String companyID) {
        this.companyID = companyID;

    }


    public String getCompanyID() {
        return companyID;
    }


    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }


}
