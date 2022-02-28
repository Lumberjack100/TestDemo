package com.shmedo.configlibrary.ble.utils;


import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.DataCommunicateMode;
import com.shmedo.configlibrary.ble.enums.DataEncryption;
import com.shmedo.configlibrary.ble.enums.EquipmentStatus;
import com.shmedo.configlibrary.ble.enums.RainStation;
import com.shmedo.configlibrary.ble.enums.SIMChoose;
import com.shmedo.configlibrary.ble.enums.SensorInterfaceType;
import com.shmedo.configlibrary.ble.enums.WorkModel;
import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.model.CollectorConfigInfo;

/**
 * Created by adu on 2017/12/22.
 * 解析数据的工具类
 */
public class ParserUtils {
    /**
     * 开始进行解析基础配置
     *
     * @param strs 返回的基础配置数据
     * @return 返回基础配置的实体类
     */
    public static BaseConfigInfo startParserBaseConfig(String[] strs) throws DASParameterException {
        BaseConfigInfo bean = new BaseConfigInfo();
        bean.setToken(strs[1]);
        if (Integer.parseInt(strs[2]) != 0) {
            if (ValidateUtil.isNumberSix(strs[2]))
                bean.setLocalBGNum(strs[2]);
        } else {
            bean.setLocalBGNum(strs[2]);
        }
        if (ValidateUtil.isNumberSix(strs[3]) || Integer.parseInt(strs[2]) == 0) {
            bean.setTargetBGNum(strs[3]);
        } else {
            throw new DASParameterException("目标北斗卡号异常");
        }
        int equipmentStatus = Integer.parseInt(strs[4]);
        bean.setEquipmentStatus(EquipmentStatus.value(equipmentStatus));

        int dataCommunicateMode = Integer.parseInt(strs[5]);
        bean.setDataCommunicateMode(DataCommunicateMode.value(dataCommunicateMode));

        int rainfallStation = Integer.parseInt(strs[6]);
        bean.setRainStation(RainStation.value(rainfallStation));

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
        bean.setWorkModel(WorkModel.value(debugModel));

        int sensorInterfaceType = Integer.parseInt(strs[17]);
        bean.setSensorInterfaceType(SensorInterfaceType.value(sensorInterfaceType));

        int dataEncryption = Integer.parseInt(strs[18]);
        bean.setDataEncryption(DataEncryption.value(dataEncryption));

        int simChoose = Integer.parseInt(strs[19]);
        bean.setSIMChoose(SIMChoose.value(simChoose));

        return bean;
    }


    /**
     * 开始解析采集器数据
     *
     * @param strs $$100XX, (1), (2), (3), (4), (5) \r\n，
     * @return 返回采集器配置的实体类
     */
    public static CollectorConfigInfo startParserCollectorConfig(String[] strs) {
        CollectorConfigInfo bean = new CollectorConfigInfo();
        bean.setCollectorAddress(strs[1]);
        bean.setStandbyTime(strs[2]);
        bean.setWorkTime(strs[3]);
        bean.setCollectorInterval(strs[4]);
        bean.setAccessSum(Integer.parseInt(strs[5]));
        bean.setSensitivity(strs.length >= 7 ? strs[6] : "NullKey");
        return bean;
    }
}
