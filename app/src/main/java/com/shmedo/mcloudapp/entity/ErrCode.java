package com.shmedo.mcloudapp.entity;

import com.shmedo.mcloudapp.util.StringUtil;

/**
 * 项目名：  CheckAndroid
 * 包名：    cn.shmedo.checkandroid.data.model.common
 * 文件名:   ErrCode
 * 创建者:   yuchao
 * 创建时间:  2016/11/14 9:40
 * 描述：    返回错误码标识
 */

public class ErrCode {
    public static final  int NONE=0;
    public static final int ERR_USERNAME=1;
    public static final int ERR_PASSWORD=2;
    public static final int ERR_POWER=3;
    public static final int ERR_ACCESSTOKEN=4;
    public static final int ERR_VERSION=5;
    public static final int ERR_NO_SENSORTYPE=6;
    public static final int ERR_NO_SENSOR=7;
    public static final int ERR_SERVER_INTERNAL_ERROR=8;
    public static final int ERR_ACCESS_TYPE=9;
    private int code;
    private String errMessage;
    public ErrCode(){}


    public ErrCode(int code, String errMessage) {
        this.code = code;
        this.errMessage = errMessage;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


    public String getErrMessage() {
        return errMessage;
    }


    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }


    public static ErrCode success()
    {
        return  new ErrCode(ErrCode.NONE,null);
    }

    public static ErrCode fail(int errCode,String errMessage)
    {
        return new ErrCode(errCode,errMessage);
    }


    @Override public String toString() {
        String message="服务访问错误代码："+ Integer.toString(code);
        return StringUtil.isNullOrEmpty(this.errMessage) ? message : (message+"  "+ this.errMessage);
    }


}
