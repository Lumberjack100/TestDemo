package com.shmedo.lib.cmd.base.iot_cmd.model.mg301

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/01/16
 * 描述：MG301 多模融合通讯数据网关设备状态信息数据模型
 *
 * 对应 MD_GET_DEVICE_STATUS (md_getdevicesta) 指令的响应数据结构
 *
 * 指令格式：
 * 发送：$cmd=md_getdevicesta&limittime=60
 *
 * 应答示例 (state 字段内容)：
 * ```json
 * {
 *   "sn": "25T0010CG3",
 *   "productDate": "20260106",
 *   "hardwareVersion": "1.0.0",
 *   "firmwareVersion": "1.0.2.2",
 *   "4g": "OK",
 *   "lora": "OK",
 *   "bt": "OK",
 *   "satcom": "OK",
 *   "adc": "OK",
 *   "emmc": "OK",
 *   "sht21": "OK",
 *   "simCard": "OK",
 *   "rtc": "OK",
 *   "extPowerVolt": "12.88",
 *   "emmcStorage": 14915.42,
 *   "emmcFree": 13517.75,
 *   "temp": 0,
 *   "humidity": 0,
 *   "sat_csq": -128,
 *   "sat_sn": "00013648",
 *   "csq": "-51",
 *   "bd_signal": "",
 *   "ccid": "89860816262320016079",
 *   "imei": "863284049855931",
 *   "imsi": "863284049855931",
 *   "bdsim": "",
 *   "mdCenterSta": "OK",
 *   "location": "0,0"
 * }
 * ```
 *
 * 字段说明：
 * - sn: 设备 SN 号
 * - firmwareVersion: 固件版本
 * - hardwareVersion: 硬件版本
 * - productDate: 生产日期 (格式: YYYYMMDD，如 20260106)
 * - emmcStorage: 内部存储总容量 (单位: MB)
 * - emmcFree: 内部存储剩余容量 (单位: MB)
 */
@JsonClass(generateAdapter = true)
data class MG301DeviceStatusInfo(
    // ==================== 设备基本信息字段 ====================

    /** 设备 SN 号 */
    val sn: String = IOTConstants.NULL_KEY,

    /** 固件版本 */
    val firmwareVersion: String = IOTConstants.NULL_KEY,

    /** 硬件版本 */
    val hardwareVersion: String = IOTConstants.NULL_KEY,

    /** 生产日期，格式：YYYYMMDD，如 20260106 */
    val productDate: String = IOTConstants.NULL_KEY,

    // ==================== 存储信息字段 ====================

    /** 内部存储总容量，单位 MB */
    val emmcStorage: Double = 0.0,

    /** 内部存储剩余容量，单位 MB */
    val emmcFree: Double = 0.0,

    // ==================== 模块状态字段 ====================

    /** 4G 模块状态：OK 表示正常 */
    val `4g`: String = IOTConstants.NULL_KEY,

    /** LoRa 模块状态：OK 表示正常 */
    val lora: String = IOTConstants.NULL_KEY,

    /** 蓝牙模块状态：OK 表示正常 */
    val bt: String = IOTConstants.NULL_KEY,

    /** 卫通模块状态：OK 表示正常 */
    val satcom: String = IOTConstants.NULL_KEY,

    /** ADC 模块状态：OK 表示正常 */
    val adc: String = IOTConstants.NULL_KEY,

    /** eMMC 存储状态：OK 表示正常 */
    val emmc: String = IOTConstants.NULL_KEY,

    /** 温湿度传感器状态：OK 表示正常 */
    val sht21: String = IOTConstants.NULL_KEY,

    /** SIM 卡状态：OK 表示正常 */
    val simCard: String = IOTConstants.NULL_KEY,

    /** RTC 时钟状态：OK 表示正常 */
    val rtc: String = IOTConstants.NULL_KEY,

    // ==================== 电源和环境信息 ====================

    /** 外部电源电压，单位 V */
    val extPowerVolt: String = IOTConstants.NULL_KEY,

    /** 设备内部温度，单位 ℃ */
    val temp: Double = 0.0,

    /** 设备内部湿度，单位 RH% */
    val humidity: Double = 0.0,

    // ==================== 通信信息字段 ====================

    /** 卫通信号强度 */
    val sat_csq: Int = 0,

    /** 卫通模块 SN */
    val sat_sn: String = IOTConstants.NULL_KEY,

    /** 4G 信号强度 (CSQ 值) */
    val csq: String = IOTConstants.NULL_KEY,

    /** 北斗信号强度 */
    val bd_signal: String = IOTConstants.NULL_KEY,

    /** SIM 卡 CCID */
    val ccid: String = IOTConstants.NULL_KEY,

    /** 4G 模块 IMEI */
    val imei: String = IOTConstants.NULL_KEY,

    /** 4G 模块 IMSI */
    val imsi: String = IOTConstants.NULL_KEY,

    /** 北斗 SIM 卡 */
    val bdsim: String = IOTConstants.NULL_KEY,

    /** 数据中心连接状态 */
    val mdCenterSta: String = IOTConstants.NULL_KEY,

    /** 设备位置坐标 */
    val location: String = IOTConstants.NULL_KEY
) {
    // ==================== 辅助方法 ====================

    /**
     * 格式化生产日期
     * 将 YYYYMMDD 格式转为 YYYY-MM-DD 格式
     *
     * @return 格式化后的日期字符串，如：2026-01-06
     *         如果原始数据无效则返回占位符
     */
    fun getFormattedProductDate(): String {
        // 检查是否为有效数据
        if (productDate == IOTConstants.NULL_KEY || productDate.length != 8) {
            return AppContants.PLACE_HOLDER_VALUE
        }

        return try {
            val year = productDate.take(4)
            val month = productDate.substring(4, 6)
            val day = productDate.substring(6, 8)

            "$year-$month-$day"
        } catch (e: Exception) {
            AppContants.PLACE_HOLDER_VALUE
        }
    }

    /**
     * 获取格式化的可用存储空间
     * 保留两位小数
     *
     * @return 格式化后的存储空间值，单位 MB
     */
    fun getFormattedEmmcFree(): String {
        return String.format("%.2f", emmcFree)
    }

    /**
     * 获取格式化的总存储空间
     * 保留两位小数
     *
     * @return 格式化后的存储空间值，单位 MB
     */
    fun getFormattedEmmcStorage(): String {
        return String.format("%.2f", emmcStorage)
    }
}
