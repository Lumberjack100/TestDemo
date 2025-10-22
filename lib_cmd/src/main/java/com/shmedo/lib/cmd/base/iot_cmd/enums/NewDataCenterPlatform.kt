package com.shmedo.lib.cmd.base.iot_cmd.enums

/** 新数据中心平台枚举，封装平台基础配置以便统一使用。 */
enum class NewDataCenterPlatform(
    private val platFormName: String,
    private val platType: String,
    private val ip: String?,
    private val port: String?
) {
    MEDO_IOT_PLATFORM("米度物联平台", "2", "mqtthub.shmedo.cn", "1883"),
    GUIZHOU_DISASTER_PLATFORM("贵州地灾平台", "3", "iot.gz1155.cn", "1883"),
    GUANGXI_DISASTER_PLATFORM("广西地灾平台", "3", "218.65.206.87", "1883"),
    YUNNAN_DISASTER_PLATFORM("云南地灾平台", "3", "222.221.241.110", "1883"),
    HENAN_WATER_PLATFORM("河南水文平台", "4", "data.skaqjc.cn", "9888"),
    HENAN_DAM_MONITOR_PLATFORM("河南大坝监测平台", "4", "data.skaqjc.cn", "1883"),
    MEDO_SOLVER_PLATFORM("米度解算平台", "6", null, null),
    CHONGQING_DISASTER_PLATFORM("重庆地灾平台", "7", "183.66.66.31", "1883"),
    GUANGDONG_WATER_PLATFORM("广东水文平台", "8", "iot.gdwater.gov.cn", "8883"),
    GUANGXI_WATER_PLATFORM("广西水文平台", "9", "222.216.6.174", "8076"),
    HUBEI_WATER_PLATFORM("湖北水文平台", "10", "183.95.190.143", "9095"),
    HUBEI_ECO_PLATFORM("湖北生态流量平台", "11", "183.95.190.143", "8094"),
    GUIZHOU_ENCRYPT_PLATFORM("贵州加密平台", "12", null, null),
    CORS_PLATFORM("CORS平台", "13", null, null),
    BEIJING_LUAN_PLATFORM("北京路安平台", "14", "120.46.221.231", "2443"),
    GUANGDONG_FLOOD_PLATFORM("广东山洪预警平台", "15", "8.134.151.187", "8069");

    fun getPlatFormName(): String {
        return platFormName
    }

    fun getPlatType(): String {
        return platType
    }

    fun getIp(): String? {
        return ip
    }

    fun getPort(): String? {
        return port
    }

    companion object {

        @JvmStatic
        val platFormNames: List<String>
            get() = NewDataCenterPlatform.entries.map { it.platFormName }

        @JvmStatic
        fun valueByPlatformName(platFormName: String): NewDataCenterPlatform {
            return entries.find { it.platFormName == platFormName } ?: MEDO_IOT_PLATFORM
        }


        @JvmStatic
        fun valueByPlatType(
            platType: String,
            ip: String? = null,
            port: String? = null
        ): NewDataCenterPlatform {
            val matchedByType = entries.filter { it.platType == platType }
            if (matchedByType.isEmpty()) return MEDO_IOT_PLATFORM
            if (matchedByType.size == 1) return matchedByType.first()

            val normalizedIp = ip?.trim()?.takeUnless { it.isEmpty() }
            val normalizedPort = port?.trim()?.takeUnless { it.isEmpty() }
            if (normalizedIp == null || normalizedPort == null) return matchedByType.first()

            // 如果没有完全匹配的，返回第一个匹配 platType 的平台
            return matchedByType.firstOrNull { it.ip == normalizedIp && it.port == normalizedPort }
                ?: matchedByType.first()
        }
    }

}
