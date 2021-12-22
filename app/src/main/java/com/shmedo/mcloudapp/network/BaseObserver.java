package com.shmedo.mcloudapp.network;


import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.model
 * 文件名:   BaseObserver
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:35
 */
public abstract class BaseObserver<T> implements Observer<ResponseWrapper<T>> {

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(ResponseWrapper<T> baseResponse) {
        ErrorInfo errorInfo = new ErrorInfo(baseResponse.getCode(), baseResponse.getMsg());
        onResponse(baseResponse.getData(), errorInfo);
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }

    protected abstract void onResponse(T t, ErrorInfo errorInfo);
}
