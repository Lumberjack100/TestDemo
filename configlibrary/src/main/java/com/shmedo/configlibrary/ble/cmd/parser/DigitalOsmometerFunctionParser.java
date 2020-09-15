package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.OsmometerStatus;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.DigitalOsmometerFunctionInfo;

/**
 * Created by adu on 2018/1/8.
 * 解析数字式渗压计功能
 */
@Parser
public class DigitalOsmometerFunctionParser implements ResultParser<DigitalOsmometerFunctionInfo> {
    @Override
    public DigitalOsmometerFunctionInfo parse(String result) {
        DigitalOsmometerFunctionInfo info = new DigitalOsmometerFunctionInfo();
        info.setOsmometerStatus(OsmometerStatus.valueOf(Integer.parseInt(result.substring(5))));
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
