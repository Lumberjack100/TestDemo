package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/27 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRRS485Port1SensorParamEntity(
    val model: String = "",//物模型
    val c_model: String = "",//创建新指令    --创建新指令发1。修改指令发0
    val num: String = "",//物模型变量	--创建新指令时可以传任意值。修改指令根据传指定变量
    val swtoken: String = IOTConstants.NULL_KEY,//水文标识
    var sensorlist: String = "-1",//传感器编号
    val cmd: String = IOTConstants.NULL_KEY,//传感器采集指令
    val ratio: String = IOTConstants.NULL_KEY,//倍率
    val dataformat: String = IOTConstants.NULL_KEY,//数据类型
    val baseflag: String = IOTConstants.NULL_KEY,//站点类型   无  0：参考点 1：测点
    val calctype: String = IOTConstants.NULL_KEY,//计算方式   0：不计算 1：线性方程计算 2：传感器联合计算
    val gateval: String = IOTConstants.NULL_KEY,//触发值
    val uplimit: String = IOTConstants.NULL_KEY,//上限值
    val lowlimit: String = IOTConstants.NULL_KEY,//下限值
    val corrvalue: String = IOTConstants.NULL_KEY,//修正值
    val ngateval: String = IOTConstants.NULL_KEY,//阈值次数
    val baud: String = IOTConstants.NULL_KEY,//波特率  bps 数字
    val databit: String = IOTConstants.NULL_KEY,//数据位   数字(5 6 7 8)
    val parity: String = IOTConstants.NULL_KEY,//校验位 0:NONE  1:ODD  2:EVEN  3:MARK 4:SPACE
    val stopbit: String = IOTConstants.NULL_KEY,//停止位   0: 1  1: 1.5  2: 2  或 数字(1 1.5 2)
    val mgbk: String = IOTConstants.NULL_KEY,//采集项名称GBK编码
    val egbk: String = IOTConstants.NULL_KEY,//采集项单位GBK编码
    val sgbk: String = IOTConstants.NULL_KEY,//传感器名称GBK编码

    val kvalue: String = IOTConstants.NULL_KEY,//灵敏度K         计算方式为"线性方程计算"时，启用此参数
    val bvalue: String = IOTConstants.NULL_KEY,//温度修正系数 b   计算方式为"线性方程计算"时，启用此参数
    val powvalue: String = IOTConstants.NULL_KEY,//指数   计算方式为"线性方程计算"时，启用此参数
    val r0value: String = IOTConstants.NULL_KEY,//初始频率 F0    计算方式为"线性方程计算"时，启用此参数
    val t0value: String = IOTConstants.NULL_KEY,//初始温度 T0    计算方式为"线性方程计算"时，启用此参数
    val l0value: String = IOTConstants.NULL_KEY,//初始水位       计算方式为"线性方程计算"时，启用此参数
    val lvalue: String = IOTConstants.NULL_KEY,//初始测量值      计算方式为"线性方程计算"时，启用此参数
    val initvalue: String = IOTConstants.NULL_KEY//初始测量值  计算方式为"传感器联合计算"时，启用此参数
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value.toString().trim()}" }
    }
}
