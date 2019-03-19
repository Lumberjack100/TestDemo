package com.shmedo.mcloudapp;

import com.shmedo.das.common.BaseConfigInfo;
import com.shmedo.das.das.cmd.CommandResult;
import com.shmedo.das.das.cmd.parser.ParseManager;
import com.shmedo.mcloudapp.entity.ble.BaseConfigInfoSub;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    //@Test
    //public void addition_isCorrect() {
    //    assertEquals(4, 2 + 2);
    //}

    @Test
    public void parse() {
        String data  = "$$000,18A095-L,0,455872,2,1,1,5400,10,100,0,9600,9600,02,3,6,3,1,2,1\r\n";
        //String data  = "$$000,18A095-L,0,455872,2,1,1,5400,10,100,0,9600,9600,02,3,6,3,1,2,1\r\n";
        BaseConfigInfoSub mInfoSub = BlueResultParserUtil.getBaseConfig(data);

        CommandResult<BaseConfigInfo> baseBean = ParseManager.getInstance().parse(data);
        BaseConfigInfo info = null;
        if (baseBean.isSuccess()) {
            info = baseBean.getResult();
        }

    }
}