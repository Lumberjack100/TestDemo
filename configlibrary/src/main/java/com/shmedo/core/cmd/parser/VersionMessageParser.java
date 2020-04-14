package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.VersionMessageInfo;

/**
 * Created by adu on 2017/12/14.
 * 解析版本信息
 */
@Parser
public class VersionMessageParser implements ResultParser<VersionMessageInfo> {

    @Override
    public VersionMessageInfo parse(String result) {
        String[] strs = result.split(",");
        if (strs.length == 4) {
            return startParser(strs);
        } else {
            throw new DASParameterException("参数错误");
        }
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.VERSION_MESSAGE;
    }

    /**
     * 版本解析
     * @param strs
     * @return
     */
    private VersionMessageInfo startParser(String[] strs) {
        VersionMessageInfo info = new VersionMessageInfo();
        info.setProductID(strs[1]);
        info.setFirmwareVersion(strs[2]);
        info.setProduceDate(strs[3]);
        return info;
    }
}
