package com.shmedo.lib.device.base.iot_cmd.model.m20

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
data class M20CurrentStateInfo(
    val ext_power_volt: Double = 0.0, //外接电源电压
    val inner_power_volt: Double = 0.0, //内部电源电压
    val temp: Double = 0.0, //设备内部环境温度，单位摄氏度
    val humidity: Double = 0.0, //设备内部湿度，单位 RH%
    val temp_out: Double = 0.0, //设备外部环境温度，单位摄氏度
    val humidity_out: Double = 0.0, //设备外部环境湿度，单位RH%
    @Json(name = "4g_signal")
    val _4g_signal: Double = 0.0, //4g信号强度
    val bd_signal: Double = 0.0, //北斗信号强度
    val sw_version: String = "", //固件版本
    val location: String = "", //设备位置-经纬度，经度在前,纬度在后。E表示东经，W表示西经，N表示北纬，S表示南纬。
    val sensor_errno: List<SensorErrnoBean>? = arrayListOf(), //传感器异常信息,
    val solar_volt: Double = 0.0, //太阳能板电压,单位V
    val battery_volt: Double = 0.0, //蓄电池电压，单位V
    val supply_power: Double = 0.0, //近12小时补充功率，单位千瓦时
    val consume_power: Double = 0.0, //近12小时消耗功率，单位千瓦时
    val time: String = "",
    val onlinetime: String = "",
    val solar_current: Double = 0.0,
    val battery_current: Double = 0.0,
    val mag: Double = 0.0,
    @Json(name = "IMEI")
    val iMEI: String = "", //IMEI卡号
    @Json(name = "CCID")
    val cCID: String = "", //物联网卡号
    val work_current: Double = 0.0, //设备工作电流，单位A
    val volt_percent: Double = 0.0,
    @Json(name = "X_Angle")
    val x_Angle: Double = 0.0, //X倾角
    @Json(name = "Y_Angle")
    val y_Angle: Double = 0.0, //Y倾角
    @Json(name = "Z_Angle")
    val z_Angle: Double = 0.0, //Z倾角
    @Json(name = "SN")
    val sN: String = "", //设备SN号
    @Json(name = "eMMC Free")
    val eMMCFree: String = "", //存储状态
    val dataCenter1: Int = 0, //数据中心1
    val dataCenter2: Int = 0, //数据中心2
    val dataCenter3: Int = 0, //数据中心3
    val dataCenter4: Int = 0, //数据中心4
    val starNum: Int = 0, //星数
    val gpsCard: Int = 0, //板卡
    val self_check: String = "" //设备自检
)