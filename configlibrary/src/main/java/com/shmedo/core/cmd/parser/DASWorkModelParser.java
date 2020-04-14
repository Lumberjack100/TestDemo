package com.shmedo.core.cmd.parser;


import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.DASWorkModel;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.DASWorkModelInfo;

/**
 * Created by adu on 2017/12/18.
 * 解析设置DAS工作模式
 */
@Parser
public class DASWorkModelParser implements ResultParser<DASWorkModelInfo> {
    @Override
    public DASWorkModelInfo parse(String result) {
        result.replace("\r\n","");
        DASWorkModelInfo info = new DASWorkModelInfo();
        info.setDasWorkModel(DASWorkModel.valueOf(Integer.valueOf(result.substring(5))));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.DAS_WORK_MODEL;
    }
}
