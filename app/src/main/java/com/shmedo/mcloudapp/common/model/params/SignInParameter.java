package com.shmedo.mcloudapp.common.model.params;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.parameter
 * 文件名:   SignInParameter
 * 创建者:   dpc
 * 创建时间:  2019/1/10 10:37
 */
public class SignInParameter {
    private String account;
    private String password;

    public SignInParameter() {

    }

    public SignInParameter(String account) {
        this.account = account;
    }

    public SignInParameter(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

