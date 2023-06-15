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

            case INCLINOMETER: {//固定测斜仪
                sensorParamsInfo = new CollectorSensorParamsInfo();
                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
                sensorParamsInfo.setSensorAddress(strs[1]);
                sensorParamsInfo.setSensorType(sensorType);
                CommonDigitalSensorInfo commonDigitalSensorInfo = new CommonDigitalSensorInfo();
                commonDigitalSensorInfo.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
                commonDigitalSensorInfo.setCorrectionValue((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
                commonDigitalSensorInfo.setExValue1((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
                sensorParamsInfo.setSensorData(commonDigitalSensorInfo);
            }
            break;

            default: {
//                 RAIN_GAUGE://雨量计
//                 WIRE_SHIFT://裂缝计
//                 SOIL_MOISTURE://管式含水率计
//                 INCLINOMETER://固定测斜仪
//                 ULTRASONIC_LEVEL_GAUGE://超声波液(物)位计
//                 RADAR_LEVEL_GAUGE://雷达液(物)位计
//                 MOISTURE_METER://墒情计
//                 TEMPERATURE_HUMIDITY_METER://温湿度计
//                 UPLIFT_PRESSURE_GAUGE://扬压力计
//                 INFRASOUND://次声仪
//                 STATIC_LEVEL://静力水准
//                 WEATHER_STATION: //气象计
                sensorParamsInfo = new CollectorSensorParamsInfo();
                sensorParamsInfo.setCollectorModel(CollectorModel.value(strs[0].substring(5, 7)));
                sensorParamsInfo.setChannelNumber(strs[0].substring(7, 9));
                sensorParamsInfo.setSensorAddress(strs[1]);
                sensorParamsInfo.setSensorType(sensorType);
                CommonDigitalSensorInfo commonDigitalSensorInfo = new CommonDigitalSensorInfo();
                commonDigitalSensorInfo.setTriggerThreshold((TextUtils.isEmpty(strs[3]) || strs[3].contains("nan")) ? "0" : strs[3]);
                commonDigitalSensorInfo.setCorrectionValue((TextUtils.isEmpty(strs[4]) || strs[4].contains("nan")) ? "0" : strs[4]);
                if (strs.length >= 6)
                    commonDigitalSensorInfo.setExValue1((TextUtils.isEmpty(strs[5]) || strs[5].contains("nan")) ? "0" : strs[5]);
                if (strs.length >= 7)
                    commonDigitalSensorInfo.setExValue2((TextUtils.isEmpty(strs[6]) || strs[6].contains("nan")) ? "0" : strs[6]);
                if (strs.length >= 8)
                    commonDigitalSensorInfo.setExValue3((TextUtils.isEmpty(strs[7]) || strs[7].contains("nan")) ? "0" : strs[7]);
                if (strs.length >= 9)
                    commonDigitalSensorInfo.setExValue4((TextUtils.isEmpty(strs[8]) || strs[8].contains("nan")) ? "0" : strs[8]);
                sensorParamsInfo.setSensorData(commonDigitalSensorInfo);
            }
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
