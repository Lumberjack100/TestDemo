package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.CellProtectionVoltageInfo;

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
