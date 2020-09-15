package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.ServerNumber;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.ServerAddressInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析设置服务器(数据中心)地址端口
 */
@Parser
public class ServerAddressInfoParser implements ResultParser<ServerAddressInfo> {
    @Override
    public ServerAddressInfo parse(String result) {
        ServerAddressInfo info = new ServerAddressInfo();
        String [] strs = result.split(" ");
        info.setNumber(ServerNumber.valueOf(Integer.parseInt(strs[0].substring(5))));
        info.setAddress(strs[1]);
        info.setPort(Integer.parseInt(strs[2]));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SERVER_ADDRESS;
    }
}
