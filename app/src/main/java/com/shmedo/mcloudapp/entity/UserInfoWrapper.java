package com.shmedo.mcloudapp.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   UserInfoWrapper
 * 创建者:   dpc
 * 创建时间:  2019/1/10 09:19
 * 描述：    TODO
 */
@Entity
public class UserInfoWrapper {
    @Unique
    @Id
    private Long id;
    private String userInfo;


    @Generated(hash = 421840028)
    public UserInfoWrapper(Long id, String userInfo) {
        this.id = id;
        this.userInfo = userInfo;
    }


    @Generated(hash = 2055202865)
    public UserInfoWrapper() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getUserInfo() {
        return userInfo;
    }


    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
}
