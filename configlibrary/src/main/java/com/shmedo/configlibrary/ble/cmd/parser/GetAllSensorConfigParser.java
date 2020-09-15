package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.model.CollectorConfigInfo;
import com.shmedo.configlibrary.ble.model.CollectorSensorParamsInfo;
import com.shmedo.configlibrary.ble.model.SensorSoilMoistureInfo;
import com.shmedo.configlibrary.ble.model.ServerAddressInfo;
import com.shmedo.configlibrary.ble.utils.ParserUtils;
import com.shmedo.configlibrary.ble.model.GetAllSensorConfigInfo;
import com.shmedo.configlibrary.ble.model.SensorInfrasoundInfo;
import com.shmedo.configlibrary.ble.model.SensorRadarLevelInfo;
import com.shmedo.configlibrary.ble.model.SensorWireShiftInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adu on 2017/12/22.
 * 解析所有的传感器配置
 */
@Parser
public class GetAllSensorConfigParser implements ResultParser<GetAllSensorConfigInfo> {
    @Override
    public GetAllSensorConfigInfo parse(String result) {
        GetAllSensorConfigInfo info = startParser(result);
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.GET_ALL_SENSOR_CONFIG;
    }

    /**
     * 开始解析获取所有配置
     * @param result
     * * $$333@@
     * $$000,170900-L,0,455872,2,1,2,50,0,100,0,9600,9600,02,240,6.5,3,1,02@@   //获取基础配置信息
     * $$2001 sinzmc.gnway.cc 7076@@
     * $$2002 sinzmc.gnway.cc 7076@@
     * $$10002,0,600,15,500,8@@     //获取XX采集器配置
     * $$1010200,1,2,10,0.000000@@  //下面是获取所有采集器通道的传感器参数
     * $$1010201,2,2,10,0.000000@@
     * $$1010202,3,2,10,0.000000@@
     * $$1010203,4,2,10,0.000000@@
     * $$1010204,5,2,10,0.000000@@
     * $$1010205,6,2,10,0.000000@@
     * $$1010206,7,2,10,0.000000@@
     * $$1010207,8,2,10,0.000000
     *
     */
    private GetAllSensorConfigInfo startParser(String result) {
        GetAllSensorConfigInfo getAllSensorConfigInfo = new GetAllSensorConfigInfo();

        String[] strs = result.split("@@");
        String[] baseConfig = strs[1].split(",");
        String[] serverAddress1 = strs[2].split(" ");
        String[] serverAddress2 = strs[3].split(" ");
        String[] collectorConfig = strs[4].split(",");

        BaseConfigInfo baseConfigInfo = ParserUtils.startParserBaseConfig(baseConfig);
        getAllSensorConfigInfo.setBaseConfig(baseConfigInfo);
        CollectorConfigInfo collectorConfigInfo = ParserUtils.startParserCollectorConfig(collectorConfig);
        getAllSensorConfigInfo.setCollectorConfig(collectorConfigInfo);

        ServerAddressInfo serverAddressPortInfo1 = ParserUtils.startParserServerAddress(serverAddress1);
        getAllSensorConfigInfo.setServerAddressPortInfo1(serverAddressPortInfo1);
        ServerAddressInfo serverAddressPortInfo2 = ParserUtils.startParserServerAddress(serverAddress2);
        getAllSensorConfigInfo.setServerAddressPortInfo2(serverAddressPortInfo2);

        String[] cs = new String[strs.length-5];
        List<String[]> list=new ArrayList<>();
        for (int i = 0;i < cs.length;i++){
            list.add(strs[5+i].split(","));
        }
        String collectorType = collectorConfig[0].substring(5);
        List<CollectorSensorParamsInfo> collectorSensorParamsInfos = null;
        switch (collectorType){
            case "02":
                 collectorSensorParamsInfos = new ArrayList<>();
                for (int i = 0; i < list.size();i++) {
                    CollectorSensorParamsInfo<SensorWireShiftInfo> csp = CollectorSensorParamsParser.parserWireShift(list.get(i));
                    collectorSensorParamsInfos.add(csp);
                    getAllSensorConfigInfo.setCollectorSensorParamsInfos(collectorSensorParamsInfos);
                }
                break;

            case "03":
                 collectorSensorParamsInfos = new ArrayList<>();
                for (int i = 0; i < list.size();i++) {
                    CollectorSensorParamsInfo<SensorSoilMoistureInfo> csp = CollectorSensorParamsParser.parserSoilMoisture(list.get(i));
                    collectorSensorParamsInfos.add(csp);
                    getAllSensorConfigInfo.setCollectorSensorParamsInfos(collectorSensorParamsInfos);
                }
                break;

            case "07":
                collectorSensorParamsInfos = new ArrayList<>();
                for (int i = 0; i < list.size();i++) {
                    CollectorSensorParamsInfo<SensorRadarLevelInfo> csp = CollectorSensorParamsParser.parserRadarLevel(list.get(i));
                    collectorSensorParamsInfos.add(csp);
                    getAllSensorConfigInfo.setCollectorSensorParamsInfos(collectorSensorParamsInfos);
                }
                break;

            case "21":
                collectorSensorParamsInfos = new ArrayList<>();
                for (int i = 0; i < list.size();i++) {
                    CollectorSensorParamsInfo<SensorInfrasoundInfo> csp = CollectorSensorParamsParser.parserInfrasound(list.get(i));
                    collectorSensorParamsInfos.add(csp);
                    getAllSensorConfigInfo.setCollectorSensorParamsInfos(collectorSensorParamsInfos);
                }
                break;
        }

        return getAllSensorConfigInfo;

    }

}
