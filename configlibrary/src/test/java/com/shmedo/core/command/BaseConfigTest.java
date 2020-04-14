package com.shmedo.core.command;


import com.shmedo.core.cmd.CommandManager;
import com.shmedo.core.enums.CommandType;

import org.junit.Test;

/**
 * Created by adu on 2018/1/15.
 */
public class BaseConfigTest {

    @Test
    public void getBaseConfig(){
        String info = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG,null);
        System.out.println(info);
        assert info.equals("##000\r\n");
    }
}
