package com.shmedo.mcloudapp.user.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/7 <br/>
 * 描述：     修改密码参数实体类
 */
public class UpdatePasswordParam {
    private String currentPassword;

    private String newPassword;

    private String confirmNewPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
