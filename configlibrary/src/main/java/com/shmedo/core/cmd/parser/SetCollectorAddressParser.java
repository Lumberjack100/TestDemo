package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SetCollectorAddressInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析采集器地址
 */
@Parser
public class SetCollectorAddressParser implements ResultParser<SetCollectorAddressInfo> {
    @Override
    public SetCollectorAddressInfo parse(String result) {
        SetCollectorAddressInfo info = new SetCollectorAddressInfo();
        info.setAddress(Integer.valueOf(result.substring(5)));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SET_COLLECTOR_ADDRESS;
    }
}
