package com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr

import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/20 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRRS485Port3SensorParamEntity(
    val device: String = "",//设备类型  1太阳能控制器  2 声光报警器  3 LED屏
    val switch: String = "",//开关  1 开 0关
    val addr: String = IOTConstants.NULL_KEY,//设置地址  数字
    val baud: String = IOTConstants.NULL_KEY,//波特率  bps 数字
    val databit: String = IOTConstants.NULL_KEY,//数据位   数字(5 6 7 8)
    val paritybit: String = IOTConstants.NULL_KEY,//校验位 0:NONE  1:ODD  2:EVEN  3:MARK 4:SPACE
    val stopbit: String = IOTConstants.NULL_KEY,//停止位   0: 1  1: 1.5  2: 2  或 数字(1 1.5 2)
    val status: String = IOTConstants.NULL_KEY,//状态  1 接入 0未接入
    var duration: String = IOTConstants.NULL_KEY,//声光报警器(语音播放时长  s 数字)、LED屏(显示时长  s 数字)
    var interval: String = IOTConstants.NULL_KEY,//声光报警器(切换间隔 ms(s) 数字)、LED屏(更新间隔  s 数字)

    //太阳能控制器
    var svolt: String = IOTConstants.NULL_KEY,//太阳能板电压  V 数字
    var bvolt: String = IOTConstants.NULL_KEY,//蓄电池电压   V 数字
    var spower: String = IOTConstants.NULL_KEY,//太阳能板功率   W 数字
    var lpower: String = IOTConstants.NULL_KEY,//负载功率  W 数字

    //声光报警器
    var volume: String = IOTConstants.NULL_KEY,//音量   数字  百分比
    var alarmtype: String = IOTConstants.NULL_KEY, //报警项   0  降雨量  1 水位
    var rlevel1: String = IOTConstants.NULL_KEY,//降雨量一级报警值 mm 数字
    var rlevel2: String = IOTConstants.NULL_KEY,//降雨量二级报警值 mm 数字
    var rlevel3: String = IOTConstants.NULL_KEY,//降雨量三级报警值 mm 数字
    var wlevel1: String = IOTConstants.NULL_KEY,//水位一级报警值 mm 数字
    var wlevel2: String = IOTConstants.NULL_KEY,//水位二级报警值 mm 数字
    var wlevel3: String = IOTConstants.NULL_KEY,//水位三级报警值 mm 数字

    //LED屏
    var type: String = IOTConstants.NULL_KEY,//显示配置   1 类型1  2 类型2
    var stime: String = IOTConstants.NULL_KEY,//熄屏时长 s  数字
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}

