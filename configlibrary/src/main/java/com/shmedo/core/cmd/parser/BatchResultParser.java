package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.cmd.Entity;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.ResultParser;

import static com.shmedo.core.cmd.CommandResult.ERROR_END;


/**
 * Created by adu on 2018/1/9.
 * 批处理指令返回结果
 */
@Parser
public class BatchResultParser implements ResultParser<Entity> {


    @Override
    public Entity parse(String result) {
        if (result.endsWith(ERROR_END))
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
