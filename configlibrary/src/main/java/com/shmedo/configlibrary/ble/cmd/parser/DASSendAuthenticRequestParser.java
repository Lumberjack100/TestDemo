package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;

/**
 * Created by adu on 2018/1/11.
 * 解析DAS发送认证请求
 */
@Parser
public class DASSendAuthenticRequestParser implements ResultParser<String> {
    @Override
    public String parse(String result) {
        String [] strs = result.split(",");
        String sn = strs[1].replace("\r\n","");
        return sn;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.DAS_SEND_AUTHENTICATION_REQUEST;
    }
}
