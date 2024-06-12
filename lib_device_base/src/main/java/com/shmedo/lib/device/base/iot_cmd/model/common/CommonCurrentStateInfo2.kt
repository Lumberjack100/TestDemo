package com.shmedo.lib.device.base.iot_cmd.model.common

import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe
 *
 * 创建时间:  1/19/21
 *
 * 描述：
 */
@JsonClass(generateAdapter = true)
data class CommonCurrentStateInfo2(
    var sn: String = IOTConstants.NULL_KEY, //设备SN号
    var productDate: String = IOTConstants.NULL_KEY, //生产日期 20240228
    var rttVersion: String = IOTConstants.NULL_KEY, //RTT操作系统版本 4.1.0
    var hardwareVersion: String = IOTConstants.NULL_KEY, //硬件版本 V10
    var firmwareVersion: String = IOTConstants.NULL_KEY, //固件版本 1.0.0_M19
    var signal: String = IOTConstants.NULL_KEY, //4G信号强度
    var csq: String = IOTConstants.NULL_KEY, //4G信号强度
    @Json(name = "4g")
    var _4g: String = IOTConstants.NULL_KEY, //状态  OK/FAIL  OK，状态正常；FAIL，状态异常
    var scl: String = IOTConstants.NULL_KEY, //倾角加速度状态 OK/FAIL
    var lora: String = IOTConstants.NULL_KEY, // 状态 OK/FAIL
    var loraVersion: String = IOTConstants.NULL_KEY, //lora模块版本
    var bt: String = IOTConstants.NULL_KEY, //蓝牙状态 OK/FAIL
    var btVersion: String = IOTConstants.NULL_KEY, //蓝牙模块版本
    var ld: String = IOTConstants.NULL_KEY, //雷达状态 OK/FAIL
    var radio: String = IOTConstants.NULL_KEY, //电台状态 OK/FAIL
    var radioVersion: String = IOTConstants.NULL_KEY, //电台模块版本
    var radioEUI: String = IOTConstants.NULL_KEY, //电台EUI-ID
    var cam: String = IOTConstants.NULL_KEY, //相机状态 OK/FAIL
    var gnss: String = IOTConstants.NULL_KEY, //gnss状态  OK/FAIL
    var gnssType: String = IOTConstants.NULL_KEY, //gnss板卡类型 TAU1312：华大  UM960L：和芯星通 K823：司南
    var gnssVersion: String = IOTConstants.NULL_KEY, //gnss版本 R4.10Build11147
    var adc: String = IOTConstants.NULL_KEY, //电压采集功能状态  OK/FAIL
    var emmc: String = IOTConstants.NULL_KEY, //emmc状态  OK/FAIL
    var sht21: String = IOTConstants.NULL_KEY, //温湿度状态  OK/FAIL
    var qmc5883: String = IOTConstants.NULL_KEY, //磁力状态  OK/FAIL
    var battery: String = IOTConstants.NULL_KEY, //电池状态  OK/FAIL
    var simCard: String = IOTConstants.NULL_KEY, //sim卡状态  OK/FAIL
    var flash: String = IOTConstants.NULL_KEY, // 外部存储芯片状态 OK/FAIL
    var fram: String = IOTConstants.NULL_KEY, // fram状态 OK/FAIL
    var rtc: String = IOTConstants.NULL_KEY, //rtc状态  OK/FAIL
    var mdCenterSta: String = IOTConstants.NULL_KEY, //米度数据中心连接状态  OK/FAIL
    var amsCenterSta: String = IOTConstants.NULL_KEY, //AMS数据中心连接状态  OK/FAIL
    var starSearchSta: String = IOTConstants.NULL_KEY, //搜星状态 OK/FAIL（搜星数量小于4 -> FAIL）
    var solarInsertSta: String = IOTConstants.NULL_KEY, //太阳能控制检验 OK/FAIL
    var extPowerVolt: String = IOTConstants.NULL_KEY, //外部电压  12.0  供电电压是设备自检出来的电压值
    var batPowerVolt: String = IOTConstants.NULL_KEY, //电池电压  3.7
    var emmcStorage: String = IOTConstants.NULL_KEY, //emmc存储量 14910.00
    var emmcFree: String = IOTConstants.NULL_KEY, //emmc剩余量 11566.50
    var temp: String = IOTConstants.NULL_KEY, //温度
    var humidity: String = IOTConstants.NULL_KEY, //湿度
    var insideTemp: String = IOTConstants.NULL_KEY, //内部温度
    var insideHum: String = IOTConstants.NULL_KEY, //内部湿度
    var height: String = IOTConstants.NULL_KEY, //安装高度
    var ldValue: String = IOTConstants.NULL_KEY, //雷达测量值
    var uptime: String = "--", //设备本次运行时间
    var mag: String = IOTConstants.NULL_KEY, //磁力方向
    var ccid: String = IOTConstants.NULL_KEY, //CCID
    var imei: String = IOTConstants.NULL_KEY, //IMEI
    var imsi: String = IOTConstants.NULL_KEY, //IMSI
    var dataCenterStatus: String = IOTConstants.NULL_KEY, //数据中心连接状态 [0,1,1]
    var dataCenterUseSta: String = IOTConstants.NULL_KEY, //数据中心启用状态 [0,1,1]
    var starSearchNum: String = IOTConstants.NULL_KEY, //搜星数
    var location: String = IOTConstants.NULL_KEY, //坐标 12135.648003E, 3112.752093N
    var worktime: String = IOTConstants.NULL_KEY,//运行时间
    var pixx: String = IOTConstants.NULL_KEY,//图片水平分辨率
    var pixy: String = IOTConstants.NULL_KEY,//图片垂直分辨率
    var capture_level: String = IOTConstants.NULL_KEY,//触发抓拍级别
)