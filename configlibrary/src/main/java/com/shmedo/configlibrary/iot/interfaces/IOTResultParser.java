package com.shmedo.configlibrary.iot.interfaces;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：     TODO
 */
public interface IOTResultParser<T> {
    T parse(String result);

    void validate(String result);

    IOTCommandType commandType();
}
