package com.shmedo.lib.device.base.iot_cmd.interfaces;

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：     TODO #gh#
 */
public interface IOTResultParser<T> {
    T parse(String result);

    void validate(String result);

    IOTCommandType commandType();
}
