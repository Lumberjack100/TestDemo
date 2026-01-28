package com.shmedo.lib.cmd.base.iot_cmd.model.ml101

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.model.AttachDataItemBean
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/01/21
 * 描述：ML101/MS101 一体化通信终端设备状态信息数据模型
 *
 * 对应 QUERY_DEVICE_STATUS (getstatus) 指令的响应数据结构
 *
 * 产品说明：
 * - ML101 (ProductType.C_L_1): 一体化 LoRa 通信终端
 * - MS101 (ProductType.C_S_2): 一体化卫星通信终端
 *
 * 指令格式：
 * 发送：$cmd=getstatus
 *
 * 应答示例 (state 字段内容)：
 * ```json
 * {
 *   "ext_power_volt": 14.838,
 *   "inner_power_volt": 0.00,
 *   "temp": 45.7,
 *   "humidity": 12.4,
 *   "temp_out": 0.0,
 *   "humidity_out": 0.0,
 *   "4g_signal": -59.0,
 *   "nb_signal": 0.0,
 *   "bd_signal": 0.0,
 *   "sw_version": "1.0.0-M1",
 *   "location": "31.2124807,121.5947501",
 *   "solar_volt": 0.0,
 *   "battery_volt": 0.0,
 *   "supply_power": 0.0,
 *   "consume_power": 0.0,
 *   "work_current": 0.0,
 *   "device_type": "ML101",
 *   "sn": "261T001CL1",
 *   "boot_code": "2400",
 *   "product_time": "20260120",
 *   "starNum": 0,
 *   "attach_data": [
 *     {"key": "gnss", "value": "OK"},
 *     {"key": "radio", "value": "OK"},
 *     {"key": "lora", "value": "OK"},
 *     {"key": "bt", "value": "OK"},
 *     {"key": "satcom", "value": "OK"},
 *     {"key": "sat_sn", "value": "00013648"},
 *     {"key": "sat_cesq", "value": "-128"},
 *     {"key": "sat_csq", "value": "-57"},
 *     {"key": "BetaVersion", "value": "5"},
 *     {"key": "hw_version", "value": "1.0"}
 *   ]
 * }
 * ```
 *
 * 字段说明：
 * - device_type: 设备类型，如 "ML101"
 * - sn: 设备 SN 号
 * - sw_version: 固件版本
 * - product_time: 生产日期 (格式: YYYYMMDD，如 20260120)
 * - attach_data.hw_version: 硬件版本
 */
