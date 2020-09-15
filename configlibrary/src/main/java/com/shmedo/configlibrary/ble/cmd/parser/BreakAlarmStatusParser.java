package com.shmedo.configlibrary.ble.cmd.parser;


import com.shmedo.configlibrary.ble.enums.BreakAlarmStatus;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.BreakAlarmStatusInfo;

public class BreakAlarmStatusParser implements ResultParser<BreakAlarmStatusInfo> {
    @Override
    public BreakAlarmStatusInfo parse(String result) {
        BreakAlarmStatusInfo info = new BreakAlarmStatusInfo();
        info.setStatus(BreakAlarmStatus.valueOf(Integer.valueOf(result.substring(5))));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.BREAK_ALARM_STATUS;
    }
}
