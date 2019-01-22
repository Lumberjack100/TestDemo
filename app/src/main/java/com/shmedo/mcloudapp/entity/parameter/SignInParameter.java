package com.shmedo.mcloudapp.entity.parameter;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.parameter
 * 文件名:   SignInParameter
 * 创建者:   dpc
 * 创建时间:  2019/1/10 10:37
 * 描述：    TODO
 */
public class SignInParameter implements Cloneable{
    private String account;
    private String password;

    public SignInParameter(){

    }
    public SignInParameter(String account,String password){
        this.account=account;
        this.password=password;
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

    @Override
    public SignInParameter clone(){
        try{
            return (SignInParameter) super.clone();
        }   catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}

