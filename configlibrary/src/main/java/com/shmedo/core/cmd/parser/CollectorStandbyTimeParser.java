package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.CollectorStandbyTimeInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析采集器待机时长
 */
@Parser
public class CollectorStandbyTimeParser implements ResultParser<CollectorStandbyTimeInfo> {
    @Override
    public CollectorStandbyTimeInfo parse(String result) {
        CollectorStandbyTimeInfo info = new CollectorStandbyTimeInfo();
        info.setType(result.substring(5,7));
        info.setTime(result.substring(7));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.COLLECTOR_STANDBY_TIME;
    }
}
