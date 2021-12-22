package com.shmedo.core.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/22 <br/>
 * 描述：     基本用户信息
 */
public class BasicUserInfo {
    private int subjectID;//用户ID
    private String subjectName;//用户名称
    private int companyID;//公司ID
    private String subjectType;//类型,默认USER
    private String imageUrl;//用户头像
    private String phone;//用户电话
    private String email;

    public int getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(int subjectID) {
        this.subjectID = subjectID;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
