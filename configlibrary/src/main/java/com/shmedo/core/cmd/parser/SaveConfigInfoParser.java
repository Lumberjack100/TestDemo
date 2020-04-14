package com.shmedo.core.cmd.parser;

import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SaveConfigInfo;

/**
 * 项目名：  das-config-base
 * 包名：    com.shmedo.das.das.cmd.parser
 * 文件名:   SaveConfigInfoParser
 * 创建者:   dpc
 * 创建时间:  2018/4/18 10:57
 * 描述：    解析保存配置信息
 */

public class SaveConfigInfoParser implements ResultParser<SaveConfigInfo> {

    @Override public SaveConfigInfo parse(String result) {
        SaveConfigInfo info = new SaveConfigInfo();
        info.setInfo(Integer.parseInt(result.substring(5,6)));
        return info;
    }


    @Override public void validate(String result) {

    }


    @Override public CommandType commandType() {
        return CommandType.SAVE_CONFIG_INFO;
    }
}
