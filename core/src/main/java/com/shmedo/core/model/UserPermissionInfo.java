package com.shmedo.core.model;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/1/11 <br/>
 * 描述：     查询用户在某公司某服务中的所有权限
 */
public class UserPermissionInfo {
    private int id;//权限id
    private String name;//权限名
    private String permissionToken;//权限token
    private String serviceName;//权限所属服务
    private String permissionDesc;//权限描述
    private String exValues;//权限拓展

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

    public String getPermissionToken() {
        return TextUtils.isEmpty(permissionToken) ? "" : permissionToken;
    }

    public void setPermissionToken(String permissionToken) {
        this.permissionToken = permissionToken;
    }

    public String getServiceName() {
        return TextUtils.isEmpty(serviceName) ? "" : serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPermissionDesc() {
        return permissionDesc;
    }

    public void setPermissionDesc(String permissionDesc) {
        this.permissionDesc = permissionDesc;
    }

    public String getExValues() {
        return exValues;
    }

    public void setExValues(String exValues) {
        this.exValues = exValues;
    }
}
