package com.shmedo.configlibrary.iot.cmd.entity;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/3 <br/>
 * 描述：     语音播报参数
 */
public class BroadcastEntity implements Validater {
    private int b_num;// 播报遍数
    private int b_size;//播报内容大学
    private String b_content;//播报内容 utf-8

    public void setB_num(int b_num) {
        this.b_num = b_num;
    }

    public void setB_size(int b_size) {
        this.b_size = b_size;
    }

    public void setB_content(String b_content) {
        this.b_content = b_content;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("b_num=" + b_num);
        stringBuilder.append("&");
        stringBuilder.append("b_size=" + b_size);
        stringBuilder.append("&");
        stringBuilder.append("b_content=" + (TextUtils.isEmpty(b_content) ? "" : b_content));

        return stringBuilder.toString();
    }
}
