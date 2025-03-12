package com.shmedo.lib.cmd.base.iot_cmd.model.mr

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/27 <br/>
 * 描述：    RS485-1-串口下传感器参数
 */
@JsonClass(generateAdapter = true)
data class MRRS485Port1SensorParam(
    var c_model: String = "",//创建新指令    --创建新指令发1。修改指令发0
    var num: String = "",//物模型变量	--创建新指令时可以传任意值。修改指令根据传指定变量
    @Json(name = "sensorlist")
    var sensorId: String = "",//传感器编号
    var model: String = "",//物模型
    var sgbk: String = "",//传感器名称GBK编码
    var mgbk: String = "",//采集项名称GBK编码
    var egbk: String = "",//采集项单位GBK编码
    var baud: String = "",//波特率  bps 数字
    var databit: String = "",//数据位   数字(5 6 7 8)
    var parity: String = "",//校验位 0:NONE  1:ODD  2:EVEN  3:MARK 4:SPACE
    var stopbit: String = "",//停止位   0: 1  1: 1.5  2: 2  或 数字(1 1.5 2)
    var swtoken: String = "",//水文标识
    var cmd: String = "",//传感器采集指令
    var ratio: String = "",//倍率
    var dataformat: String = "",//数据类型
    val baseflag: String =  "",//站点类型   0：参考点 1：测点
    var calctype: String = "",//计算方式   0：不计算 1：线性方程计算 2：传感器联合计算
    var gateval: String = "",//触发值
    var uplimit: String = "",//上限值
    var lowlimit: String = "",//下限值
    var corrvalue: String = "",//修正值
    var ngateval: String = "",//阈值次数
    var show: String = "",//展示指令信息（终端），1：展示，0：不展示
    var kvalue: String = "",//灵敏度K
    var bvalue: String = "",//温度修正系数 b
    var r0value: String = "",//初始频率 F0
    var t0value: String = "",//初始温度 T0
    var l0value: String = "",//初始水位
    var lvalue: String = ""//初始测量值
)
