package com.shmedo.mcloudapp.util.bleutil;

import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.cmd.parser.ParseManager;
import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.model.CollectorSensorParamsInfo;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.bleutil
 * 文件名:   BlueResultParserUtil
 * 创建者:   dpc
 * 创建时间:  2019/3/7 13:39
 * 描述：   蓝牙交互数据解析工具类
 */
public class BlueResultParserUtil {
    //"$$1010200,2,2,10,0.000000\r\n";
    public static CollectorSensorParamsInfoSub setCollectorParams(String result) {
        CollectorSensorParamsInfoSub collectorSensorParamsInfoSub = new CollectorSensorParamsInfoSub();
        CommandResult<CollectorSensorParamsInfo> bean = ParseManager.getInstance().parse(result);
        CollectorSensorParamsInfo collectorSensorParamsInfo = null;

        if (bean.isSuccess()) {
            collectorSensorParamsInfo = bean.getResult();
            collectorSensorParamsInfoSub.setSensorAddress(collectorSensorParamsInfo.getSensorAddress());
            collectorSensorParamsInfoSub.setSensorData(collectorSensorParamsInfo.getSensorData());
            collectorSensorParamsInfoSub.setChannelNumber(collectorSensorParamsInfo.getChannelNumber());
            collectorSensorParamsInfoSub.setCollectorModel(collectorSensorParamsInfo.getCollectorModel().toString());

            return setCollectorSeneorInfo(collectorSensorParamsInfo, collectorSensorParamsInfoSub);
        } else {

            return collectorSensorParamsInfoSub;
        }
    }


    /**
     * 设置传感器类型
     */
    private static CollectorSensorParamsInfoSub setCollectorSeneorInfo(CollectorSensorParamsInfo info, CollectorSensorParamsInfoSub infoSub) {
        switch (info.getSensorType()) {
            case WIRE_SHIFT:
                infoSub.setSensorType("02");
                break;

            case SOIL_MOISTURE:
                infoSub.setSensorType("03");
                break;

            case INCLINOMETER:
                infoSub.setSensorType("04");
                break;

            case ULTRASONIC_LEVEL_GAUGE:
                infoSub.setSensorType("06");
                break;

            case RADAR_LEVEL_GAUGE:
                infoSub.setSensorType("07");
                break;

            case MOISTURE_METER:
                infoSub.setSensorType("08");
                break;

            case TEMPERATURE_HUMIDITY_METER:
                infoSub.setSensorType("12");
                break;

            case UPLIFT_PRESSURE_GAUGE:
                infoSub.setSensorType("15");
                break;

            case KANG_PERCOLATE:
                infoSub.setSensorType("50");
                break;

            case GUDAN_PERCOLATE:
                infoSub.setSensorType("51");
                break;

            case GUDAN_SOIL_PRESSURE:
                infoSub.setSensorType("52");
                break;

            case GUDAN_STRESS:
                infoSub.setSensorType("53");
                break;

            case GUDAN_NOT_STRESS:
                infoSub.setSensorType("54");
                break;

            case GUDAN_DISPLACEMENT_METER:
                infoSub.setSensorType("55");
                break;

            case INFRASOUND_SENSOR:
                infoSub.setSensorType("21");
                break;

            default:
                infoSub.setSensorType("00");//未知的传感器类型
        }

        return infoSub;
    }



    public static String getCollectorName(CollectorModel collectorModel) {
        String collectorName = "";
        switch (collectorModel) {
            case VW08:
                collectorName = "采集器";
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

            case CS08:
                collectorName = "次声采集器";
                break;

            case VW01:
                collectorName = "单通道采集器";
                break;

            default:
                break;
        }

        return collectorName;
    }
}
