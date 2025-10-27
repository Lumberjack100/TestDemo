package com.shmedo.mcloudapp.model

import com.shmedo.lib.cmd.base.iot_cmd.enums.NewDataCenterPlatform
import com.shmedo.lib.cmd.base.iot_cmd.enums.PlatformDataProtocol
import com.shmedo.lib.cmd.base.iot_cmd.enums.SL651StationType

/**
 * 平台默认配置数据类
 * 用于存储平台类型的默认参数配置
 */
data class DefaultPlatformConfig(
    val platform: NewDataCenterPlatform,//平台类型
    val address: String = "",
    val port: String = "",
    val dataProtocol: PlatformDataProtocol = PlatformDataProtocol.MQTT,//数据协议
    val productId: String = "",//产品ID
    val deviceId: String = "",//设备 Id
    val deviceKey: String = "",//设备Key
    val registerCode: String = "",//注册码
    val registerAddress: String = "",//注册地址
    val registerPort: String = "",//注册端口
    val stationType: String = SL651StationType.RESERVOIR.getStationName(), //SL651协议 测站分类
    val centerStationAddr: String = "",//SL651协议 中心站地址
    val password: String = "",//SL651协议 密码
    val telemetryStationAddr: String = "",//SL651协议 测站编码(遥测站地址)
    val hourlyReport: Boolean = false,//SL651协议 小时报开启标识
    val timingReport: Boolean = false,//SL651协议 定时报开启标识
    val addReport: Boolean = false,//SL651协议 加报报开启标识
    val maintainReport: Boolean = true,//SL651协议 维持报开启标识
    val maintainReportInterval: String = "30",//SL651协议 维持上报间隔（秒）
    val reissuingDataValidDays: String = "180",//SL651协议 数据补发有效天数
    val reissuingDataInterval: String = "30",//SL651协议 数据补发间隔（分钟）
    val packType: String = "0" // 广东水文平台测站类型
) {
    companion object {
        /**
         * 获取通用设备支持的平台默认配置
         */
        fun getUniversalDefaultConfigs(): Map<NewDataCenterPlatform, DefaultPlatformConfig> {
            return mapOf(
                NewDataCenterPlatform.MEDO_IOT_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.MEDO_IOT_PLATFORM,
                    address = "47.96.80.48",
                    port = "1883",
                    dataProtocol = PlatformDataProtocol.MQTT,
                    registerCode = "0d4b5ee1-472c-4352-883e-ed012e725b2f",
                    registerAddress = "47.96.80.48",
                    registerPort = "80"
                ),

                NewDataCenterPlatform.GUIZHOU_DISASTER_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.GUIZHOU_DISASTER_PLATFORM,
                    address = "iot.gz1155.cn",
                    port = "1883",
                    dataProtocol = PlatformDataProtocol.MQTT
                ),

                NewDataCenterPlatform.GUANGXI_DISASTER_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.GUANGXI_DISASTER_PLATFORM,
                    address = "218.65.206.87",
                    port = "1883",
                    dataProtocol = PlatformDataProtocol.MQTT,
                ),

                NewDataCenterPlatform.YUNNAN_DISASTER_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.YUNNAN_DISASTER_PLATFORM,
                    address = "222.221.241.110",
                    port = "1883",
                    dataProtocol = PlatformDataProtocol.MQTT,
                ),

                NewDataCenterPlatform.GANSU_DISASTER_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.GANSU_DISASTER_PLATFORM,
                    address = "61.178.41.182",
                    port = "21807",
                    dataProtocol = PlatformDataProtocol.MQTT,
                ),

                NewDataCenterPlatform.HENAN_WATER_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.HENAN_WATER_PLATFORM,
                    address = "data.skaqjc.cn",
                    port = "9888",
                    dataProtocol = PlatformDataProtocol.SL651,
                    stationType = SL651StationType.RESERVOIR.getStationName(), // 水库(湖泊)
                    hourlyReport = true,
                    timingReport = true,
                    addReport = false,
                    maintainReport = true,
                    maintainReportInterval = "30",
                    reissuingDataValidDays = "180",
                    reissuingDataInterval = "30"
                ),

                NewDataCenterPlatform.HENAN_DAM_MONITOR_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.HENAN_DAM_MONITOR_PLATFORM,
                    address = "data.skaqjc.cn",
                    port = "1883",
                    dataProtocol = PlatformDataProtocol.MQTT
                ),

                NewDataCenterPlatform.MEDO_SOLVER_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.MEDO_SOLVER_PLATFORM,
                    address = "",
                    port = "",
                    dataProtocol = PlatformDataProtocol.TCP_C
                ),

                NewDataCenterPlatform.CHONGQING_DISASTER_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.CHONGQING_DISASTER_PLATFORM,
                    address = "183.66.66.31",
                    port = "1883",
                    dataProtocol = PlatformDataProtocol.MQTT,
                ),

                NewDataCenterPlatform.GUANGDONG_WATER_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.GUANGDONG_WATER_PLATFORM,
                    address = "iot.gdwater.gov.cn",
                    port = "8883",
                    dataProtocol = PlatformDataProtocol.MQTTS,
                    registerCode = "a1b2c3d4e5f6g7h8",
                    registerAddress = "0.0.0.0",
                    registerPort = "1667",
                    maintainReportInterval = "30",
                    packType = "0" // 山洪灾害监测站
                ),

                NewDataCenterPlatform.GUANGXI_WATER_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.GUANGXI_WATER_PLATFORM,
                    address = "222.216.6.174",
                    port = "8076",
                    dataProtocol = PlatformDataProtocol.SL651,
                    stationType = SL651StationType.RESERVOIR.getStationName(), // 水库(湖泊)
                    hourlyReport = true,
                    timingReport = true,
                    addReport = false,
                    maintainReport = true,
                    maintainReportInterval = "30",
                    reissuingDataValidDays = "180",
                    reissuingDataInterval = "30"
                ),

                NewDataCenterPlatform.HUBEI_WATER_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.HUBEI_WATER_PLATFORM,
                    address = "183.95.190.143",
                    port = "9095",
                    dataProtocol = PlatformDataProtocol.SL651,
                    stationType = SL651StationType.RESERVOIR.getStationName(), // 水库(湖泊)
                    hourlyReport = true,
                    timingReport = true,
                    addReport = false,
                    maintainReport = true,
                    maintainReportInterval = "30",
                    reissuingDataValidDays = "180",
                    reissuingDataInterval = "30"
                ),

                NewDataCenterPlatform.HUBEI_ECO_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.HUBEI_ECO_PLATFORM,
                    address = "183.95.190.143",
                    port = "8094",
                    dataProtocol = PlatformDataProtocol.SZY206,
                    hourlyReport = true,
                    timingReport = true,
                    addReport = true,
                    maintainReport = true,
                    maintainReportInterval = "30",
                    reissuingDataValidDays = "180",
                    reissuingDataInterval = "30"
                ),

                NewDataCenterPlatform.GUIZHOU_ENCRYPT_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.GUIZHOU_ENCRYPT_PLATFORM,
                    address = "",
                    port = "",
                    dataProtocol = PlatformDataProtocol.MQTT,
                ),

                NewDataCenterPlatform.CORS_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.CORS_PLATFORM,
                    address = "",
                    port = "",
                    dataProtocol = PlatformDataProtocol.NTRIP_C,
                ),

                NewDataCenterPlatform.BEIJING_LUAN_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.BEIJING_LUAN_PLATFORM,
                    address = "120.46.221.231",
                    port = "2443",
                    dataProtocol = PlatformDataProtocol.MQTT,
                ),

                NewDataCenterPlatform.GUANGDONG_FLOOD_PLATFORM to DefaultPlatformConfig(
                    platform = NewDataCenterPlatform.GUANGDONG_FLOOD_PLATFORM,
                    address = "8.134.151.187",
                    port = "8069",
                    dataProtocol = PlatformDataProtocol.HTTP
                )
            )
        }

        /**
         * 根据平台名称获取默认配置
         */
        fun getConfigByPlatformName(platformName: String): DefaultPlatformConfig? {
            val configs = getUniversalDefaultConfigs()
            return configs.entries.find { it.key.getPlatFormName() == platformName }?.value
        }
    }
}
