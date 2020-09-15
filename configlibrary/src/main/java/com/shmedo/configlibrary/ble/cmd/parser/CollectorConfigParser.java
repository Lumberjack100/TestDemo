package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.CollectorConfigInfo;
import com.shmedo.configlibrary.ble.utils.ParserUtils;


/**
 * Created by adu on 2017/12/12.
 * 获取XX采集器配置
 */
@Parser
public class CollectorConfigParser implements ResultParser<CollectorConfigInfo> {
    @Override
    public CollectorConfigInfo parse(String result) throws DASParameterException {
        String[] strs = result.split(",");
        if (strs.length == 6) {
            return ParserUtils.startParserCollectorConfig(strs);
        }
        return null;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.COLLECTOR_CONFIG;
    }

}
