package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.SetAuthorzePhoneInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析设置授权手机号码
 */
@Parser
public class SetAuthorzePhoneParser implements ResultParser<SetAuthorzePhoneInfo> {
    @Override
    public SetAuthorzePhoneInfo parse(String result) {
        SetAuthorzePhoneInfo info = new SetAuthorzePhoneInfo();
        info.setPhoneNumber(result.substring(5));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SET_AUTHORIZE_PHONE;
    }
}
