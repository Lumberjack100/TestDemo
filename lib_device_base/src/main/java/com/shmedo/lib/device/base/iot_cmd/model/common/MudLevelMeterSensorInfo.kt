package com.shmedo.lib.device.base.iot_cmd.model.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： 泥位计传感器参数
 */
data class MudLevelMeterSensorInfo(
    var height: String = "", //安装高度
    var gap: String = "", //测量间隔  雷达测量间隔时间(ms)
    var times: String = "", //平均次数  数据平均次数
    var level: String = "", //能够触发拍照的级别
    var pixx: String = "", //图片水平分辨率
    var pixy: String = "", //图片垂直分辨率
)
