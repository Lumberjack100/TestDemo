package com.shmedo.lib.cmd.base.iot_cmd.model.ml101

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/01/21
 * 描述：ML101/MS101 一体化通信终端设备自检状态信息数据模型
 *
 * 对应 MD_GET_DEVICE_STATUS (md_getdevicesta) 指令的响应数据结构
 *
 * 产品说明：
 * - ML101 (ProductType.C_L_1): 一体化 LoRa 通信终端
 * - MS101 (ProductType.C_S_2): 一体化卫星通信终端
 *
 * 指令格式：
 * 发送：$cmd=md_getdevicesta&limittime=60
 *
 * 应答示例 (state 字段内容)：
 * ```json
 * {
 *   "sn": "25A1436CS1",
 *   "productDate": "251008",
 *   "rttVersion": "4.1.0",
 *   "hardwareVersion": "2.1",
 *   "firmwareVersion": "2.0.5-M2-25092415",
 *   "lora": "FAIL",
 *   "radio": "OK",
 *   "bt": "OK",
 *   "gnss": "OK",
 *   "satcom": "FAIL",
 *   "adc": "OK",
 *   "sht21": "OK",
 *   "flash": "OK",
 *   "rtc": "OK",
 *   "sat_sn": "00013648",
 *   "sat_csq": "-66",
 *   "sat_cesq": "-5",
 *   "extPowerVolt": 11.0,
 *   "temp": 17.8,
 *   "humidity": 45.3,
 *   "gnssType": "HD9510B.91045df54.01.0.ba0096ab",
 *   "starSearchNum": 19,
 *   "gnssVersion": "4.01.0.ba0096ab",
 *   "radioNetSta": "FAIL",
 *   "location": "114.247246, 30.576454"
 * }
 * ```
 *
 * 字段说明：
 * - sn: 设备 SN 号
 * - productDate: 生产日期（格式: YYMMDD，如 251008 表示 2025年10月08日）
 * - rttVersion: RTT 版本
 * - hardwareVersion: 硬件版本
 * - firmwareVersion: 固件版本
 * - lora: LoRa 模块状态（OK/FAIL）
 * - radio: 电台模块状态（OK/FAIL）
 * - bt: 蓝牙模块状态（OK/FAIL）
 * - gnss: GNSS 模块状态（OK/FAIL）
 * - satcom: 卫星通信模块状态（OK/FAIL）
 * - adc: 电压采集功能状态（OK/FAIL）
 * - sht21: 温湿度传感器状态（OK/FAIL）
 * - flash: Flash 存储状态（OK/FAIL）
 * - rtc: RTC 时钟状态（OK/FAIL）
 * - sat_sn: 卫星模组序列号
 * - sat_csq: 卫星通信信号强度
 * - sat_cesq: 卫星信号质量
 * - extPowerVolt: 外部电压（单位 V）
 * - temp: 温度（单位 ℃）
 * - humidity: 湿度（单位 RH%）
 * - gnssType: GNSS 模组型号
 * - starSearchNum: 搜星数量
 * - gnssVersion: GNSS 模组固件版本号
 * - radioNetSta: 电台入网状态（OK/FAIL）
 * - location: 经纬度坐标
 */
@JsonClass(generateAdapter = true)
data class ML101StatusSelfCheckInfo(
    // ==================== 设备基本信息字段 ====================

    /** 设备 SN 号 */
    val sn: String = IOTConstants.NULL_KEY,

    /** 生产日期，格式：YYMMDD，如 251008 表示 2025年10月08日 */
    val productDate: String = IOTConstants.NULL_KEY,

    /** RTT 版本 */
    val rttVersion: String = IOTConstants.NULL_KEY,

    /** 硬件版本 */
    val hardwareVersion: String = IOTConstants.NULL_KEY,

    /** 固件版本 */
    val firmwareVersion: String = IOTConstants.NULL_KEY,

    // ==================== 模块状态字段 ====================

    /** LoRa 模块状态：OK 表示正常，FAIL 表示故障 */
    val lora: String = IOTConstants.NULL_KEY,

    /** 电台模块状态：OK 表示正常，FAIL 表示故障 */
    val radio: String = IOTConstants.NULL_KEY,

    /** 蓝牙模块状态：OK 表示正常，FAIL 表示故障 */
    val bt: String = IOTConstants.NULL_KEY,

    /** GNSS 模块状态：OK 表示正常，FAIL 表示故障 */
    val gnss: String = IOTConstants.NULL_KEY,

    /** 卫星通信模块状态：OK 表示正常，FAIL 表示故障 */
    val satcom: String = IOTConstants.NULL_KEY,

    /** 电压采集功能状态：OK 表示正常，FAIL 表示故障 */
    val adc: String = IOTConstants.NULL_KEY,

    /** 温湿度传感器状态：OK 表示正常，FAIL 表示故障 */
    val sht21: String = IOTConstants.NULL_KEY,

    /** Flash 存储状态：OK 表示正常，FAIL 表示故障 */
    val flash: String = IOTConstants.NULL_KEY,

    /** RTC 时钟状态：OK 表示正常，FAIL 表示故障 */
    val rtc: String = IOTConstants.NULL_KEY,

    // ==================== 电源和环境信息 ====================

    /** 外部电源电压，单位 V */
    val extPowerVolt: String = IOTConstants.NULL_KEY,

    /** 设备内部温度，单位 ℃ */
    val temp: Double = 0.0,

    /** 设备内部湿度，单位 RH% */
    val humidity: Double = 0.0,

    // ==================== 卫星通信信息 ====================

    /** 卫星模组序列号 */
    val sat_sn: String = IOTConstants.NULL_KEY,

    /** 卫星通信信号强度 */
    val sat_csq: String = IOTConstants.NULL_KEY,

    /** 卫星信号质量 */
    val sat_cesq: String = IOTConstants.NULL_KEY,

    // ==================== GNSS 信息 ====================

    /** GNSS 模组型号 */
    val gnssType: String = IOTConstants.NULL_KEY,

    /** 搜星数量 */
    val starSearchNum: Int = 0,

    /** GNSS 模组固件版本号 */
    val gnssVersion: String = IOTConstants.NULL_KEY,

    // ==================== 电台信息 ====================

    /** 电台入网状态：OK 表示正常，FAIL 表示故障 */
    val radioNetSta: String = IOTConstants.NULL_KEY,

    // ==================== 位置信息 ====================

    /** 经纬度坐标，格式: "经度, 纬度" */
    val location: String = IOTConstants.NULL_KEY
)

