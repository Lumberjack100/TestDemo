package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SetCordLengthInfo;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.das.cmd.parser
 * 文件名:   SetCordLenghtParser
 * 创建者:   dpc
 * 创建时间:  2018/4/14 15:13
 * 描述：    解析绳长
 */

@Parser
public class SetCordLenghtParser implements ResultParser<SetCordLengthInfo> {
    @Override public SetCordLengthInfo parse(String result) {
        SetCordLengthInfo info = new SetCordLengthInfo();
        String lenght = result.substring(5);
        info.setCordLenght(Double.valueOf(lenght));
        return info;
    }


    @Override public void validate(String result) {

    }


    @Override public CommandType commandType() {
        return CommandType.SET_CORD_LENGHT;
    }
}
