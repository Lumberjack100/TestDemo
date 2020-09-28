package com.shmedo.mcloudapp.user.model.params;

/**
 * 项目名：  das-config-app
 * 包名：    com.example.medoDas.request.parameters
 * 文件名:   SetUserHeadPhotoParameter
 * 创建者:   dpc
 * 创建时间:  2018/1/25 23:04
 * 描述：    上传自己的用户头像
 * @author adu
 */

public class SetUserHeadPhotoParameter {


    private String photoName;
    private String photoContent;

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getPhotoContent() {
        return photoContent;
    }

    public void setPhotoContent(String photoContent) {
        this.photoContent = photoContent;
    }
}
