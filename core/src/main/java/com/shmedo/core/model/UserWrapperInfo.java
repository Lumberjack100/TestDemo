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

public class UserWrapperInfo {
    private UserInfo user;
    private List<DepartmentInfo> departments;

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }


    public List<DepartmentInfo> getDepartments() {
        return departments;
    }

    public void setDepartments(List<DepartmentInfo> departments) {
        this.departments = departments;
    }


    public static class UserInfo {
        /**
         "userID": 636,
         "companyID": 138,
         "companyName": "上海米度测控科技有限公司",
         "account": "medo_gh",
         "name": "宫贺",
         "cellPhone": "13915272257",
         "position": "",
         "email": "",
         "phone": "",
         "address": "",
         "allowAccessType": 0,
         "headPhotoPath": null,
         "userEnable": true,
         "createUserID": 1,
         "createTime": "2021-12-20 17:40:31",
         "updateUserID": 636,
         "updateTime": "2021-12-21 17:10:41",
         "expireTime": null,
         "ssoUser": false,
         "ssoToken": null
         */
        private int userID;
        private int companyID;
        private String companyName;
        private String account;
        private String name;
        private String cellPhone;
        private String position;
        private String email;
        private String phone;
        private String address;
        private int allowAccessType;
        private String headPhotoPath;
        private boolean userEnable;
        private int createUserID;
        private String createTime;
        private int updateUserID;
        private String updateTime;
        private String expireTime;
        private boolean ssoUser;
        private String ssoToken;

        public int getUserID() {
            return userID;
        }

        public void setUserID(int userID) {
            this.userID = userID;
        }

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


    public static class DepartmentInfo {
        /**
         "id": 326,
         "name": "产品开发部",
         "companyID": 138,
         "parentID": 323,
         "desc": null,
         "level": 2,
         "readOnly": false,
         "displayOrder": 1,
         "createUserID": 539,
         "createTime": "2021-07-22 11:32:08",
         "updateUserID": 539,
         "updateTime": "2021-07-22 11:32:08",
         "hasChild": false,
         "delete": false
         */
        private int id;
        private String name;
        private int companyID;
        private int parentID;
        private String desc;
        private int level;
        private boolean readOnly;
        private int displayOrder;
        private int createUserID;
        private String createTime;
        private int updateUserID;
        private String updateTime;
        private boolean hasChild;
        private boolean delete;

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


        public int getParentID() {
            return parentID;
        }

        public void setParentID(int parentID) {
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

        public int getDisplayOrder() {
            return displayOrder;
        }

        public void setDisplayOrder(int displayOrder) {
            this.displayOrder = displayOrder;
        }

        public boolean isDelete() {
            return delete;
        }

        public void setDelete(boolean delete) {
            this.delete = delete;
        }
    }
}
