package com.shmedo.mcloudapp.network;

/**
 * 创建时间:  2016/11/14 9:40
 * 描述：    返回错误码标识
 */
public class ErrorInfo {
    private int code;
    private String msg;

    public ErrorInfo() {
    }

    public ErrorInfo(int code, String msg) {
        this.code = code;
        this.msg = msg;
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

}
