package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.DataMessageModel;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.DataMessageModelInfo;

/**
 * Created by adu on 2017/12/15.
 * 解析数据通讯模式
 */
@Parser
public class DataMessageModelParser implements ResultParser<DataMessageModelInfo> {
    @Override
    public DataMessageModelInfo parse(String result) {
        DataMessageModelInfo info = new DataMessageModelInfo();
        info.setDataMessageModel(DataMessageModel.valueOf(Integer.valueOf(result.substring(5))));
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
