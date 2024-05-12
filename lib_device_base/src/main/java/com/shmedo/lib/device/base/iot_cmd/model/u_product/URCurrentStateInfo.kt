package com.shmedo.lib.device.base.iot_cmd.model.u_product

import com.shmedo.lib.device.base.iot_cmd.model.lb20s.URSensorInfo
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
data class URCurrentStateInfo(
    val ext_power_volt: String = IOTConstants.NULL_KEY, //外接电源电压
    val inner_power_volt: String = IOTConstants.NULL_KEY, //内部电源电压
    val temp: String = IOTConstants.NULL_KEY, //设备内部环境温度，单位摄氏度
    val humidity: String = IOTConstants.NULL_KEY, //设备内部湿度，单位 RH%
    val temp_out: String = IOTConstants.NULL_KEY, //设备外部环境温度，单位摄氏度
    val humidity_out: String = IOTConstants.NULL_KEY, //设备外部环境湿度，单位RH%
    @Json(name = "4g_signal")
    val _4g_signal: Int = 0, //4g信号强度
    val on_4g: Boolean = true, //
    val bd_signal: Double = 0.0, //北斗信号强度
    val sw_version: String = IOTConstants.NULL_KEY, //固件版本
    val location: String = IOTConstants.NULL_KEY, //设备位置-经纬度，经度在前,纬度在后。E表示东经，W表示西经，N表示北纬，S表示南纬。
    val sensor_errno: List<SensorErrnoBean>? = arrayListOf(), //传感器异常信息,
    val attach_data: List<URSensorInfo>? = arrayListOf(), //
)

@JsonClass(generateAdapter = true)
data class URSensorInfo(
    val key: String = "",
    val value: String = ""
)