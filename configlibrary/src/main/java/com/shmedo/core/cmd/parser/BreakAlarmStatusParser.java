package com.shmedo.core.cmd.parser;


import com.shmedo.core.enums.BreakAlarmStatus;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.BreakAlarmStatusInfo;

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
