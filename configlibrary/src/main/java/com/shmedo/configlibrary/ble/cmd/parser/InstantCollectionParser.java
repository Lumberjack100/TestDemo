package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.cmd.Entity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;

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
