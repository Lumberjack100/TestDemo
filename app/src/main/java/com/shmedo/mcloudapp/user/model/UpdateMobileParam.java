package com.shmedo.mcloudapp.user.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/8 <br/>
 * 描述：      修改手机号参数实体类
 */
public class UpdateMobileParam {

    /**
     * newCellPhone : 13915283356
     * code : 123456
     */

    private String newCellPhone;
    private String code;

    public String getNewCellPhone() {
        return newCellPhone;
    }

    public void setNewCellPhone(String newCellPhone) {
        this.newCellPhone = newCellPhone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
