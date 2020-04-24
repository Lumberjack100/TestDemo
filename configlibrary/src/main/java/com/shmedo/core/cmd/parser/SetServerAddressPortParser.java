package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.ServerAddressNumber;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SetServerAddressPortInfo;

/**
 * Created by adu on 2017/12/19.
 * 解析设置服务器地址端口
 */
@Parser
public class SetServerAddressPortParser implements ResultParser<SetServerAddressPortInfo> {
    @Override
    public SetServerAddressPortInfo parse(String result) {
        SetServerAddressPortInfo info = new SetServerAddressPortInfo();
        String [] strs = result.split(" ");
        info.setNumber(ServerAddressNumber.valueOf(Integer.parseInt(strs[0].substring(5))));
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
