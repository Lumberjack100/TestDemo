package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.CollectorConfigInfo;
import com.shmedo.core.utils.ParserUtils;


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
