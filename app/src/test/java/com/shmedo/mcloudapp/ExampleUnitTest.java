package com.shmedo.mcloudapp;


import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.cmd.parser.ParseManager;
import com.shmedo.core.model.BaseConfigInfo;
import com.shmedo.mcloudapp.entity.ble.BaseConfigInfoSub;
import com.shmedo.mcloudapp.entity.ble.DigitalOsmometerFunctionSub;
import com.shmedo.mcloudapp.entity.ble.QueryOsmometerParameterSubInfo;
import com.shmedo.mcloudapp.entity.ble.SettingRainPrecisionSub;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;

import org.junit.Test;

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


    public void parse() {
        StringUtil.setSize(646456);
        String data  = "$$000,18A095-L,0,455872,2,1,1,5400,10,100,0,9600,9600,02,3,6,3,1,2,1\r\n";
        //String data  = "$$000,18A095-L,0,455872,2,1,1,5400,10,100,0,9600,9600,02,3,6,3,1,2,1\r\n";
        BaseConfigInfoSub mInfoSub = BlueResultParserUtil.getBaseConfig(data);

        CommandResult<BaseConfigInfo> baseBean = ParseManager.getInstance().parse(data);
        BaseConfigInfo info = null;
        if (baseBean.isSuccess()) {
            info = baseBean.getResult();
        }
        String shenya = "$$400,2,1,100,0.000000,5,0.000000,0.000000\r\n";
        QueryOsmometerParameterSubInfo mFuncSubInfo = BlueResultParserUtil.getQueryOsmometerParameter(shenya);
        System.out.println(mFuncSubInfo.toString());

       SettingRainPrecisionSub rainStationSub =  BlueResultParserUtil.getRainPrecisionInfo("$$12120");
        System.out.println(rainStationSub.toString());

        DigitalOsmometerFunctionSub digitalOsmometerFunctionSub = BlueResultParserUtil.getOsmoeterFunctionInfo("$$4011\r\n");
        System.out.println(digitalOsmometerFunctionSub.toString());

        String str ="$$1010300,2,2,10,0.000000\r\n";
        CollectorSensorParamsInfoSub mCollectorParamsInfoSub=BlueResultParserUtil.setCollectorParams(str);
        System.out.println("---000---"+mCollectorParamsInfoSub.toString());

        //for (int i = 0; i < 5; i++) {
        //        //##101XXYY\r\n：获取XX采集器YY通道的传感器参数
        //    String count  = com.shmedo.mcloudapp.util.StringUtil.formatTwo(i);
        //    System.out.println("================"+count);
        //}

         boolean stay1,stay2,stay3,stay4,stay5,stay6,stay7,stay8 =false;

        boolean[] switchs = new boolean[8];
        for (int j = 0; j < 2; j++) {
            switchs[j]= true;
        }

        for (int j = 0; j < 1; j++) {
            switchs[j]= true;
            if (switchs[j]){
                openSwitch(j);
            }
        }
    }

    private void openSwitch(int j) {
        switch (j){
            case 0:
                System.out.println("0000");
                break;
            case 1:
                System.out.println("1111");
                break;
        }
    }

    @Test
    public void testRain(){

        String command = "##9161,123.12412,30.123\r\n";

            String cmd = command.replace("\r\n", "").substring(7);
            System.out.println(cmd);



//        DeviceInternetStatus internetStatus = ParserDeviceDetailsUtils.parserInternetStatus(message1);
//        System.out.println(internetStatus.toString());

    }

}