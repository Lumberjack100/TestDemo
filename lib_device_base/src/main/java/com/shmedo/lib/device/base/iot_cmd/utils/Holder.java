package com.shmedo.lib.device.base.iot_cmd.utils;

/**
 * Created by Liudongdong on 17/12/12.
 */
public class Holder<T> {
    private T data;

    public Holder() {
    }

    public Holder(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
