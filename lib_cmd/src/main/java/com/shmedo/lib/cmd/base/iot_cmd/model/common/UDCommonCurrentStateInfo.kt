package com.shmedo.lib.cmd.base.iot_cmd.model.common

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
data class UDCommonCurrentStateInfo(
    @Json(name = "dev_type")
    val deviceType: String = IOTConstants.NULL_KEY, //设备型号
    @Json(name = "dev_sn")
    val sn: String = IOTConstants.NULL_KEY, //设备SN号
    @Json(name = "dev_sta")
    val deviceStatus: String = IOTConstants.NULL_KEY, //设备状态 0：正常  -3：异常
    @Json(name = "sw_version")
    val firmwareVersion: String = IOTConstants.NULL_KEY, //固件版本 V10
    @Json(name = "sw_date")
    val firmwareDate: String = IOTConstants.NULL_KEY, //固件日期
    @Json(name = "boot_code")
    val bootCode: String = IOTConstants.NULL_KEY, //启动代码
    @Json(name = "boot_time")
    val bootTime: String = IOTConstants.NULL_KEY, //启动时间
    @Json(name = "run_time")
    val runTime: String = IOTConstants.NULL_KEY, //本次运行时间
    @Json(name = "total_run_time")
    val totalRunTime: String = IOTConstants.NULL_KEY, //累计运行时间
    @Json(name = "rept_mode")
    val reportMode: String = IOTConstants.NULL_KEY, //上报模式
    @Json(name = "rept_sta")
    val reportStatus: String = IOTConstants.NULL_KEY, //上报状态
    @Json(name = "rept_freq")
    val reportFrequency: String = IOTConstants.NULL_KEY, //上报频率
    @Json(name = "warning_switch")
    val levelFourWarningEnabled: String = IOTConstants.NULL_KEY, //四级预警启用 0：启用  1：不启用
    @Json(name = "mobile_net")
    val mobileNet: String = IOTConstants.NULL_KEY, //移动网络（连接与否） 0：未连接 1：已连接
    @Json(name = "net_type")
    val netType: String = IOTConstants.NULL_KEY, //网络类型
    val operator: String = IOTConstants.NULL_KEY, //运营商
    val csq: String = IOTConstants.NULL_KEY, //信号强度
    val imei: String = IOTConstants.NULL_KEY,
    val iccid: String = IOTConstants.NULL_KEY, //
    val lora_use: String = IOTConstants.NULL_KEY, //电台（启用/未启用）0：未启用  1：启用
    val bt_connected: String = IOTConstants.NULL_KEY, //蓝牙（连接/未连接） 0：未连接 1：已连接
    val data_center1: String = IOTConstants.NULL_KEY, //
    val data_center2: String = IOTConstants.NULL_KEY, //
    val data_center3: String = IOTConstants.NULL_KEY, //
    val data_center4: String = IOTConstants.NULL_KEY, //
    @Json(name = "bat_cap")
    val batteryCapacity: String = IOTConstants.NULL_KEY, //电池电量  int 0 - 100
    @Json(name = "bat_volt")
    val batteryVoltage: String = IOTConstants.NULL_KEY, //电池电压 float
    @Json(name = "extern_volt")
    val externalVoltage: String = IOTConstants.NULL_KEY, //外部电压
    @Json(name = "bat_sta")
    val batteryStatus: String = IOTConstants.NULL_KEY, //电池充放状态
    @Json(name = "bat_health")
    val batteryHealth: String = IOTConstants.NULL_KEY, //电池健康
    @Json(name = "bat_temp")
    val batteryTemp: String = IOTConstants.NULL_KEY, //电池温度
    @Json(name = "inside_hum")
    val internalHumidity: String = IOTConstants.NULL_KEY, //内部湿度
    @Json(name = "inside_temp")
    val internalTemp: String = IOTConstants.NULL_KEY, //内部温度
    @Json(name = "ld_sta")
    val ldStatus: String = IOTConstants.NULL_KEY, //雷达状态 0:正常 -2:数据异常 -3：模块异常
    @Json(name = "alt")
    val altitude: String = IOTConstants.NULL_KEY, //海拔高度
    @Json(name = "cam_sta")
    val cameraStatus: String = IOTConstants.NULL_KEY, //摄像头状态  0：正常 -3：异常
    var pixx: String = IOTConstants.NULL_KEY,//图片水平分辨率
    var pixy: String = IOTConstants.NULL_KEY,//图片垂直分辨率
    @Json(name = "adxl_sta")
    val accelerometerStatus: String = IOTConstants.NULL_KEY, //加速度计 -3：模块异常  -2：数据异常 0：正常
    @Json(name = "utc_time")
    val utcTime: String = IOTConstants.NULL_KEY, //UTC时间
    @Json(name = "lng")
    val longitude: String = IOTConstants.NULL_KEY, //经度
    @Json(name = "lat")
    val latitude: String = IOTConstants.NULL_KEY, //纬度
    @Json(name = "ld_module_gap")
    val radarMeasureInterval: String = IOTConstants.NULL_KEY, //雷达测量间隔
    @Json(name = "angle_threshol")
    val installAngleOffsetThreshold: String = IOTConstants.NULL_KEY, //安装角度偏移阈值
    @Json(name = "cam_module_gap")
    val captureFrequency: String = IOTConstants.NULL_KEY, //抓拍频率
    @Json(name = "alt_get_mode")
    val altitudeMeasureMode: String = IOTConstants.NULL_KEY, //海拔高度获取模式  0：自动   1：手动
)