package com.shmedo.mcloudapp.model

import com.shmedo.lib.cmd.base.iot_cmd.enums.DataCenterPlatform
import com.shmedo.lib.cmd.base.iot_cmd.enums.PlatformDataProtocol
import com.shmedo.lib.cmd.base.iot_cmd.enums.SL651StationType

/**
 * 平台默认配置数据类
 * 用于存储平台类型的默认参数配置
 */
data class DefaultPlatformConfig(
    val platform: DataCenterPlatform,//平台类型
    val address: String = "",
    val port: String = "",
    val dataProtocol: PlatformDataProtocol = PlatformDataProtocol.MQTT,//数据协议
    val productId: String = "",//产品ID
    val deviceId: String = "",//设备 Id
    val deviceKey: String = "",//设备Key
    val registerCode: String = "",//注册码
    val registerAddress: String = "",//注册地址
    val registerPort: String = "",//注册端口
    val stationType: String = "", //SL651协议 测站分类
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
         * 获取MR702支持的平台默认配置
         */
        fun getMR702DefaultConfigs(): Map<DataCenterPlatform, DefaultPlatformConfig> {
            return mapOf(
                DataCenterPlatform.MEDO_IOT_PLATFORM to DefaultPlatformConfig(
                    platform = DataCenterPlatform.MEDO_IOT_PLATFORM,
                    address = "47.96.80.48",
                    port = "1883",
                    dataProtocol = PlatformDataProtocol.MQTT,
                    registerCode = "0d4b5ee1-472c-4352-883e-ed012e725b2f",
                    registerAddress = "47.96.80.48",
                    registerPort = "80",
                    maintainReportInterval = "30"
                ),
                DataCenterPlatform.GUANGDONG_WATER_PLATFORM to DefaultPlatformConfig(
                    platform = DataCenterPlatform.GUANGDONG_WATER_PLATFORM,
                    address = "iot.gdwater.gov.cn",
                    port = "8883",
                    dataProtocol = PlatformDataProtocol.MQTTS,
                    registerCode = "a1b2c3d4e5f6g7h8",
                    registerAddress = "0.0.0.0",
                    registerPort = "1667",
                    maintainReportInterval = "30",
                    packType = "0" // 山洪灾害监测站
                ),
                DataCenterPlatform.GUANGXI_WATER_PLATFORM to DefaultPlatformConfig(
                    platform = DataCenterPlatform.GUANGXI_WATER_PLATFORM,
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
                DataCenterPlatform.HUBEI_WATER_PLATFORM to DefaultPlatformConfig(
                    platform = DataCenterPlatform.HUBEI_WATER_PLATFORM,
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
                DataCenterPlatform.HUBEI_ECO_PLATFORM to DefaultPlatformConfig(
                    platform = DataCenterPlatform.HUBEI_ECO_PLATFORM,
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
                )
            )
        }

        /**
         * 获取通用设备支持的平台默认配置
         */
        fun getUniversalDefaultConfigs(): Map<DataCenterPlatform, DefaultPlatformConfig> {
            return getMR702DefaultConfigs() // 初始使用相同的配置，可根据需要扩展
        }

        /**
         * 根据平台名称获取默认配置
         */
        fun getConfigByPlatformName(platformName: String, forMR702: Boolean = true): DefaultPlatformConfig? {
            val configs = if (forMR702) getMR702DefaultConfigs() else getUniversalDefaultConfigs()
            return configs.entries.find { it.key.getPlatName() == platformName }?.value
        }

        /**
         * 获取MR702支持的平台名称列表
         */
        fun getMR702SupportedPlatformNames(): List<String> {
            return getMR702DefaultConfigs().keys.map { it.getPlatName() }
        }
    }
}
