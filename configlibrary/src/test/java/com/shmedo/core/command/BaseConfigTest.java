package com.shmedo.core.command;


import com.shmedo.configlibrary.ble.utils.ValidateUtil;

import org.junit.Test;

/**
 * Created by adu on 2018/1/15.
 */
public class BaseConfigTest {

    @Test
    public void getBaseConfig(){
//        String info = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG,null);
//        System.out.println(info);
//        assert info.equals("##000\r\n");

        boolean result = ValidateUtil.isNumberSix("123450");
        String ss = "";
    }
}
