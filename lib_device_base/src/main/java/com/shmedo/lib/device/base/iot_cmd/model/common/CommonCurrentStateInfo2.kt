package com.shmedo.lib.device.base.iot_cmd.model.common

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
    var sn: String = "", //设备SN号
    var productDate: String = "", //生产日期 20240228
    var rttVersion: String = "", //RTT操作系统版本 4.1.0
    var hardwareVersion: String = "", //硬件版本 V10
    var firmwareVersion: String = "", //固件版本 1.0.0_M19
    var signal: Int = 0, //4G信号强度
    @Json(name = "4g")
    var _4g: String = "", //状态  OK/FAIL  OK，状态正常；FAIL，状态异常
    var scl: String = "", //倾角加速度状态 OK/FAIL
    var lora: String = "", // 状态 OK/FAIL
    var loraVersion: String = "", //lora模块版本
    var bt: String = "", //蓝牙状态 OK/FAIL
    var btVersion: String = "", //蓝牙模块版本
    var ld: String = "", //雷达状态 OK/FAIL
    var radio: String = "", //电台模块状态 OK/FAIL
    var radioVersion: String = "", //电台模块版本
    var radioEUI: String = "", //电台EUI-ID
    var cam: String = "", //相机状态 OK/FAIL
    var gnss: String = "", //gnss状态  OK/FAIL
    var gnssType: String = "", //gnss板卡类型 TAU1312：华大  UM960L：和芯星通 K823：司南
    var gnssVersion: String = "", //gnss版本 R4.10Build11147
    var adc: String = "", //电压采集功能状态  OK/FAIL
    var emmc: String = "", //emmc状态  OK/FAIL
    var sht21: String = "", //温湿度状态  OK/FAIL
    var qmc5883: String = "", //磁力状态  OK/FAIL
    var battery: String = "", //电池状态  OK/FAIL
    var simCard: String = "", //sim卡状态  OK/FAIL
    var flash: String = "", // 外部存储芯片状态 OK/FAIL
    var fram: String = "", // fram状态 OK/FAIL
    var rtc: String = "", //rtc状态  OK/FAIL
    var mdCenterSta: String = "", //米度数据中心连接状态  OK/FAIL
    var amsCenterSta: String = "", //AMS数据中心连接状态  OK/FAIL
    var starSearchSta: String = "", //搜星状态 OK/FAIL（搜星数量小于4 -> FAIL）
    var solarInsertSta: String = "", //太阳能控制检验 OK/FAIL
    var extPowerVolt: String = "", //外部电压  12.0
    var batPowerVolt: String = "", //电池电压  3.7
    var emmcStorage: String = "", //emmc存储量 14910.00
    var emmcFree: String = "", //emmc剩余量 11566.50
    var temp: String = "", //温度
    var humidity: String = "", //湿度
    var insideTemp: String = "", //内部温度
    var insideHum: String = "", //内部湿度
    var height: String = "", //安装高度
    var ldValue: String = "", //雷达测量值
    var uptime: String = "--", //设备本次运行时间
    var mag: String = "", //磁力方向
    var ccid: String = "", //CCID
    var imei: String = "", //IMEI
    var imsi: String = "", //IMSI
    var datacenterStatus: String = "", //数据中心状态 [0,1,1]
    var starSearchNum: String = "", //搜星数
    var location: String = "", //坐标 12135.648003E, 3112.752093N
)