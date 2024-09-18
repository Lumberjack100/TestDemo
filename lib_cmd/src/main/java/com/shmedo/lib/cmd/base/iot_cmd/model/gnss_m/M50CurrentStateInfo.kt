package com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2024/8/27
 * @desc: 一体化雷达设备当前状态
 *
 */
@JsonClass(generateAdapter = true)
data class M50CurrentStateInfo(
    @Json(name = "dev_type")
    val deviceType: String = IOTConstants.NULL_KEY, //设备型号   "M50"
    @Json(name = "SN")
    val sn: String = IOTConstants.NULL_KEY, //设备SN号
    @Json(name = "dev_sta")
    val deviceStatus: String = IOTConstants.NULL_KEY, //设备状态  0：正常  -2:告警  -3：设备故障
    @Json(name = "sw_version")
    val firmwareVersion: String = IOTConstants.NULL_KEY, //固件版本 "2.0.1-M2"
    @Json(name = "sw_date")
    val firmwareDate: String = IOTConstants.NULL_KEY, //固件日期  "2024.08.27"
    @Json(name = "boot_code")
    val bootCode: String = IOTConstants.NULL_KEY, //启动代码  "2000"
    @Json(name = "boot_time")
    val bootTime: String = IOTConstants.NULL_KEY, //启动时间   "2024.08.29 17:58:32"
    @Json(name = "run_time")
    val runTime: String = IOTConstants.NULL_KEY, //本次运行时间 单位：秒  "55832"
    @Json(name = "total_run_time")
    val totalRunTime: String = IOTConstants.NULL_KEY, //累计运行时间  单位：秒 "56202"
    val workMode: String = IOTConstants.NULL_KEY, //工作模式  1:基站  2:测站
    @Json(name = "rept_mode")
    val reportMode: String = IOTConstants.NULL_KEY, //上报模式 0:常在线  1:低功耗
    @Json(name = "cap_freq")
    val captureFrequency: String = IOTConstants.NULL_KEY, //抓拍频率 单位：分钟/次 "120"
    @Json(name = "emmc_free")
    var emmcFree: String = IOTConstants.NULL_KEY, //可用空间
    @Json(name = "emmc_storage")
    var emmcStorage: String = IOTConstants.NULL_KEY,//总空间
    @Json(name = "mobile_net")
    val mobileNet: String = IOTConstants.NULL_KEY, //移动网络（连接与否） 0：未连接 1：已连接
    @Json(name = "net_type")
    val netType: String = IOTConstants.NULL_KEY, //网络类型  "4G"
    val operator: String = IOTConstants.NULL_KEY, //运营商  CMCC(中国移动)、CU(中国联通)、CT(中国电信)
    val csq: String = IOTConstants.NULL_KEY, //信号强度  "10"
    @Json(name = "IMEI")
    val imei: String = IOTConstants.NULL_KEY,   //"865019074780812"
    @Json(name = "ICCID")
    val iccid: String = IOTConstants.NULL_KEY, //"898608162623D0102610"
    @Json(name = "radio_use")
    val radioEnableStatus: String = IOTConstants.NULL_KEY, //电台（启用/未启用）0：未启用  1：启用
    val bt_connected: String = IOTConstants.NULL_KEY, //蓝牙（连接/未连接） 0：未连接 1：已连接
    @Json(name = "dataCenterUseSta")
    val dataCenterEnableStatus: String = IOTConstants.NULL_KEY, //数据中心启用状态 "1,0,0,0"
    @Json(name = "dataCenterStatus")
    val dataCenterLinkStatus: String = IOTConstants.NULL_KEY, //数据中心连接状态 "0,0,0,0"
    @Json(name = "dataCenterPlatform")
    val dataCenterPlatformType: String = IOTConstants.NULL_KEY, //数据中心连接平台类型 "2,7,0,0"
    @Json(name = "extern_volt")
    val externalVoltage: String = IOTConstants.NULL_KEY, //外部电压  float  "0.00"
    @Json(name = "solar_volt")
    val solarVoltage: String = IOTConstants.NULL_KEY, //太阳能光伏板电压  float  "0.00"
    @Json(name = "bat_volt")
    val batteryVoltage: String = IOTConstants.NULL_KEY, //电池电压 float  "8.14"
    @Json(name = "bat_cap")
    val batteryCapacity: String = IOTConstants.NULL_KEY, //电池电量  int   0 - 100
    @Json(name = "bat_temp")
    val batteryTemp: String = IOTConstants.NULL_KEY, //电池温度  "28.15"
    @Json(name = "bat_sta")
    val batteryStatus: String = IOTConstants.NULL_KEY, //电池充放状态   0: 放电中  1: 充电中 -3：电池异常
    @Json(name = "bat_health")
    val batteryHealth: String = IOTConstants.NULL_KEY, //电池健康  "100"
    @Json(name = "inside_temp")
    val internalTemp: String = IOTConstants.NULL_KEY, //内部温度  "30.47"
    @Json(name = "inside_hum")
    val internalHumidity: String = IOTConstants.NULL_KEY, //内部湿度  "70.4"

)