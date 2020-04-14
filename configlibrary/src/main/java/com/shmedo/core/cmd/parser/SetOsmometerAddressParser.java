package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SetOsmometerAddressInfo;

/**
 * Created by adu on 2018/1/8.
 * 解析渗压计地址
 */
@Parser
public class SetOsmometerAddressParser implements ResultParser<SetOsmometerAddressInfo> {
    @Override
    public SetOsmometerAddressInfo parse(String result) {
        SetOsmometerAddressInfo info = new SetOsmometerAddressInfo();
        info.setAddress(Integer.valueOf(result.substring(5)));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SET_OSMOMETER_ADDRESS;
    }
}
