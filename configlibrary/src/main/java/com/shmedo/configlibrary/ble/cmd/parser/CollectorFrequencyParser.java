package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.CollectorFrequencyInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析采集器采集频度
 */
@Parser
public class CollectorFrequencyParser implements ResultParser<CollectorFrequencyInfo> {
    @Override
    public CollectorFrequencyInfo parse(String result) {
        CollectorFrequencyInfo info = new CollectorFrequencyInfo();
        info.setType(result.substring(5,7));
        info.setTimeInterval(result.substring(7));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.COLLECTOR_FREQUENCY;
    }
}
