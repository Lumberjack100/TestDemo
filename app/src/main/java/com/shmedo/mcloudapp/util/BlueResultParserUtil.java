package com.shmedo.mcloudapp.util;

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
                return IOTSensorType.INFRASOUND;

            case QXZ:
                return IOTSensorType.WEATHER_STATION;

            case LSY:
                return IOTSensorType.WEIR;

            default:
                return null;
        }
    }
}
