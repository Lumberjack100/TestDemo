package com.shmedo.configlibrary.ble.cmd.parser;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.CollectorSensorParamsInfo;
import com.shmedo.configlibrary.ble.model.SensorGudanPercolateInfo;
import com.shmedo.configlibrary.ble.model.SensorGudanSoilPressureInfo;
import com.shmedo.configlibrary.ble.model.SensorGudanStressInfo;
import com.shmedo.configlibrary.ble.model.SensorInclinometerInfo;
import com.shmedo.configlibrary.ble.model.SensorJunXingZljInfo;
import com.shmedo.configlibrary.ble.model.SensorMoistureMeterInfo;
import com.shmedo.configlibrary.ble.model.SensorSoilMoistureInfo;
import com.shmedo.configlibrary.ble.model.SensorTemperHumidityInfo;
import com.shmedo.configlibrary.ble.model.SensorUltrasonicLevelInfo;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.configlibrary.ble.model.SensorGudanDisplacementInfo;
import com.shmedo.configlibrary.ble.model.SensorGudanNotStressInfo;
import com.shmedo.configlibrary.ble.model.SensorInfrasoundInfo;
import com.shmedo.configlibrary.ble.model.SensorKangPercolateInfo;
import com.shmedo.configlibrary.ble.model.SensorRadarLevelInfo;
import com.shmedo.configlibrary.ble.model.SensorUpliftPressureInfo;
import com.shmedo.configlibrary.ble.model.SensorWireShiftInfo;


/**
 * Created by adu on 2017/12/14.
 * 解析 获取XX采集器YY通道的传感器参数
 */
@Parser
public class CollectorSensorParamsParser implements ResultParser<CollectorSensorParamsInfo> {

    @Override
    public CollectorSensorParamsInfo parse(String result) {
        String[] strs = result.split(",");
        String collectorType = StringUtil.formatStringTwo(strs[2]);
        switch (collectorType) {
            case "02"://拉线位移计 MPS-M-2000
                return parserWireShift(strs);
            case "03"://土壤含水率 TR-3000
                return parserSoilMoisture(strs);
            case "04"://测斜仪 I-P-I
                return parserInclinometer(strs);
            case "06"://超声波物位计 HBRD908
                return parserUltrasonicLevel(strs);
            case "07"://雷达物位计 MH-A15R
                return parserRadarLevel(strs);
            case "08"://墒情计 EP100G
                return parserMoistureMeter(strs);
            case "12"://温湿度计 CSW18
                return parserTemperHumidity(strs);
            case "15"://扬压力计 VWP-G
                return parserUpliftPressure(strs);
            case "21"://次声传感器
                return parserInfrasound(strs);
            case "50"://基康渗压计 BGK-4500
                return parserKangPercolate(strs);
            case "51"://葛南渗压计 VWP-03
                return parserGudanPercolate(strs);
            case "52"://葛南土压力盒 VWE-0.6
                return parserGudanSoilPressure(strs);
            case "53":// 葛南应力计 VWS-15
                return parserGudanStress(strs);
            case "54"://葛南无应力计 VWS-15M
                return parserGudanNotStress(strs);
            case "55"://葛南位移计 VWD-100
                return parserGudanDisplacement(strs);
            case "58":
                return parserJunXingZlj(strs);
            default:
                return null;
        }
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER;
    }


