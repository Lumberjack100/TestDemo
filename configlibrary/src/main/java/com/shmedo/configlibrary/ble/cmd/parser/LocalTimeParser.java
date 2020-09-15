package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.LoaclTimeInfo;

/**
 * Created by adu on 2017/12/15.
 * 解析本地时间
 */
@Parser
public class LocalTimeParser implements ResultParser<LoaclTimeInfo> {
    @Override
    public LoaclTimeInfo parse(String result) {
        LoaclTimeInfo info = new LoaclTimeInfo();
        String time = result.substring(5);
        info.setTime(time);
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.LOCAL_TIME;
    }
}
