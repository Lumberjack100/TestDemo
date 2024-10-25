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
    @Json(name = "device_type")
    val deviceType: String = IOTConstants.NULL_KEY, //设备型号  M50
    val sn: String = IOTConstants.NULL_KEY, //设备SN号
    @Json(name = "device_status")
    val deviceStatus: String = IOTConstants.NULL_KEY, //设备状态(0：正常  -2:告警  -3：设备故障)
    @Json(name = "dev_warn")
    val deviceWarn: Map<String, String>? = null, //设备告警信息
    @Json(name = "dev_error")
    val deviceError: Map<String, String>? = null, //设备故障信息
    @Json(name = "sw_version")
    val firmwareVersion: String = IOTConstants.NULL_KEY, //固件版本 2.0.1-M2
    @Json(name = "sw_date")
    val firmwareDate: String = IOTConstants.NULL_KEY, //固件日期  2024.08.27
    @Json(name = "boot_code")
    val bootCode: String = IOTConstants.NULL_KEY, //启动代码  2000
    @Json(name = "boot_time")
    val bootTime: String = IOTConstants.NULL_KEY, //启动时间   2024.08.29 17:58:32
    @Json(name = "run_time")
    val runTime: String = IOTConstants.NULL_KEY, //本次运行时间(单位：秒)  55832
    @Json(name = "total_run_time")
    val totalRunTime: String = IOTConstants.NULL_KEY, //累计运行时间  单位：秒 56202
    @Json(name = "work_mode")
    val workMode: String = IOTConstants.NULL_KEY, //工作模式(1:基站  2:测站)
    @Json(name = "rept_mode")
    val reportMode: String = IOTConstants.NULL_KEY, //上报模式(0:常在线  1:低功耗)
    @Json(name = "cap_freq")
    val captureFrequency: String = IOTConstants.NULL_KEY, //抓拍频率(单位：分钟/次)  120
    @Json(name = "emmc_free")
    var emmcFree: String = IOTConstants.NULL_KEY, //可用空间  14.6GB
    @Json(name = "emmc_storage")
    var emmcStorage: String = IOTConstants.NULL_KEY,//总空间  18.6GB
    @Json(name = "mobile_net")
    val mobileNet: String = IOTConstants.NULL_KEY, //移动网络(0：未连接 1：已连接)
    @Json(name = "net_type")
    val netType: String = IOTConstants.NULL_KEY, //网络类型  4G
    val operator: String = IOTConstants.NULL_KEY, //运营商(CMCC(中国移动)、CU(中国联通)、CT(中国电信))
    val csq: String = IOTConstants.NULL_KEY, //信号强度  -51
    val imei: String = IOTConstants.NULL_KEY,   //865019074780812
    val ccid: String = IOTConstants.NULL_KEY, //898608162623D0102610
    val location: String = IOTConstants.NULL_KEY, //10925.709961E,3111.139160N,33.0862
    @Json(name = "radio_use")
    val radioEnableStatus: String = IOTConstants.NULL_KEY, //电台(0：未启用  1：启用)
    val bt_connected: String = IOTConstants.NULL_KEY, //蓝牙(0：未连接 1：已连接)
    val dataCenterStatus: String = IOTConstants.NULL_KEY, //数据链路连接状态  "0,0,0,0"
    @Json(name = "dataCenterPlatform")
    val dataCenterPlatformType: String = IOTConstants.NULL_KEY, //数据链路连接平台类型 "2,7,0,0"
    @Json(name = "ext_power_volt")
    val externalVoltage: String = IOTConstants.NULL_KEY, //外部电压   0.00
    @Json(name = "solar_volt")
    val solarVoltage: String = IOTConstants.NULL_KEY, //太阳能光伏板电压   "0.00"
    @Json(name = "temp")
    val internalTemp: String = IOTConstants.NULL_KEY, //内部温度  30.47
    @Json(name = "humidity")
    val internalHumidity: String = IOTConstants.NULL_KEY, //内部湿度  70.4
    val battery: List<BatteryInfo>? = arrayListOf(),
    var gnss: String = IOTConstants.NULL_KEY, //GNSS 模块版本
    var scl: String = IOTConstants.NULL_KEY, //倾角加速度状态 OK/FAIL
    @Json(name = "4g")
    var _4g: String = IOTConstants.NULL_KEY, //状态  OK/FAIL  OK，状态正常；FAIL，状态异常
    var bt: String = IOTConstants.NULL_KEY, //蓝牙状态 OK/FAIL
    var lora: String = IOTConstants.NULL_KEY, // 状态 OK/FAIL
    var sd: String = IOTConstants.NULL_KEY, //emmc状态  OK/FAIL
    var sht21: String = IOTConstants.NULL_KEY, //温湿度状态  OK/FAIL
)

@JsonClass(generateAdapter = true)
data class BatteryInfo(
    @Json(name = "battery_volt")
    val batteryVoltage: String = IOTConstants.NULL_KEY, //电池电压 float  "8.14"
    @Json(name = "battery_cap")
    val batteryCapacity: String = IOTConstants.NULL_KEY, //电池电量  int   0 - 100
    @Json(name = "battery_status")
    val batteryStatus: String = IOTConstants.NULL_KEY, //电池充放状态   0: 放电中  1: 充电中 -3：电池异常
    @Json(name = "battery_temp")
    val batteryTemp: String = IOTConstants.NULL_KEY, //电池温度  "28.15"
    @Json(name = "battery_health")
    val batteryHealth: String = IOTConstants.NULL_KEY, //电池健康  "100"
)