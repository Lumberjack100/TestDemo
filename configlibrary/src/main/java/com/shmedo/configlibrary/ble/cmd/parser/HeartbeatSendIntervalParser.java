package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.HeartbeatSendIntervalInfo;

/**
 * Created by adu on 2017/12/18.
 * 解析心跳包间隔
 */
@Parser
public class HeartbeatSendIntervalParser implements ResultParser<HeartbeatSendIntervalInfo> {

    @Override
    public HeartbeatSendIntervalInfo parse(String result) {
        HeartbeatSendIntervalInfo info = new HeartbeatSendIntervalInfo();
        String time = result.substring(5);
        info.setTime(time);
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.HEARTBEAT_SEND_INTERVAL;
    }
}
