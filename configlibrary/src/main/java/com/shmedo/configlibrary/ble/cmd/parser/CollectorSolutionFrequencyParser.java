package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.CollectorSolutionFrequencyInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析采集器解算频度
 */
@Parser
public class CollectorSolutionFrequencyParser implements ResultParser<CollectorSolutionFrequencyInfo> {
    @Override
    public CollectorSolutionFrequencyInfo parse(String result) {
        CollectorSolutionFrequencyInfo info = new CollectorSolutionFrequencyInfo();
        info.setType(result.substring(5,7));
        info.setTimeInterval(result.substring(7));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.COLLECTOR_SOLUTION_FREQUENCY;
    }
}
