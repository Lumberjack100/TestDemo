package com.shmedo.configlibrary.ble.interfaces;


import com.shmedo.configlibrary.ble.enums.CommandType;

/**
 * Created by Liudongdong on 17/12/12.
 */
public interface ResultParser<T> {
    T parse(String result);

    void validate(String result);

    CommandType commandType();
}
