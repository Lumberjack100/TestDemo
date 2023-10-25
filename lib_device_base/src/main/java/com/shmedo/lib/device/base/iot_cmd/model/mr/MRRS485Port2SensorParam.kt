package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/24 <br/>
 * 描述：      RS485-2-串口下传感器参数
 */
data class MRRS485Port2SensorParam(
    var chl: String = "",//通道编号 根据设备来最多到通道15
    var model: String = "",//物模型
    var swtoken: String = "",//水文标识
    var sensoraddr: String = "",//传感器地址
    var sensortype: String = "",//传感器类型
    var filtercnt: String = "",//滤波次数
    var gateval: String = "",//触发值
    var uplimit: String = "",//上限值
    var lowlimit: String = "",//下限值
    var corrvalue: String = "",//修正值
)
