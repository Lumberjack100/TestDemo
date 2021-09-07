package com.shmedo.mcloudapp.deviceconfig.callback;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/9/7 <br/>
 * 描述：     TODO
 */
public abstract class XyHandler<T> extends Handler {

    private WeakReference<T> mWeak;

    public XyHandler(T t) {
        mWeak = new WeakReference<>(t);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mWeak == null || mWeak.get() == null) {
            return;
        }
        handleMessage(msg, mWeak.get());
        super.handleMessage(msg);
    }

    protected abstract void handleMessage(Message msg, T t);

}

