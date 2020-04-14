package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.OsmometerStatus;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.DigitalOsmometerFunctionInfo;

/**
 * Created by adu on 2018/1/8.
 * 解析数字式渗压计功能
 */
@Parser
public class DigitalOsmometerFunctionParser implements ResultParser<DigitalOsmometerFunctionInfo> {
    @Override
    public DigitalOsmometerFunctionInfo parse(String result) {
        DigitalOsmometerFunctionInfo info = new DigitalOsmometerFunctionInfo();
        info.setOsmometerStatus(OsmometerStatus.valueOf(Integer.valueOf(result.substring(5))));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.DIGITAL_OSMOMETER_FUNCTION;
    }
}
