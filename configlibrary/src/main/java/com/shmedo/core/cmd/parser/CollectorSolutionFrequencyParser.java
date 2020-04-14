package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.CollectorSolutionFrequencyInfo;

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
