package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/27 <br/>
 * 描述：    RS485-1-串口下传感器参数
 */
data class MRRS485Port1SensorParam(
    var model: String = "",//物模型
    var c_model: String = "",//创建新指令    --创建新指令发1。修改指令发0
    var sensoraddr: String = "",//传感器地址
    var num: String = "",//物模型变量	--创建新指令时可以传任意值。修改指令根据传指定变量
    var swtoken: String = "",//水文标识
    var cmd: String = "",//传感器采集指令
    var ratio: String = "",//倍率
    var dataformat: String = "",//数据类型
    var calctype: String = "",//解算方式   --目前只支持 0:加权平均
    var gateval: String = "",//触发值
    var uplimit: String = "",//上限值
    var lowlimit: String = "",//下限值
    var corrvalue: String = "",//修正值
    var baud: String = "",//波特率  bps 数字
    var databit: String = "",//数据位   数字(5 6 7 8)
    var paritybit: String = "",//校验位 1:NONE  2:ODD  3:EVEN  4:MARK 5:SPACE
    var stopbit: String = "",//停止位   1: 1  2: 1.5  3: 2  或 数字(1 1.5 2)
    var show: String = "",//展示指令信息（终端），1：展示，0：不展示
)
