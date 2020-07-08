package com.shmedo.mcloudapp.user.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/8 <br/>
 * 描述：    修改用户信息参数实体
 */
public class UpdateMyInfoParam {
    private String name;
    private String position;
    private String email;
//    private String cellPhone;
    private String phone;
    private String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

//    public String getCellPhone() {
//        return cellPhone;
//    }
//
//    public void setCellPhone(String cellPhone) {
//        this.cellPhone = cellPhone;
//    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
