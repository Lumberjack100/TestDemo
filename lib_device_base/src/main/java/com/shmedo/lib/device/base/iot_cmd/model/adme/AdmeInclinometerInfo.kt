package com.shmedo.lib.device.base.iot_cmd.model.adme

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/28/20 <br></br>
 * 描述：     ADME 测斜仪配置参数
 */
@JsonClass(generateAdapter = true)
data class AdmeInclinometerInfo(
    var inctype: String = "", //测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    var address: String = "", //采集器 / MAC 地址
    var collinval: String = "", //采集器采集间隔
    var calcinval: String = "", //采集器解算间隔
    var dormancytime: String = "", //休眠时间
    var interupdate: String = "",//测斜仪修正值
    var mode: String = "", //测量工作模式(5:蓝牙关测量关，7:蓝牙开测量关，8:蓝牙关测量开，9:蓝牙开测量开)
    var incversion: String = "", //测斜仪版本（0：2.1 版本，1：3.0 版本）
    var compenway: String = "", //补偿方式（0：X+Y轴无扭转角补偿，1：X轴扭转角补偿）
    var torangle: String = "", //扭转角γ
)