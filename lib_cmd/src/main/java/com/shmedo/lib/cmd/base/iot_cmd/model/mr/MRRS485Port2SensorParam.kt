package com.shmedo.lib.cmd.base.iot_cmd.model.mr

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
    var calctype: String = "",//是否计算
    var kvalue: String = "",//灵敏度K
    var bvalue: String = "",//温度修正系数 b
    var r0value: String = "",//初始频率 F0
    var t0value: String = "",//初始温度 T0
    var l0value: String = "",//初始水位
    var lvalue: String = "",//堰角高度
    var polyavalue: String = "",//多项式系数A值
    var polybvalue: String = "",//多项式系数B值
    var polycvalue: String = "",//多项式系数C值
)
