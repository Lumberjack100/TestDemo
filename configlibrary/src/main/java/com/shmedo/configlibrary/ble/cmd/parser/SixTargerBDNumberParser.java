package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.SixTargerBDNumberInfo;

/**
 * Created by adu on 2017/12/15.
 */
@Parser
public class SixTargerBDNumberParser implements ResultParser<SixTargerBDNumberInfo> {
    @Override
    public SixTargerBDNumberInfo parse(String result) {
        SixTargerBDNumberInfo info = new SixTargerBDNumberInfo();
        info.setSixNumber(result.substring(5));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SIX_TARGER_BD_NUMBER;
    }
}
