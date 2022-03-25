package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.InclinometerInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/6/20 <br/>
 * 描述：     解析倾角计信息 X、Y、Z 轴角度、加速度
 */
public class InclinometerInfoParser implements ResultParser<InclinometerInfo> {
    @Override
    public InclinometerInfo parse(String result) {
        InclinometerInfo info = new InclinometerInfo();
        try {
            String[] values = result.split(",");
            info.setStatus(values[2]);
            info.setxAxis(values[3]);
            info.setyAxis(values[4]);
            info.setzAxis(values[5]);
            if (values.length >= 9) {
                info.setxAcceleration(values[6]);
                info.setyAcceleration(values[7]);
                info.setzAcceleration(values[8]);
            }
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
