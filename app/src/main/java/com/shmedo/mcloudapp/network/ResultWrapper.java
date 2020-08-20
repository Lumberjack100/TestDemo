package com.shmedo.mcloudapp.network;

/**
 * 项目名：  CheckAndroid
 * 包名：    cn.shmedo.checkandroid.data.model.common
 * 文件名:   ResultWrapper
 * 创建者:   yuchao
 * 创建时间:  2016/11/14 9:35
 * 描述：    返回包装数据结果类
 */

public class ResultWrapper<T> {
    private boolean success;
    private ErrCode errCode;
    private T data;

    public ResultWrapper()
    {}

    public ResultWrapper(boolean isSuccess, ErrCode errInfo, T data)
    {
        this.success=isSuccess;
        this.errCode = errInfo;
        this.data=data;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ErrCode getErrCode() {
        return errCode;
    }

    public void setErrCode(ErrCode errCode) {
        this.errCode = errCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static<U> ResultWrapper<U> success(U data)
    {
        return new ResultWrapper<U>(true,ErrCode.success(),data);
    }

    public static ResultWrapper serverException(String exceptionMessage)
    {
        return  new ResultWrapper(false,ErrCode.fail(ErrCode.ERR_SERVER_INTERNAL_ERROR,exceptionMessage),null);
    }

    public static ResultWrapper fail(int errCode)
    {
        return  new ResultWrapper(false,new ErrCode(errCode,null),null);
    }


}
