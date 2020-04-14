package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.CellProtectionVoltageInfo;

/**
 * Created by adu on 2017/12/18.
 * 解析电池过放保护电压
 */
@Parser
public class CellProtectionVoltageParser implements ResultParser<CellProtectionVoltageInfo> {

    @Override
    public CellProtectionVoltageInfo parse(String result) {
        CellProtectionVoltageInfo info = new CellProtectionVoltageInfo();
        double voltage = Double.parseDouble(result.substring(5));
        info.setVoltage(voltage);
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.CELL_PROTECTION_VOLTAGE;
    }
}
