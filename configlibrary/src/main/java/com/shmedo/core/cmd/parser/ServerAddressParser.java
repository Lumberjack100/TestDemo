package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.ServerAddress;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.ServerAddressInfo;

/**
 * Created by adu on 2017/12/15.
 * 解析获取服务器地址
 */
@Parser
public class ServerAddressParser implements ResultParser<ServerAddressInfo> {
    @Override
    public ServerAddressInfo parse(String result) {
        ServerAddressInfo info = new ServerAddressInfo();
        info.setPort(ServerAddress.valueOf(result.substring(5)));
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
