package com.shmedo.mcloudapp.util.bleutil;

import com.shmedo.configlibrary.ble.enums.CollectorModel;

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
                break;
        }

        return collectorName;
    }
}
