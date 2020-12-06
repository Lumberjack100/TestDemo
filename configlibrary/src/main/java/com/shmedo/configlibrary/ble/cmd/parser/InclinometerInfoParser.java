package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.InclinometerInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/6/20 <br/>
 * 描述：     解析倾角计信息
 */
public class InclinometerInfoParser implements ResultParser<InclinometerInfo> {
    @Override
    public InclinometerInfo parse(String result) {
        InclinometerInfo info = new InclinometerInfo();

        try {
            String[] strs = result.split(",");
            info.setStatus(strs[2]);
            info.setxAxis(strs[3]);
            info.setyAxis(strs[4]);
            info.setzAxis(strs[5]);
            return info;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.QUERY_INCLINOMETER_INFO;
    }
}
