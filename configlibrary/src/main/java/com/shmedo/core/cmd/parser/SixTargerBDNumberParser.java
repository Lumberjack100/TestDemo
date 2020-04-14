package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SixTargerBDNumberInfo;

/**
 * Created by adu on 2017/12/15.
 */
@Parser
public class SixTargerBDNumberParser implements ResultParser<SixTargerBDNumberInfo> {
    @Override
    public SixTargerBDNumberInfo parse(String result) {
        SixTargerBDNumberInfo info = new SixTargerBDNumberInfo();
        int number = Integer.valueOf(result.substring(5));
        info.setSixNumber(number);
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