@JsonClass(generateAdapter = true)
data class ML101DeviceStatusInfo(
    // ==================== 设备基本信息字段 ====================

    /** 设备类型，如 "ML101" */
    val device_type: String = IOTConstants.NULL_KEY,

    /** 设备 SN 号 */
    val sn: String = IOTConstants.NULL_KEY,

    /** 固件版本，如 "1.0.0-M1" */
    val sw_version: String = IOTConstants.NULL_KEY,

    /** 生产日期，格式：YYYYMMDD，如 20260120 */
    val product_time: String = IOTConstants.NULL_KEY,

    /** 启动码 */
    val boot_code: String = IOTConstants.NULL_KEY,

    // ==================== 电源信息字段 ====================

    /** 外部电源电压，单位 V */
    val ext_power_volt: Double = 0.0,

    /** 内部电源电压，单位 V */
    val inner_power_volt: Double = 0.0,

    /** 太阳能电压，单位 V */
    val solar_volt: Double = 0.0,

    /** 电池电压，单位 V */
    val battery_volt: Double = 0.0,

    /** 供电功率，单位 W */
    val supply_power: Double = 0.0,

    /** 消耗功率，单位 W */
    val consume_power: Double = 0.0,

    /** 工作电流，单位 A */
    val work_current: Double = 0.0,

    // ==================== 环境信息字段 ====================

    /** 设备内部温度，单位 ℃ */
    val temp: Double = 0.0,

    /** 设备内部湿度，单位 RH% */
    val humidity: Double = 0.0,

    /** 外部温度，单位 ℃ */
    val temp_out: Double = 0.0,

    /** 外部湿度，单位 RH% */
    val humidity_out: Double = 0.0,

    // ==================== 通信信息字段 ====================

    /** 4G 信号强度，单位 dBm */
    @Json(name = "4g_signal")
    val _4g_signal: Double = 0.0,

    /** NB-IoT 信号强度，单位 dBm */
    val nb_signal: Double = 0.0,

    /** 北斗信号强度，单位 dBm */
    val bd_signal: Double = 0.0,

    /** 设备位置坐标，格式: "纬度,经度" */
    val location: String = IOTConstants.NULL_KEY,

    /** 卫星数量 */
    val starNum: Int = 0,

    // ==================== 扩展数据字段 ====================

    /** 扩展状态数据列表 */
    val attach_data: List<AttachDataItemBean>? = arrayListOf()
) {
    // ==================== 辅助方法 ====================

    /**
     * 从 attach_data 中获取指定 key 的值
     *
     * @param key 要查找的键名
     * @return 对应的值字符串，如果不存在则返回占位符
     */
    fun getAttachValue(key: String): String {
        return attach_data?.find { it.key == key }?.value?.toString()
            ?: AppContants.PLACE_HOLDER_VALUE
    }

    /**
     * 获取硬件版本
     * 数据来源：attach_data.hw_version
     */
    val hardwareVersion: String
        get() = getAttachValue("hw_version")

    /**
     * 获取 GNSS 模块状态
     * 数据来源：attach_data.gnss
     */
    val gnssStatus: String
        get() = getAttachValue("gnss")

    /**
     * 获取电台模块状态
     * 数据来源：attach_data.radio
     */
    val radioStatus: String
        get() = getAttachValue("radio")

    /**
     * 获取 LoRa 模块状态
     * 数据来源：attach_data.lora
     */
    val loraStatus: String
        get() = getAttachValue("lora")

    /**
     * 获取蓝牙模块状态
     * 数据来源：attach_data.bt
     */
    val btStatus: String
        get() = getAttachValue("bt")

    /**
     * 获取卫通模块状态
     * 数据来源：attach_data.satcom
     */
    val satcomStatus: String
        get() = getAttachValue("satcom")

    /**
     * 获取卫通模块 SN
     * 数据来源：attach_data.sat_sn
     */
    val satSn: String
        get() = getAttachValue("sat_sn")

    /**
     * 获取卫通信号强度 CESQ
     * 数据来源：attach_data.sat_cesq
     */
    val satCesq: String
        get() = getAttachValue("sat_cesq")

    /**
     * 获取卫通信号强度 CSQ
     * 数据来源：attach_data.sat_csq
     */
    val satCsq: String
        get() = getAttachValue("sat_csq")

    /**
     * 获取 Beta 版本号
     * 数据来源：attach_data.BetaVersion
     */
    val betaVersion: String
        get() = getAttachValue("BetaVersion")

    /**
     * 格式化生产日期
     * 将 YYYYMMDD 格式转为 YYYY-MM-DD 格式
     *
     * @return 格式化后的日期字符串，如：2026-01-20
     *         如果原始数据无效则返回占位符
     */
    fun getFormattedProductTime(): String {
        // 检查是否为有效数据
        if (product_time == IOTConstants.NULL_KEY || product_time.length != 8) {
            return AppContants.PLACE_HOLDER_VALUE
        }

        return try {
            val year = product_time.take(4)
            val month = product_time.substring(4, 6)
            val day = product_time.substring(6, 8)

            "$year-$month-$day"
        } catch (e: Exception) {
            AppContants.PLACE_HOLDER_VALUE
        }
    }

    /**
     * 获取格式化的外部电源电压
     * 保留两位小数
     *
     * @return 格式化后的电压值，单位 V
     */
    fun getFormattedExtPowerVolt(): String {
        return String.format("%.2f", ext_power_volt)
    }

    /**
     * 获取格式化的内部温度
     * 保留一位小数
     *
     * @return 格式化后的温度值，单位 ℃
     */
    fun getFormattedTemp(): String {
        return String.format("%.1f", temp)
    }

    /**
     * 获取格式化的内部湿度
     * 保留一位小数
     *
     * @return 格式化后的湿度值，单位 RH%
     */
    fun getFormattedHumidity(): String {
        return String.format("%.1f", humidity)
    }

    /**
     * 获取格式化的 4G 信号强度
     *
     * @return 格式化后的信号强度值，单位 dBm
     */
    fun getFormatted4GSignal(): String {
        return String.format("%.0f", _4g_signal)
    }
}
