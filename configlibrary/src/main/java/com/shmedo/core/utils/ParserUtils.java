package com.shmedo.core.utils;


import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.enums.DataCommunicateMode;
import com.shmedo.core.enums.DataEncryption;
import com.shmedo.core.enums.DebugModel;
import com.shmedo.core.enums.EquipmentStatus;
import com.shmedo.core.enums.RainStation;
import com.shmedo.core.enums.SIMChoose;
import com.shmedo.core.enums.SensorInterfaceType;
import com.shmedo.core.enums.SetServerAddressPort;
import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.model.BaseConfigInfo;
import com.shmedo.core.model.CollectorConfigInfo;
import com.shmedo.core.model.SetServerAddressPortInfo;

/**
 * Created by adu on 2017/12/22.
 * 解析数据的工具类
 */
public class ParserUtils
{
    /**
     * 开始进行解析基础配置
     *
     * @param strs 返回的基础配置数据
     * @return 返回基础配置的实体类
     */
    public static BaseConfigInfo startParserBaseConfig(String[] strs) throws DASParameterException
    {
        BaseConfigInfo bean = new BaseConfigInfo();
        bean.setToken(strs[1]);
        if (Integer.parseInt(strs[2]) != 0)
        {
            if (ValidateUtil.isNumberSix(strs[2]))
                bean.setLocalBGNum(strs[2]);
        }
        else
        {
            bean.setLocalBGNum(strs[2]);
        }
        if (ValidateUtil.isNumberSix(strs[3]) || Integer.parseInt(strs[2]) == 0)
        {
            bean.setTargetBGNum(strs[3]);
        }
        else
        {
            throw new DASParameterException("目标北斗卡号异常");
        }
        int equipmentStatus = Integer.parseInt(strs[4]);
        bean.setEquipmentStatus(EquipmentStatus.valueOf(equipmentStatus));

        int dataCommunicateMode = Integer.parseInt(strs[5]);
        bean.setDataCommunicateMode(DataCommunicateMode.valueOf(dataCommunicateMode));

        int rainfallStation = Integer.parseInt(strs[6]);
        bean.setRainStation(RainStation.valueOf(rainfallStation));

        int rainAccuracy = Integer.parseInt(strs[7]);
        bean.setRainAccuracy(rainAccuracy);

        int locationSensitivity = Integer.parseInt(strs[8]);
        bean.setLocationSensitivity(locationSensitivity);

        int locationAccuracy = Integer.parseInt(strs[9]);
        bean.setLocationAccuracy(locationAccuracy);

        int heartbeatTimeInterval = Integer.parseInt(strs[10]);
        bean.setHeartbeatTimeInterval(heartbeatTimeInterval);

        long debugBandRate = Long.parseLong(strs[11]);
        bean.setDebugBandRate(debugBandRate);

        long sensorBandRate = Long.parseLong(strs[12]);
        bean.setSensorBandRate(sensorBandRate);

        String collectorModel = strs[13];
        bean.setCollectorModel(CollectorModel.value(collectorModel));

        int dataReportInterval = Integer.parseInt(strs[14]);
        bean.setDataReportInterval(dataReportInterval);
        bean.setBatteryOverProtect(strs[15]);

        int debugModel = Integer.parseInt(strs[16]);
        bean.setDebugModel(DebugModel.valueOf(debugModel));

        int sensorInterfaceType = Integer.parseInt(strs[17]);
        bean.setSensorInterfaceType(SensorInterfaceType.valueOf(sensorInterfaceType));

        int dataEncryption = Integer.parseInt(strs[18]);
        bean.setDataEncryption(DataEncryption.valueOf(dataEncryption));

        int simChoose = Integer.parseInt(strs[19]);
        bean.setSIMChoose(SIMChoose.valueOf(simChoose));

        return bean;
    }


    /**
     * 开始解析采集器数据
     *
     * @param strs $$100XX, (1), (2), (3), (4), (5) \r\n，
     * @return 返回采集器配置的实体类
     */
    public static CollectorConfigInfo startParserCollectorConfig(String[] strs)
    {
        CollectorConfigInfo bean = new CollectorConfigInfo();
        bean.setCollectorAddress(strs[1]);
        bean.setStandbyTime(strs[2]);
        bean.setWorkTime(strs[3]);
        bean.setCollectorInterval(strs[4]);
        bean.setAccessSum(Integer.parseInt(strs[5]));

        return bean;
    }


    public static SetServerAddressPortInfo startParserServerAddress(String[] strs)
    {
        SetServerAddressPortInfo info = new SetServerAddressPortInfo();
        info.setNumber(SetServerAddressPort.valueOf(Integer.parseInt(strs[0].substring(5))));
        info.setAddress(strs[1]);
        info.setPort(Integer.parseInt(strs[2]));
        return info;
    }


    /**
     * 格式化字符串 将2格式化为02
     *
     * @param sersorValue 传感器的值
     * @return 返回格式化后的字符串
     */
    public static String setSersonValue(String sersorValue)
    {
        int result = Integer.parseInt(sersorValue);
        return String.format("%02d", result);
    }

}
