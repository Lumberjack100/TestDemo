package com.shmedo.lib.device.base.iot_cmd.model.common

import com.shmedo.lib.device.base.iot_cmd.model.m20.SensorErrnoBean
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe
 *
 * 创建时间:  1/19/21
 *
 * 描述：   M20设备当前状态
 *
 * ext_power_volt : 0
 * inner_power_volt : 13.21
 * temp : 37.46
 * humidity : 0
 * temp_out : 0
 * humidity_out : 0
 * 4g_signal : -29
 * bd_signal : 0
 * sw_version : 1.3.99
 * sensor_errno : [{"errno":0,"sensor_id":"203_1"}]
 * location : 0.00000000E,0.00000000N
 * IMEI : 867435053801965
 * CCID : 89860445101970723723
 * solar_volt : 0
 * battery_volt : 0
 * supply_power : 0
 * consume_power : 0
 * work_current : 0
 * volt_percent : 0
 * Z_Angle : -86.15
 * SN : M2020C002V
 * eMMC Free : 13257MB
 * dataCenter3 : 1
 * dataCenter4 : 0
 * starNum : 0
 * gpsCard : S22221K726,391TN-2.059-1
 * self_check : GPS:1,eMMC:1,4g:1,RTC:1,solar485:0,G-Sensor:1
 */
@JsonClass(generateAdapter = true)
data class CommonCurrentStateInfo(
    var ext_power_volt: String = IOTConstants.NULL_KEY, //外接电源电压
    var inner_power_volt: String = IOTConstants.NULL_KEY, //内部电源电压
    var temp: String = IOTConstants.NULL_KEY, //设备内部环境温度，单位摄氏度
    var humidity: String = IOTConstants.NULL_KEY, //设备内部湿度，单位 RH%
    var temp_out: String = IOTConstants.NULL_KEY, //设备外部环境温度，单位摄氏度
    var humidity_out: String = IOTConstants.NULL_KEY, //设备外部环境湿度，单位RH%
    @Json(name = "4g_signal")
    var _4g_signal: Int = 0, //4g信号强度
    var bd_signal: Double = 0.0, //北斗信号强度
    var hw_version: String = IOTConstants.NULL_KEY, //硬件版本
    var sw_version: String = IOTConstants.NULL_KEY, //固件版本
    var location: String = IOTConstants.NULL_KEY, //设备位置-经纬度，经度在前,纬度在后。E表示东经，W表示西经，N表示北纬，S表示南纬。
    var sensor_errno: List<SensorErrnoBean>? = arrayListOf(), //传感器异常信息,
    var solar_volt: String = IOTConstants.NULL_KEY, //太阳能板电压,单位V
    var battery_volt: String = IOTConstants.NULL_KEY, //蓄电池电压，单位V
    var supply_power: String = IOTConstants.NULL_KEY, //近12小时补充功率，单位千瓦时
    var consume_power: String = IOTConstants.NULL_KEY, //近12小时消耗功率，单位千瓦时
    var time: String = IOTConstants.NULL_KEY,
    var onlinetime: String = IOTConstants.NULL_KEY,
    var solar_current: String = IOTConstants.NULL_KEY,
    var battery_current: String = IOTConstants.NULL_KEY,
    var mag: String = IOTConstants.NULL_KEY,
    @Json(name = "IMEI")
    var iMEI: String = IOTConstants.NULL_KEY, //IMEI卡号
    @Json(name = "IMSI")
    var iMSI: String = IOTConstants.NULL_KEY, //
    @Json(name = "CCID")
    var cCID: String = IOTConstants.NULL_KEY, //物联网卡号
    var work_current: String = IOTConstants.NULL_KEY, //设备工作电流，单位A
    var volt_percent: String = IOTConstants.NULL_KEY,
    @Json(name = "X_Angle")
    var x_Angle: String = IOTConstants.NULL_KEY, //X倾角
    @Json(name = "Y_Angle")
    var y_Angle: String = IOTConstants.NULL_KEY, //Y倾角
    @Json(name = "Z_Angle")
    var z_Angle: String = IOTConstants.NULL_KEY, //Z倾角
    @Json(name = "SN")
    var sN: String = IOTConstants.NULL_KEY, //设备SN号
    @Json(name = "eMMC Free")
    var eMMCFree: String = IOTConstants.NULL_KEY, //存储状态
    var dataCenter1: Int = 0, //数据中心1
    var dataCenter2: Int = 0, //数据中心2
    var dataCenter3: Int = 0, //数据中心3
    var dataCenter4: Int = 0, //数据中心4
    var starNum: Int = 0, //星数
    var gpsCard: String = IOTConstants.NULL_KEY, //板卡
    var self_check: String = IOTConstants.NULL_KEY,//设备自检
    var workMode: String = IOTConstants.NULL_KEY, //工作模式 1:固定站  2:移动站
    var worktime: String = IOTConstants.NULL_KEY,//运行时间
    var emmc_storage: String = IOTConstants.NULL_KEY,//emmc存储
)