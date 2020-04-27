package com.shmedo.core.model;

import java.util.List;

/**
 * Created by adu on 2017/12/22.
 * 获取所有传感器配置
 *
 * $$333@@
 * $$000,170900-L,0,455872,2,1,2,50,0,100,0,9600,9600,02,240,6.5,3,1,02@@   //获取基础配置信息
 * $$10002,0,600,15,500,8@@     //获取XX采集器配置
 * $$1010200,1,2,10,0.000000@@  //下面是获取所有采集器通道的传感器参数
 * $$1010201,2,2,10,0.000000@@
 * $$1010202,3,2,10,0.000000@@
 * $$1010203,4,2,10,0.000000@@
 * $$1010204,5,2,10,0.000000@@
 * $$1010205,6,2,10,0.000000@@
 * $$1010206,7,2,10,0.000000@@
 * $$1010207,8,2,10,0.000000@@
 * $$2001,mcloud.shmedo.cn,9001@@
 * $$2002,172.168.5.76,12306
 *
 * $$333@@
 * $$000,180400-L,0,455872,2,1,2,5000,10,100,0,9600,9600,02,5,6,3,1,2,2@@
 * $$10002,1,60,15,500,1@@
 * $$1010200,1,2,10,0.000000@@
 * $$2001 sinzmc.gnway.cc 7076@@
 * $$2002 sinzmc.gnway.cc 7076
 */
public class GetAllSensorConfigInfo {
    private BaseConfigInfo baseConfig;
    private CollectorConfigInfo collectorConfig;
    private List<CollectorSensorParamsInfo> collectorSensorParamsInfos;
    private ServerAddressInfo serverAddressPortInfo1;
    private ServerAddressInfo serverAddressPortInfo2;

    public BaseConfigInfo getBaseConfig() {
        return baseConfig;
    }

    public void setBaseConfig(BaseConfigInfo baseConfig) {
        this.baseConfig = baseConfig;
    }

    public CollectorConfigInfo getCollectorConfig() {
        return collectorConfig;
    }

    public void setCollectorConfig(CollectorConfigInfo collectorConfig) {
        this.collectorConfig = collectorConfig;
    }

    public List<CollectorSensorParamsInfo> getCollectorSensorParamsInfos() {
        return collectorSensorParamsInfos;
    }

    public void setCollectorSensorParamsInfos(List<CollectorSensorParamsInfo> collectorSensorParamsInfos) {
        this.collectorSensorParamsInfos = collectorSensorParamsInfos;
    }


    public ServerAddressInfo getServerAddressPortInfo1() {
        return serverAddressPortInfo1;
    }


    public void setServerAddressPortInfo1(ServerAddressInfo serverAddressPortInfo1) {
        this.serverAddressPortInfo1 = serverAddressPortInfo1;
    }


    public ServerAddressInfo getServerAddressPortInfo2() {
        return serverAddressPortInfo2;
    }


    public void setServerAddressPortInfo2(ServerAddressInfo serverAddressPortInfo2) {
        this.serverAddressPortInfo2 = serverAddressPortInfo2;
    }


    @Override public String toString() {
        return "GetAllSensorConfigInfo{" +
            "baseConfig=" + baseConfig +
            ", collectorConfig=" + collectorConfig +
            ", collectorSensorParamsInfos=" + collectorSensorParamsInfos +
            ", serverAddressPortInfo1=" + serverAddressPortInfo1 +
            ", serverAddressPortInfo2=" + serverAddressPortInfo2 +
            '}';
    }
}
