package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.DataReportIntervalInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析设置数据上传间隔
 */
@Parser
public class DataReportIntervalParser implements ResultParser<DataReportIntervalInfo> {
    @Override
    public DataReportIntervalInfo parse(String result) {
        DataReportIntervalInfo info = new DataReportIntervalInfo();
        info.setTime(Integer.valueOf(result.substring(5)));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.DATA_REPORT_INTERVAL;
    }
}
