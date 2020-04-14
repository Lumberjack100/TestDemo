package com.shmedo.core.cmd.parser;

import com.shmedo.core.enums.CommandType;
import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.BaseConfigInfo;
import com.shmedo.core.annotations.Parser;
import com.shmedo.core.utils.ParserUtils;


/**
 * Created by Liudongdong on 17/12/12.
 * 获取基础配置信息
 */
@Parser
public class BaseConfigParser implements ResultParser<BaseConfigInfo> {
    @Override
    public BaseConfigInfo parse(String result) {
        String[] strs = result.split(",");
        if (strs.length == 20) {
            return ParserUtils.startParserBaseConfig(strs);
        }else {
            throw new DASParameterException("参数错误");
        }
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.BASE_CONFIG;
    }
}
