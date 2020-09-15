package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.DeviceStatusInfoThree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：    TODO
 */
public class DeviceStatusInfoThreeParse implements ResultParser<DeviceStatusInfoThree> {
    @Override
    public DeviceStatusInfoThree parse(String result) {
        String[] cmd = result.split(",", -1);

        DeviceStatusInfoThree statusThree = new DeviceStatusInfoThree();
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(cmd).subList(4, cmd.length));
        statusThree.setSnNumber(cmd[1]);
        statusThree.setCollectorModel(cmd[2]);
        statusThree.setCollectorAddress(cmd[3]);
        statusThree.setSensorStatus(list);

        return statusThree;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.QUERY_DAS_STATUS_3;
    }
}
