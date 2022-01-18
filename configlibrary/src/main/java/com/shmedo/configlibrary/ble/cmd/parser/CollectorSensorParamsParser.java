package com.shmedo.configlibrary.ble.cmd.parser;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.CollectorSensorParamsInfo;
import com.shmedo.configlibrary.ble.model.CommonDigitalSensorInfo;
import com.shmedo.configlibrary.ble.model.SensorGudanDisplacementInfo;
import com.shmedo.configlibrary.ble.model.SensorGudanNotStressInfo;
import com.shmedo.configlibrary.ble.model.SensorGudanPercolateInfo;
import com.shmedo.configlibrary.ble.model.SensorGudanSoilPressureInfo;
import com.shmedo.configlibrary.ble.model.SensorGudanStressInfo;
import com.shmedo.configlibrary.ble.model.SensorJunXingZljInfo;
import com.shmedo.configlibrary.ble.model.SensorKangPercolateInfo;
import com.shmedo.configlibrary.ble.utils.StringUtil;


/**
 * Created by adu on 2017/12/14.
 * 解析 获取XX采集器YY通道的传感器参数
 */
@Parser
public class CollectorSensorParamsParser implements ResultParser<CollectorSensorParamsInfo> {

    @Override
    public CollectorSensorParamsInfo parse(String result) {
        CollectorSensorParamsInfo sensorParamsInfo = null;
        String[] strs = result.split(",");
        String sensorTypeCode = StringUtil.formatStringTwo(strs[2]);
        SensorType sensorType = SensorType.value(sensorTypeCode);
        switch (sensorType) {
            case RAIN_GAUGE://雨量计
            case WIRE_SHIFT://裂缝计
            case SOIL_MOISTURE://管式含水率计
            case INCLINOMETER://固定测斜仪
            case ULTRASONIC_LEVEL_GAUGE://超声波液(物)位计
            case RADAR_LEVEL_GAUGE://雷达液(物)位计
            case MOISTURE_METER://墒情计
            case TEMPERATURE_HUMIDITY_METER://温湿度计
            case UPLIFT_PRESSURE_GAUGE://扬压力计
            case INFRASOUND://次声仪
            case STATIC_LEVEL://静力水准
            case WEATHER_STATION: //气象计
            {
                sensorParamsInfo = new CollectorSensorParamsInfo();
                CommonDigitalSensorInfo commonDigitalSensorInfo = new CommonDigitalSensorInfo();
                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
                sensorParamsInfo.setSensorAddress(strs[1]);
                sensorParamsInfo.setSensorType(sensorType);
                commonDigitalSensorInfo.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
                commonDigitalSensorInfo.setCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
                if (strs.length >= 6)
                    commonDigitalSensorInfo.setExValue1((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
                if (strs.length >= 7)
                    commonDigitalSensorInfo.setExValue1((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
                if (strs.length >= 8)
                    commonDigitalSensorInfo.setExValue1((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
                if (strs.length >= 9)
                    commonDigitalSensorInfo.setExValue1((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
                sensorParamsInfo.setSensorData(commonDigitalSensorInfo);
            }
            break;

//            case INCLINOMETER: {//固定测斜仪
//                sensorParamsInfo = new CollectorSensorParamsInfo();
//                SensorInclinometerInfo info = new SensorInclinometerInfo();
//                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
//                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
//                sensorParamsInfo.setSensorAddress(strs[1]);
//                sensorParamsInfo.setSensorType(sensorType);
//                info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
//                info.setMeasureLength((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
//                info.setCorrectionValue((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
//                sensorParamsInfo.setSensorData(info);
//            }
//            break;

//            case ULTRASONIC_LEVEL_GAUGE: {//超声波液(物)位计
//                sensorParamsInfo = new CollectorSensorParamsInfo();
//                SensorUltrasonicLevelInfo info = new SensorUltrasonicLevelInfo();
//                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
//                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
//                sensorParamsInfo.setSensorAddress(strs[1]);
//                sensorParamsInfo.setSensorType(sensorType);
//                info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
//                info.setCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
//                if (strs.length == 6)
//                    info.setProbeElevation(strs[5]);
//
//                sensorParamsInfo.setSensorData(info);
//            }
//            break;

//            case MOISTURE_METER: {//墒情计
//                sensorParamsInfo = new CollectorSensorParamsInfo();
//                SensorMoistureMeterInfo info = new SensorMoistureMeterInfo();
//                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
//                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
//                sensorParamsInfo.setSensorAddress(strs[1]);
//                sensorParamsInfo.setSensorType(sensorType);
//                info.setHumidityTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
//                info.setHumidityCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
//                info.setSaltTriggerThreshold((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
//                info.setSaltCorrectionValue((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
//                info.setTemperatureTriggerThreshold((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
//                info.setTemperatureCorrectionValue((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
//                sensorParamsInfo.setSensorData(info);
//            }
//            break;

//            case UPLIFT_PRESSURE_GAUGE: {//扬压力计
//                sensorParamsInfo = new CollectorSensorParamsInfo();
//                SensorUpliftPressureInfo info = new SensorUpliftPressureInfo();
//                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
//                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
//                sensorParamsInfo.setSensorAddress(strs[1]);
//                sensorParamsInfo.setSensorType(sensorType);
//                info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
//                info.setCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
//                info.setCordlength((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
//                info.setInstallationElevation((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
//                sensorParamsInfo.setSensorData(info);
//            }
//            break;

            case KANG_PERCOLATE: {//基康渗压计
                sensorParamsInfo = new CollectorSensorParamsInfo();
                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
                sensorParamsInfo.setSensorAddress(strs[1]);
                sensorParamsInfo.setSensorType(sensorType);
                SensorKangPercolateInfo sensorInfo = new SensorKangPercolateInfo();
                sensorInfo.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
                sensorInfo.setPolynomialRatioA((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
                sensorInfo.setPolynomialRatioB((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
                sensorInfo.setPolynomialRatioC((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
                sensorInfo.setTemperatureCoefficientK((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
                sensorInfo.setCreateTemperature((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
                sensorInfo.setManualCorrection((TextUtils.isEmpty(strs[9]) || strs[9].contains("nan")) ? "0" : strs[9]);
                sensorInfo.setCordLenght((TextUtils.isEmpty(strs[10]) || strs[10].contains("nan")) ? "0" : strs[10]);
                sensorInfo.setInstallElevation((TextUtils.isEmpty(strs[11]) || strs[11].contains("nan")) ? "0" : strs[11]);
                sensorParamsInfo.setSensorData(sensorInfo);
            }
            break;

            case GUDAN_PERCOLATE: {//葛南渗压计
                sensorParamsInfo = new CollectorSensorParamsInfo();
                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
                sensorParamsInfo.setSensorAddress(strs[1]);
                sensorParamsInfo.setSensorType(sensorType);
                SensorGudanPercolateInfo sensorInfo = new SensorGudanPercolateInfo();
                sensorInfo.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
                sensorInfo.setSensitivityK((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
                sensorInfo.setTemperatureCoefficientB((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
                sensorInfo.setReferenceValue((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
                sensorInfo.setCreateTemperature((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
                sensorInfo.setManualCorrection((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
                sensorInfo.setCordLenght((TextUtils.isEmpty(strs[9]) || strs[9].contains("nan")) ? "0" : strs[9]);
                sensorInfo.setInstallElevation((TextUtils.isEmpty(strs[10]) || strs[10].contains("nan")) ? "0" : strs[10]);
                sensorParamsInfo.setSensorData(sensorInfo);
            }
            break;

            case GUDAN_SOIL_PRESSURE: {//葛南土压力计
                sensorParamsInfo = new CollectorSensorParamsInfo();
                SensorGudanSoilPressureInfo info = new SensorGudanSoilPressureInfo();
                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
                sensorParamsInfo.setSensorAddress(strs[1]);
                sensorParamsInfo.setSensorType(sensorType);
                info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
                info.setSensitivityK((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
                info.setTemperatureCoefficientB((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
                info.setDatumValueF0((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
                info.setCreateTemperature((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
                info.setManualCorrection((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
                sensorParamsInfo.setSensorData(info);
            }
            break;

            case GUDAN_STRESS: {//葛南应力计
                sensorParamsInfo = new CollectorSensorParamsInfo();
                SensorGudanStressInfo info = new SensorGudanStressInfo();
                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
                sensorParamsInfo.setSensorAddress(strs[1]);
                sensorParamsInfo.setSensorType(sensorType);
                info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
                info.setSensitivityK((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
                info.setTemperatureCoefficientB((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
                info.setExpansionCoefficient((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
                info.setDatumValueF0((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
                info.setCreateTemperature((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
                info.setManualCorrection((TextUtils.isEmpty(strs[9]) || strs[9].contains("nan")) ? "0" : strs[9]);
                sensorParamsInfo.setSensorData(info);
            }
            break;

            case GUDAN_NOT_STRESS: {//葛南无应力计
                sensorParamsInfo = new CollectorSensorParamsInfo();
                SensorGudanNotStressInfo info = new SensorGudanNotStressInfo();
                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
                sensorParamsInfo.setSensorAddress(strs[1]);
                sensorParamsInfo.setSensorType(sensorType);
                info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
                info.setSensitivityK((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
                info.setTemperatureCoefficientB((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
                info.setExpansionCoefficient((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
                info.setDatumValueF0((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
                info.setCreateTemperature((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
                info.setManualCorrection((TextUtils.isEmpty(strs[9]) || strs[9].contains("nan")) ? "0" : strs[9]);
                sensorParamsInfo.setSensorData(info);
            }
            break;

            case GUDAN_DISPLACEMENT_METER: {//葛南位移计
                sensorParamsInfo = new CollectorSensorParamsInfo();
                SensorGudanDisplacementInfo info = new SensorGudanDisplacementInfo();
                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
                sensorParamsInfo.setSensorAddress(strs[1]);
                sensorParamsInfo.setSensorType(sensorType);
                info.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
                info.setSensitivityK((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
                info.setTemperatureCoefficientB((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
                info.setDatumValueF0((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
                info.setCreateTemperature((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
                info.setManualCorrection((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
                sensorParamsInfo.setSensorData(info);
            }
            break;

            case JUNXING_ZLJ_300T: {//轴力计
                sensorParamsInfo = new CollectorSensorParamsInfo();
                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
                sensorParamsInfo.setSensorAddress(strs[1]);
                sensorParamsInfo.setSensorType(sensorType);
                SensorJunXingZljInfo sensorInfo = new SensorJunXingZljInfo();
                sensorInfo.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
                sensorInfo.setSensitivityK(strs[4]);
                sensorInfo.setReferenceValue(strs[5]);
                sensorInfo.setManualCorrection(strs[6]);
                sensorInfo.setTemperatureCoefficientB(strs[7]);
                sensorInfo.setCreateTemperature(strs[8]);
                sensorParamsInfo.setSensorData(sensorInfo);
            }
            break;

            default:
                break;
        }

        return sensorParamsInfo;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER;
    }
}
