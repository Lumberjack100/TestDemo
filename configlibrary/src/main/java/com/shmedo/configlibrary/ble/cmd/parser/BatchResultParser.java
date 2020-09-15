package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.Entity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;


/**
 * Created by adu on 2018/1/9.
 * 批处理指令返回结果
 */
@Parser
public class BatchResultParser implements ResultParser<Entity> {


    @Override
    public Entity parse(String result) {
        if (result.endsWith(CommandResult.ERROR_END))
            throw new DASParameterException("批指令出错");

        return Entity.empty();
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.BATCH_BEGIN;
    }
}
