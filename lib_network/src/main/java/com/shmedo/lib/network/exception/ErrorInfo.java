package com.shmedo.lib.network.exception;

import android.text.TextUtils;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.JsonSyntaxException;
import com.shmedo.lib.network.R;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import rxhttp.wrapper.exception.HttpStatusCodeException;
import rxhttp.wrapper.exception.ParseException;

/**
 * Http请求错误信息
 * User: ljx
 * Date: 2019-06-26
 * Time: 14:26
 */
public class ErrorInfo {
    private int errorCode;  //仅指服务器返回的错误码
    private String errorMsg; //错误文案，网络错误、请求失败错误、服务器返回的错误等文案
    private Throwable throwable; //异常信息

    public ErrorInfo(Throwable throwable) {
        this.throwable = throwable;
        String errorMsg = null;
        if (throwable instanceof UnknownHostException) {
            if (!NetworkUtils.isAvailable()) {
                errorMsg = getString(R.string.network_error_tip);
            } else {
                errorMsg = getString(R.string.notify_no_network);
            }
        } else if (throwable instanceof SocketTimeoutException || throwable instanceof TimeoutException) {
            //前者是通过OkHttpClient设置的超时引发的异常，后者是对单个请求调用timeout方法引发的超时异常
            errorMsg = getString(R.string.network_connect_timeout);
        } else if (throwable instanceof ConnectException) {
            errorMsg = getString(R.string.network_connect_error);
        } else if (throwable instanceof HttpStatusCodeException) { //请求失败异常
            String code = throwable.getLocalizedMessage();
            this.errorCode = Integer.parseInt(code);
            if ("416".equals(code)) {
                errorMsg = "请求范围不符合要求";
            } else {
                errorMsg = throwable.getMessage();
            }
        } else if (throwable instanceof JsonSyntaxException) { //请求成功，但Json语法异常,导致解析失败
            errorMsg = "数据解析失败,请稍后再试";
        } else if (throwable instanceof ParseException) { // ParseException异常表明请求成功，但是数据不正确
            String code = throwable.getLocalizedMessage();
            this.errorCode = Integer.parseInt(code);
            errorMsg = throwable.getMessage();
            if (TextUtils.isEmpty(errorMsg))
                errorMsg = code;//errorMsg为空，显示errorCode
        } else {
            errorMsg = throwable.getMessage();
        }
        this.errorMsg = errorMsg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean show() {
        ToastUtils.showShort(TextUtils.isEmpty(errorMsg) ? throwable.getMessage() : errorMsg);
        return true;
    }

    /**
     * @param standbyMsg 备用的提示文案
     */
    public boolean show(String standbyMsg) {
        ToastUtils.showShort(TextUtils.isEmpty(errorMsg) ? standbyMsg : errorMsg);
        return true;
    }

    /**
     * @param standbyMsg 备用的提示文案
     */
    public boolean show(int standbyMsg) {
        ToastUtils.showShort(TextUtils.isEmpty(errorMsg) ? StringUtils.getString(standbyMsg) : errorMsg);
        return true;
    }

    public String getString(int resId) {
        return StringUtils.getString(resId);
    }
}
