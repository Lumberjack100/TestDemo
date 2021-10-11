package com.shmedo.mcloudapp.util;

import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.iot.enums.IOTCollectorModel;
import com.shmedo.configlibrary.iot.enums.IOTSensorType;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.bleutil
 * 文件名:   BlueResultParserUtil
 * 创建者:   dpc
 * 创建时间:  2019/3/7 13:39
 * 描述：   蓝牙交互数据解析工具类
 */
public class BlueResultParserUtil {

    /**
     * 根据采集器型号得到采集器名称
     *
     * @param collectorModel
     * @return
     */
    public static String getCollectorName(CollectorModel collectorModel) {
        String collectorName = "";
        switch (collectorModel) {
            case VW08:
                collectorName = "振弦式采集器";
                break;

            case RAIN08:
                collectorName = "雨量采集器";
                break;

            case DS08:
                collectorName = "裂缝计采集器";
                break;

            case HD08:
                collectorName = "土壤湿度采集器";
                break;

            case CX08:
                collectorName = "测斜仪采集器";
                break;

            case UDS08:
                collectorName = "超声波采集器";
                break;

            case RD08:
                collectorName = "雷达采集器";
                break;

            case SMC08:
                collectorName = "墒情采集器";
                break;

            case TH08:
                collectorName = "温湿度采集器";
                break;

            case DVWP:
                collectorName = "数字式渗压计采集器";
                break;

            case QJY08:
                collectorName = "倾角仪采集器";
                break;

            case VW01:
                collectorName = "单通道采集器";
                break;

            case CS08:
                collectorName = "次声采集器";
                break;

            default:
                collectorName = "未知采集器";
                break;
        }

        return collectorName;
    }

    /**
     * 根据传感器类型得到传感器名称
     *
     * @param sensorType
     * @return
     */
    public static String getSensorName(SensorType sensorType) {
        String sensorName = "";
        switch (sensorType) {
            case RAIN_GAUGE:
                sensorName = "压电式雨量计";
                break;

            case WIRE_SHIFT:
                sensorName = "拉线位移计";
                break;

            case SOIL_MOISTURE:
                sensorName = "土壤含水率计";
                break;

            case INCLINOMETER:
                sensorName = "测斜仪";
                break;

            case ULTRASONIC_LEVEL_GAUGE:
                sensorName = "超声波物位计";
                break;

            case RADAR_LEVEL_GAUGE:
                sensorName = "雷达物位计";
                break;

            case MOISTURE_METER:
                sensorName = "墒情计";
                break;

            case TEMPERATURE_HUMIDITY_METER:
                sensorName = "温湿度计";
                break;

            case UPLIFT_PRESSURE_GAUGE:
                sensorName = "扬压力计";
                break;

            case LUYAN_INCLINOMETER:
                sensorName = "陆岩倾角仪";
                break;

            case INFRASOUND_SENSOR:
                sensorName = "次声传感器";
                break;

            case KANG_PERCOLATE:
                sensorName = "基康渗压计";
                break;

            case GUDAN_PERCOLATE:
                sensorName = "葛南渗压计";
                break;

            case GUDAN_SOIL_PRESSURE:
                sensorName = "葛南土压力盒";
                break;

            case GUDAN_STRESS:
                sensorName = "葛南应力计";
                break;

            case GUDAN_NOT_STRESS:
                sensorName = "葛南无应力计";
                break;

            case GUDAN_DISPLACEMENT_METER:
                sensorName = "葛南位移计";
                break;

            case JUNXING_ZLJ_300T:
                sensorName = "军星轴力计";
                break;
        }

        return sensorName;
    }

    /**
     * 根据传感器类型得到传感器名称
     *
     * @param iotSensorType
     * @return
     */
    public static String getSensorName(IOTSensorType iotSensorType) {
        String sensorName = "";
        switch (iotSensorType) {
            case RAIN_GAUGE:
                sensorName = "压电式雨量计";
                break;

            case WIRE_SHIFT:
                sensorName = "拉线位移计";
                break;

            case SOIL_MOISTURE:
                sensorName = "土壤含水率计";
                break;

            case INCLINOMETER:
                sensorName = "测斜仪";
                break;

            case ULTRASONIC_LEVEL_GAUGE:
                sensorName = "超声波物位计";
                break;

            case RADAR_LEVEL_GAUGE:
                sensorName = "雷达物位计";
                break;

            case MOISTURE_METER:
                sensorName = "墒情计";
                break;

            case TEMPERATURE_HUMIDITY_METER:
                sensorName = "温湿度计";
                break;

            case UPLIFT_PRESSURE_GAUGE:
                sensorName = "扬压力计";
                break;

            case LUYAN_INCLINOMETER:
                sensorName = "陆岩倾角仪";
                break;

            case INFRASOUND_SENSOR:
                sensorName = "次声传感器";
                break;

            case KANG_PERCOLATE:
                sensorName = "基康渗压计";
                break;

            case GUDAN_PERCOLATE:
                sensorName = "葛南渗压计";
                break;

            case GUDAN_SOIL_PRESSURE:
                sensorName = "葛南土压力盒";
                break;

            case GUDAN_STRESS:
                sensorName = "葛南应力计";
                break;

            case GUDAN_NOT_STRESS:
                sensorName = "葛南无应力计";
                break;

            case GUDAN_DISPLACEMENT_METER:
                sensorName = "葛南位移计";
                break;

            case JUNXING_ZLJ_300T:
                sensorName = "军星轴力计";
                break;
        }

        return sensorName;
    }

    /**
     *
     * @param code
     * @return
     */
    public static IOTSensorType getSensorTypeByCollectorCode(String code) {
        switch (IOTCollectorModel.value(code)) {
            case VW08:
                return IOTSensorType.KANG_PERCOLATE;

            case RAIN08:
                return IOTSensorType.RAIN_GAUGE;

            case DS08:
                return IOTSensorType.WIRE_SHIFT;

            case HD08:
                return IOTSensorType.SOIL_MOISTURE;

            case CX08:
                return IOTSensorType.INCLINOMETER;

            case UDS08:
                return IOTSensorType.ULTRASONIC_LEVEL_GAUGE;

            case RD08:
                return IOTSensorType.RADAR_LEVEL_GAUGE;

            case SMC08:
                return IOTSensorType.MOISTURE_METER;

            case TH08:
                return IOTSensorType.TEMPERATURE_HUMIDITY_METER;

            case CS08:
                return IOTSensorType.INFRASOUND_SENSOR;

            case QXZ:
                return IOTSensorType.WEATHER_STATION;

            default:
                return null;
        }
    }
}
