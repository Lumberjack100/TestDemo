package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.cmd.Entity;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;

/**
 * Created by adu on 2017/12/15.
 * 即时采集解析
 */
@Parser
public class InstantCollectionParser implements ResultParser<Entity> {

    @Override
    public Entity parse(String result) {
        return Entity.empty();
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.INSTANT_COLLEACTOR;
    }
}
