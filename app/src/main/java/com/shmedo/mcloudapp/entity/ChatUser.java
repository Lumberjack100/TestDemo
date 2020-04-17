package com.shmedo.mcloudapp.entity;

import android.graphics.Bitmap;

import com.github.bassaer.chatmessageview.model.IChatUser;

import org.jetbrains.annotations.Nullable;

/**
 * 项目名：  mCloudapp <br/>
 * 包名：    com.shmedo.mcloudapp.entity <br/>
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/17 <br/>
 * 描述：    TODO
 */
public class ChatUser  implements IChatUser {
    Integer id;
    String name;
    Bitmap icon;

    public ChatUser(Integer id, String name, Bitmap icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    @Override
    public String getId() {
        return this.id.toString();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Nullable
    @Override
    public Bitmap getIcon() {
        return icon;
    }

    @Override
    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }
}