    /**
     * 解析拉线位移计 2个参数   02
     *
     * @param strs 返回裂缝计数据
     * @return 返回具体的传感器拉线位移计实体类
     */
    public static CollectorSensorParamsInfo<SensorWireShiftInfo> parserWireShift(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorWireShiftInfo info = new SensorWireShiftInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.WIRE_SHIFT);
        info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
        info.setCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        bean.setSensorData(info);
        return bean;
    }


    /**
     * 解析土壤含水率  03
     *
     * @param strs
     * @return
     */
    public static CollectorSensorParamsInfo<SensorSoilMoistureInfo> parserSoilMoisture(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorSoilMoistureInfo info = new SensorSoilMoistureInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.SOIL_MOISTURE);
        info.setTriggerThreshold(strs[3]);
        info.setCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        bean.setSensorData(info);
        return bean;
    }

    /**
     * 解析测斜仪  3个参数  04
     *
     * @param strs
     * @return
     */
    private CollectorSensorParamsInfo<SensorInclinometerInfo> parserInclinometer(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorInclinometerInfo info = new SensorInclinometerInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.INCLINOMETER);
        info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
        info.setMeasureLength((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        info.setCorrectionValue((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
        bean.setSensorData(info);
        return bean;
    }


    /**
     * 解析超声波物位计  06
     *
     * @param strs
     * @return
     */
    private CollectorSensorParamsInfo<SensorUltrasonicLevelInfo> parserUltrasonicLevel(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorUltrasonicLevelInfo info = new SensorUltrasonicLevelInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.ULTRASONIC_LEVEL_GAUGE);
        info.setTriggerThreshold(strs[3]);
        info.setCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        if (strs.length == 6)
            info.setProbeElevation(strs[5]);

        bean.setSensorData(info);

        return bean;
    }

    /**
     * 解析雷达物位计  07
     *
     * @param strs
     * @return
     */
    public static CollectorSensorParamsInfo<SensorRadarLevelInfo> parserRadarLevel(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorRadarLevelInfo info = new SensorRadarLevelInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.RADAR_LEVEL_GAUGE);
        info.setTriggerThreshold(strs[3]);
        info.setCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        bean.setSensorData(info);
        return bean;
    }

    /**
     * 解析墒情计    6个参数   08
     *
     * @param strs
     * @return
     */
    private CollectorSensorParamsInfo<SensorMoistureMeterInfo> parserMoistureMeter(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorMoistureMeterInfo info = new SensorMoistureMeterInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.MOISTURE_METER);
        info.setHumidityTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
        info.setHumidityCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        info.setSaltTriggerThreshold((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
        info.setSaltCorrectionValue((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
        info.setTemperatureTriggerThreshold((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
        info.setTemperatureCorrectionValue((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
        bean.setSensorData(info);
        return bean;
    }


    /**
     * 解析温湿度计  12
     *
     * @param strs
     * @return
     */
    private CollectorSensorParamsInfo<SensorTemperHumidityInfo> parserTemperHumidity(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorTemperHumidityInfo info = new SensorTemperHumidityInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.TEMPERATURE_HUMIDITY_METER);
        info.setTemperatureTriggerThreshold(strs[3]);
        info.setHumidityTriggerThreshold(strs[4]);
        bean.setSensorData(info);
        return bean;
    }


    /**
     * 解析扬压力计 15
     *
     * @param strs
     * @return
     */
    private CollectorSensorParamsInfo<SensorUpliftPressureInfo> parserUpliftPressure(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorUpliftPressureInfo info = new SensorUpliftPressureInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.UPLIFT_PRESSURE_GAUGE);
        info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
        info.setCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        info.setCordlength(strs[5]);
        info.setInstallationElevation(strs[6]);
        bean.setSensorData(info);
        return bean;
    }

    /**
     * 次声传感器  17
     *
     * @param strs
     * @return
     */
    public static CollectorSensorParamsInfo<SensorInfrasoundInfo> parserInfrasound(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorInfrasoundInfo info = new SensorInfrasoundInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.INFRASOUND_SENSOR);
        info.setTriggerThreshold(strs[3]);
        info.setCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        bean.setSensorData(info);
        return bean;
    }

    /**
     * 解析基康渗压计 50
     * $$1010003,03,50,2.000000e+00,4.597945e-08,3.000000e+00,4.000000e+00,5.000000e+00,3.800000e+01,3.300000e+01,2.300000e+01,6.600000e+01
     * $$1010003,传感器地址/通道号,传感器类型,触发阀值,A,B,C,K,初始温度T0,手动纠偏,绳长,安装高程
     *
     * @param strs
     * @return
     */
    private CollectorSensorParamsInfo<SensorKangPercolateInfo> parserKangPercolate(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.KANG_PERCOLATE);
        SensorKangPercolateInfo sensorInfo = new SensorKangPercolateInfo();
        sensorInfo.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
        sensorInfo.setPolynomialRatioA(strs[4]);
        sensorInfo.setPolynomialRatioB(strs[5]);
        sensorInfo.setPolynomialRatioC(strs[6]);
        sensorInfo.setTemperatureCoefficientK(strs[7]);
        sensorInfo.setCreateTemperature(strs[8]);
        sensorInfo.setManualCorrection(strs[9]);
        sensorInfo.setCordLenght(strs[10]);
        sensorInfo.setInstallElevation(strs[11]);
        bean.setSensorData(sensorInfo);
        return bean;
    }

    /**
     * 解析葛南渗压计
     * $$1010002,02,51,2.000000e+00,4.597945e-08,3.000000e+00,4.400000e+01,3.800000e+01,1.300000e+01,5.000000e+00,1.600000e+01
     * $$1010002,传感器地址/通道号,传感器类型,触发阀值,灵敏度K,温修系数b,基准值F0,初始温度T0,手动纠偏,绳长,安装高程
     *
     * @param strs
     * @return
     */
    private CollectorSensorParamsInfo<SensorGudanPercolateInfo> parserGudanPercolate(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.GUDAN_PERCOLATE);
        SensorGudanPercolateInfo sensorInfo = new SensorGudanPercolateInfo();
        sensorInfo.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
        sensorInfo.setSensitivityK(strs[4]);
        sensorInfo.setTemperatureCoefficientB(strs[5]);
        sensorInfo.setReferenceValue(strs[6]);
        sensorInfo.setCreateTemperature(strs[7]);
        sensorInfo.setManualCorrection(strs[8]);
        sensorInfo.setCordLenght(strs[9]);
        sensorInfo.setInstallElevation(strs[10]);
        bean.setSensorData(sensorInfo);
        return bean;
    }


    /**
     * 解析葛南土压力盒 52
     *
     * @param strs
     * @return
     */
    private CollectorSensorParamsInfo<SensorGudanSoilPressureInfo> parserGudanSoilPressure(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorGudanSoilPressureInfo info = new SensorGudanSoilPressureInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.GUDAN_SOIL_PRESSURE);
        info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
        info.setSensitivityK((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        info.setTemperatureCoefficientB((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
        info.setDatumValueF0((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
        info.setCreateTemperature((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
        info.setManualCorrection(strs[7]);
        bean.setSensorData(info);
        return bean;
    }

    /**
     * 解析 葛南应力计 7个参数  53
     *
     * @param strs
     * @return
     */
    private CollectorSensorParamsInfo<SensorGudanStressInfo> parserGudanStress(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorGudanStressInfo info = new SensorGudanStressInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.GUDAN_STRESS);
        info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
        info.setSensitivityK((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        info.setTemperatureCoefficientB((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
        info.setExpansionCoefficient((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
        info.setDatumValueF0((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
        info.setCreateTemperature((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
        info.setManualCorrection(strs[8]);
        bean.setSensorData(info);
        return bean;
    }


    /**
     * 解析 葛南无应力计   54
     *
     * @param strs
     * @return
     */
    private CollectorSensorParamsInfo<SensorGudanNotStressInfo> parserGudanNotStress(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorGudanNotStressInfo info = new SensorGudanNotStressInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.GUDAN_NOT_STRESS);
        info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
        info.setSensitivityK((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        info.setTemperatureCoefficientB((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
        info.setExpansionCoefficient((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
        info.setDatumValueF0((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
        info.setCreateTemperature((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
        info.setManualCorrection(strs[9]);
        bean.setSensorData(info);
        return bean;
    }


    private CollectorSensorParamsInfo<SensorGudanDisplacementInfo> parserGudanDisplacement(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        SensorGudanDisplacementInfo info = new SensorGudanDisplacementInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.GUDAN_DISPLACEMENT_METER);
        info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
        info.setSensitivityK((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
        info.setTemperatureCoefficientB((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
        info.setDatumValueF0((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
        info.setCreateTemperature((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
        info.setManualCorrection(strs[8]);
        bean.setSensorData(info);
        return bean;
    }


    /**
     * 解析轴力计ZLJ-300T
     * $$1010001,01,58,2.000000e+00,4.597945e-08,3.000000e+00,5.000000e+00
     * $$1010001,传感器地址/通道号,传感器类型,触发阀值,标定系数A,基准值F0,手动纠偏,温修系数B,初始温度T0
     *
     * @param strs
     * @return
     */
    private CollectorSensorParamsInfo<SensorJunXingZljInfo> parserJunXingZlj(String[] strs) {
        CollectorSensorParamsInfo bean = new CollectorSensorParamsInfo();
        bean.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
        bean.setChannelNumber(strs[0].substring(7, 9));
        bean.setSensorAddress(strs[1]);
        bean.setSensorType(SensorType.JUNXING_ZLJ_300T);
        SensorJunXingZljInfo sensorInfo = new SensorJunXingZljInfo();
        sensorInfo.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
        sensorInfo.setPolynomialRatioA(strs[4]);
        sensorInfo.setReferenceValue(strs[5]);
        sensorInfo.setManualCorrection(strs[6]);
        sensorInfo.setTemperatureCoefficientB(strs[7]);
        sensorInfo.setCreateTemperature(strs[8]);
        bean.setSensorData(sensorInfo);

        return bean;
    }

}
