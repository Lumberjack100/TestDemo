package com.shmedo.lib.core.network;

import com.google.gson.annotations.SerializedName;

/**
 * 项目名：  CheckAndroid
 * 包名：    cn.shmedo.checkandroid.data.model.common
 * 文件名:   ResultWrapper
 * 创建者:   yuchao
 * 创建时间:  2016/11/14 9:35
 * 描述：    返回包装数据结果类
 */
public class PgyerApiResponse<T> {
    private int code;
    @SerializedName("message")
    private String msg;
    private T data;

    public PgyerApiResponse() {
    }

    public PgyerApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
