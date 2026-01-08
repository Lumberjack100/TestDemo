package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.model.AttachDataItemBean
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/01/08
 * 描述：GT600 设备状态信息数据模型
 *
 * 对应 QUERY_DEVICE_STATUS (getstatus) 指令的响应数据结构
 *
 * 示例 JSON:
 * ```json
 * {
 *   "ext_power_volt": 12.281,
 *   "temp": 23.5,
 *   "humidity": 31.9,
 *   "attach_data": [
 *     {"key": "solar", "value": "0.00"},
 *     {"key": "LTEMod", "value": "OK"},
 *     {"key": "SIM", "value": "OK"},
 *     {"key": "Sata", "value": "42"},
 *     {"key": "MEMs", "value": "5[0]----16.40--0.36--73.64----276.88--6.23--941.05---OK"},
 *     {"key": "sysinfo", {"tfcard_status": "OK", ...}}
 *   ]
 * }
 * ```
 *
 * attach_data 字段说明：
 * - solar: 太阳能电池板电压 (光伏板电压)，单位 V
 * - Sata: 卫星数量
 * - LTEMod: 4G 模块状态，OK 表示正常，Fail 表示故障
 * - SIM: SIM 卡状态，OK 表示有，Fail 表示无
 * - MEMs: 倾角加速度模块数据，格式: ID[errno]----X_angle--Y_angle--Z_angle----...---status
 * - sysinfo: 系统信息对象，包含 tfcard_status 等
 */
@JsonClass(generateAdapter = true)
data class GT600DeviceStatusInfo(
    // ==================== 主状态字段 ====================

    /** 外部电压，单位 V */
    val ext_power_volt: String = IOTConstants.NULL_KEY,

    /** 内部温度，单位 ℃ */
    val temp: String = IOTConstants.NULL_KEY,

    /** 内部湿度，单位 RH% */
    val humidity: String = IOTConstants.NULL_KEY,

    /** 4G 信号强度，单位 dBm */
    @Json(name = "4g_signal")
    val _4g_signal: String = IOTConstants.NULL_KEY,

    /** 扩展状态数据列表 */
    val attach_data: List<AttachDataItemBean>? = arrayListOf()
) {
    // ==================== 辅助方法 ====================

    /**
     * 从 attach_data 中获取指定 key 的值
     *
     * @param key 要查找的键名
     * @return 对应的值字符串，如果不存在则返回空字符串
     */
    fun getAttachValue(key: String): String {
        return attach_data?.find { it.key == key }?.value?.toString()
            ?: AppContants.PLACE_HOLDER_VALUE
    }

    /**
     * 获取光伏板电压 (太阳能电池板电压)
     * 字段: attach_data.solar
     */
    val solarVoltage: String
        get() = getAttachValue("solar")

    /**
     * 获取卫星数量
     * 字段: attach_data.Sata
     */
    val satelliteCount: String
        get() = getAttachValue("Sata")

    /**
     * 获取 GNSS 模块状态
     * 根据卫星数量判断：> 0 表示正常，= 0 表示异常
     */
    val gnssStatus: String
        get() = if (getAttachValue("gnss").contains("OK", ignoreCase = true)) "OK" else "Fail"

    /**
     * 获取倾角加速度模块原始数据
     * 字段: attach_data.MEMs
     * 格式: ID[errno]----X_angle--Y_angle--Z_angle----accX--accY--accZ---status
     */
    val memsRawData: String
        get() = getAttachValue("MEMs")

    /**
     * 解析 MEMs 数据，获取模块状态
     * 包含 "OK" 表示正常，否则为故障
     */
    val memsStatus: String
        get() = if (memsRawData.contains("OK", ignoreCase = true)) "OK" else "Fail"

    /**
     * 获取 4G 模块状态
     * 字段: attach_data.LTEMod
     * OK: 正常, Fail: 故障
     */
    val lteModuleStatus: String
        get() = if (getAttachValue("LTEMod").contains("OK", ignoreCase = true)) "OK" else "Fail"

    /**
     * 获取 SIM 卡状态
     * 字段: attach_data.SIM
     * OK: 有, Fail: 无
     */
    val simStatus: String
        get() = if (getAttachValue("SIM").contains("OK", ignoreCase = true)) "OK" else "Fail"

    /**
     * 获取存储卡状态
     * 字段: attach_data.sysinfo.tfcard_status 或 从 sysinfo 对象中解析
     */
    val storageCardStatus: String
        get() {
            // 尝试从 attach_data 中查找 sysinfo
            val sysInfoItem = attach_data?.find { it.key == "sysinfo" }
            return when (val value = sysInfoItem?.value) {
                is Map<*, *> -> {
                    // 如果是 Map 对象，尝试获取 tfcard_status
                    value["tfcard_status"]?.toString() ?: "Fail"
                }

                is String -> {
                    // 如果是字符串，尝试解析
                    if (value.contains("OK", ignoreCase = true)) "OK" else "Fail"
                }

                else -> "Fail"
            }
        }
}
