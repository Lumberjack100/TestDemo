package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SetGPRSOnlineTimeInfo;

/**
 * Created by adu on 2018/1/4.
 * 解析 设置GPRS持续在线时长
 */
@Parser
public class SetGPRSOnlineTimeParser implements ResultParser<SetGPRSOnlineTimeInfo> {
    @Override
    public SetGPRSOnlineTimeInfo parse(String result) {
        SetGPRSOnlineTimeInfo info = new SetGPRSOnlineTimeInfo();
        info.setTime(Integer.valueOf(result.substring(5)));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SET_GPRS_ONLINE_TIME;
    }
}
