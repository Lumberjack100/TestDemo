package com.shmedo.mcloudapp.network;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.model
 * 文件名:   BaseObserver
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:35
 *
 */
public  abstract class BaseObserver<T> implements Observer<ResultWrapper<T>> {

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(ResultWrapper<T> baseResponse) {
        if (baseResponse.isSuccess()) {
            String msg = baseResponse.getErrCode().getErrMessage() != null ? baseResponse.getErrCode().getErrMessage() : "";
            if (baseResponse.getData() != null) {
                Success(baseResponse.getData(), msg);
            }
        } else {
            String msg = baseResponse.getErrCode().getErrMessage() != null ? baseResponse.getErrCode().getErrMessage() : "请求失败";
            Failure(msg);
        }
    }

    @Override
    public void onError(Throwable e) {
        Failure(e.getMessage());
    }

    @Override
    public void onComplete() {

    }

    //请求成功返回结果和信息
    public abstract void Success(T t, String message);

    //请求失败返回错误信息
    public abstract void Failure(String message);
}
