package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.DataCommunicateMode;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.DataCommunicateModeInfo;

/**
 * Created by adu on 2017/12/15.
 * 解析数据通讯模式
 */
@Parser
public class DataCommunicateModeParser implements ResultParser<DataCommunicateModeInfo> {
    @Override
    public DataCommunicateModeInfo parse(String result) {
        DataCommunicateModeInfo info = new DataCommunicateModeInfo();
        info.setDataCommunicateMode(DataCommunicateMode.valueOf(Integer.parseInt(result.substring(5))));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.DATA_MASSAGE_MODEL;
    }
}
