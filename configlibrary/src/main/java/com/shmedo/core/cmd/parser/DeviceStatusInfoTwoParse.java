package com.shmedo.core.cmd.parser;

import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.DeviceStatusInfoTwo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：   解析设备状态信息
 */
public class DeviceStatusInfoTwoParse implements ResultParser<DeviceStatusInfoTwo> {
    @Override
    public DeviceStatusInfoTwo parse(String result) {
        String[] cmd = result.split(",", -1);
        if (cmd.length < 19) {
            return null;
        }
        DeviceStatusInfoTwo statusTwo = new DeviceStatusInfoTwo();
        statusTwo.setSnNumber(cmd[1]);
        statusTwo.setLongitude(cmd[2]);
        statusTwo.setLatitude(cmd[3]);
        statusTwo.setInternalVoltage(Double.parseDouble(cmd[4]));
        statusTwo.setExternalVoltage(cmd[5]);
        statusTwo.setSolarControllerStatus(cmd[6]);
        statusTwo.setSolarPanelVoltage(cmd[7]);
        statusTwo.setBatteryVoltage(cmd[8]);
        statusTwo.setDailyPowerGeneration(cmd[9]);
        statusTwo.setDailyPowerConsumption(cmd[10]);
        statusTwo.setInternalTempHumidityStatus(cmd[11]);
        statusTwo.setInternalTemperature(cmd[12]);
        statusTwo.setInternalHumidity(cmd[13]);
        statusTwo.setExternalTempHumidityStatus(cmd[14]);
        statusTwo.setExternalTemperature(cmd[15]);
        statusTwo.setExternalHumidity(cmd[16]);
        statusTwo.setSwitchType(cmd[17]);
        statusTwo.setRainfallStatus(cmd[18]);

        return statusTwo;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.QUERY_DAS_STATUS_2;
    }
}
