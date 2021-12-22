package com.shmedo.mcloudapp.common.model.params;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/22 <br/>
 * 描述：     手机验证码登录参数
 */
public class MobileSignInParameter {
    private String phone;
    private String code;

    public MobileSignInParameter(String phone, String code) {
        this.phone = phone;
        this.code = code;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
