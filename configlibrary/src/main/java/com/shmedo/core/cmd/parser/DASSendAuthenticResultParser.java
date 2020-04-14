package com.shmedo.core.cmd.parser;

import com.shmedo.core.enums.CommandType;
import com.shmedo.core.annotations.Parser;
import com.shmedo.core.interfaces.ResultParser;

/**
 * Created by adu on 2018/1/11.
 * 解析DAS发送认证结果
 */
@Parser
public class DASSendAuthenticResultParser implements ResultParser<Boolean> {
    @Override
    public Boolean parse(String result) {
        String [] strs = result.split(",");
        String authenticResult = strs[1].replace("\r\n","");
        if (authenticResult.equals("0")) {
            return false;
        } else if(authenticResult.equals("1")) {
            return true;
        }
        return null;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.DAS_SEND_AUTHENTICATION_RESULT;
    }
}
