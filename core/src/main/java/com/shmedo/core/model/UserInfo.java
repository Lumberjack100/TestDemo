package com.shmedo.core.model;

import android.text.TextUtils;

import java.util.List;

/**
 * 项目名：  mobileAndroid
 * 包名：    com.shmedo.mobileandroid.entity
 * 文件名:   UserInfo
 * 创建者:   dpc
 * 创建时间:  2018/11/27 14:48
 */

public class UserInfo {

    /**
     * user : {"id":53,"account":"medo_dpc","name":"杜鹏程","password":"6CD02257A28C39909DD058B25EECAF64","position":"职位","email":"12@123.com","cellPhone":"13562536253","phone":"","address":"","allowAccessType":0,"headPhotoPath":"http://172.168.5.48:8089/images/common/ab5019eb-6b40-48d7-8072-b353504cfc90tupian.png","warnLevel":1,"createUserID":92,"createTime":"2018-03-12 00:00:00","updateUserID":101,"updateTime":"2018-06-27 16:37:57","userEnable":true}
     * departments : [{"id":59,"name":"创新技术中心","companyID":1,"parentID":null,"desc":"创新技术中心","level":0,"readOnly":false,"createUserID":94,"createTime":"2018-04-08 16:25:31","updateUserID":94,"updateTime":"2018-04-08 16:25:31","hasChild":false}]
     */

    private UserBean user;
    private List<DepartmentsBean> departments;


    public UserBean getUser() {
        return user;
    }


    public void setUser(UserBean user) {
        this.user = user;
    }


    public List<DepartmentsBean> getDepartments() {
        return departments;
    }


    public void setDepartments(List<DepartmentsBean> departments) {
        this.departments = departments;
    }


    public static class UserBean {
        /**
         * id : 53
         * account : medo_dpc
         * name : 杜鹏程
         * password : 6CD02257A28C39909DD058B25EECAF64
         * position : 职位
         * email : 12@123.com
         * cellPhone : 13562536253
         * phone :
         * address :
         * allowAccessType : 0
         * headPhotoPath : http://172.168.5.48:8089/images/common/ab5019eb-6b40-48d7-8072-b353504cfc90tupian.png
         * warnLevel : 1
         * createUserID : 92
         * createTime : 2018-03-12 00:00:00
         * updateUserID : 101
         * updateTime : 2018-06-27 16:37:57
         * userEnable : true
         */

        private int id;
        private String account;
        private String name;
        private String password;
        private String position;
        private String email;
        private String cellPhone;
        private String phone;
        private String address;
        private int allowAccessType;
        private String headPhotoPath;
        private int warnLevel;
        private int createUserID;
        private String createTime;
        private int updateUserID;
        private String updateTime;
        private boolean userEnable;


        public int getId() {
            return id;
        }


        public void setId(int id) {
            this.id = id;
        }


        public String getAccount() {
            return TextUtils.isEmpty(account) ? "" : account;
        }


        public void setAccount(String account) {
            this.account = account;
        }


        public String getName() {
            return TextUtils.isEmpty(name) ? "" : name;
        }


        public void setName(String name) {
            this.name = name;
        }


        public String getPassword() {
            return TextUtils.isEmpty(password) ? "" : password;
        }


        public void setPassword(String password) {
            this.password = password;
        }


        public String getPosition() {
            return TextUtils.isEmpty(position) ? "" : position;
        }


        public void setPosition(String position) {
            this.position = position;
        }


        public String getEmail() {
            return TextUtils.isEmpty(email) ? "" : email;
        }


        public void setEmail(String email) {
            this.email = email;
        }


        public String getCellPhone() {
            return TextUtils.isEmpty(cellPhone) ? "" : cellPhone;
        }


        public void setCellPhone(String cellPhone) {
            this.cellPhone = cellPhone;
        }


        public String getPhone() {
            return TextUtils.isEmpty(phone) ? "" : phone;
        }


        public void setPhone(String phone) {
            this.phone = phone;
        }


        public String getAddress() {
            return TextUtils.isEmpty(address) ? "" : address;
        }


        public void setAddress(String address) {
            this.address = address;
        }


        public int getAllowAccessType() {
            return allowAccessType;
        }


        public void setAllowAccessType(int allowAccessType) {
            this.allowAccessType = allowAccessType;
        }


        public String getHeadPhotoPath() {
            return TextUtils.isEmpty(headPhotoPath) ? "" : headPhotoPath;
        }


        public void setHeadPhotoPath(String headPhotoPath) {
            this.headPhotoPath = headPhotoPath;
        }


        public int getWarnLevel() {
            return warnLevel;
        }


        public void setWarnLevel(int warnLevel) {
            this.warnLevel = warnLevel;
        }


        public int getCreateUserID() {
            return createUserID;
        }


        public void setCreateUserID(int createUserID) {
            this.createUserID = createUserID;
        }


        public String getCreateTime() {
            return TextUtils.isEmpty(createTime) ? "" : createTime;
        }


        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }


        public int getUpdateUserID() {
            return updateUserID;
        }


        public void setUpdateUserID(int updateUserID) {
            this.updateUserID = updateUserID;
        }


        public String getUpdateTime() {
            return TextUtils.isEmpty(updateTime) ? "" : updateTime;
        }


        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }


        public boolean isUserEnable() {
            return userEnable;
        }


        public void setUserEnable(boolean userEnable) {
            this.userEnable = userEnable;
        }
    }


    public static class DepartmentsBean {
        /**
         * id : 59
         * name : 创新技术中心
         * companyID : 1
         * parentID : null
         * desc : 创新技术中心
         * level : 0
         * readOnly : false
         * createUserID : 94
         * createTime : 2018-04-08 16:25:31
         * updateUserID : 94
         * updateTime : 2018-04-08 16:25:31
         * hasChild : false
         */

        private int id;
        private String name;
        private int companyID;
        private Object parentID;
        private String desc;
        private int level;
        private boolean readOnly;
        private int createUserID;
        private String createTime;
        private int updateUserID;
        private String updateTime;
        private boolean hasChild;


        public int getId() {
            return id;
        }


        public void setId(int id) {
            this.id = id;
        }


        public String getName() {
            return TextUtils.isEmpty(name) ? "" : name;
        }


        public void setName(String name) {
            this.name = name;
        }


        public int getCompanyID() {
            return companyID;
        }


        public void setCompanyID(int companyID) {
            this.companyID = companyID;
        }


        public Object getParentID() {
            return parentID;
        }


        public void setParentID(Object parentID) {
            this.parentID = parentID;
        }


        public String getDesc() {
            return TextUtils.isEmpty(desc) ? "" : desc;
        }


        public void setDesc(String desc) {
            this.desc = desc;
        }


        public int getLevel() {
            return level;
        }


        public void setLevel(int level) {
            this.level = level;
        }


        public boolean isReadOnly() {
            return readOnly;
        }


        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }


        public int getCreateUserID() {
            return createUserID;
        }


        public void setCreateUserID(int createUserID) {
            this.createUserID = createUserID;
        }


        public String getCreateTime() {
            return TextUtils.isEmpty(createTime) ? "" : createTime;
        }


        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }


        public int getUpdateUserID() {
            return updateUserID;
        }


        public void setUpdateUserID(int updateUserID) {
            this.updateUserID = updateUserID;
        }


        public String getUpdateTime() {
            return TextUtils.isEmpty(updateTime) ? "" : updateTime;
        }


        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }


        public boolean isHasChild() {
            return hasChild;
        }


        public void setHasChild(boolean hasChild) {
            this.hasChild = hasChild;
        }
    }
}
