package com.shmedo.mcloudapp.model

import com.shmedo.lib.cmd.base.iot_cmd.enums.DataCenterPlatform
import com.shmedo.lib.cmd.base.iot_cmd.enums.GuangdongWaterPlatformStationType
import com.shmedo.lib.cmd.base.iot_cmd.enums.PlatformDataProtocol
import com.shmedo.mcloudapp.ui.viewmodel.state.DataCenterParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702DataCenterParamViewModel

/**
 * 平台默认配置管理器
 * 用于管理和提供平台类型的默认参数配置
 */
object DefaultPlatformConfigManager {

    /**
     * 获取指定平台的默认配置
     * @param platform 平台类型
     * @param forMR702 是否为MR702设备
     * @return 平台默认配置，如果不存在则返回null
     */
    fun getDefaultConfig(platform: DataCenterPlatform, forMR702: Boolean = true): DefaultPlatformConfig? {
        return if (forMR702) {
            DefaultPlatformConfig.getMR702DefaultConfigs()[platform]
        } else {
            DefaultPlatformConfig.getUniversalDefaultConfigs()[platform]
        }
    }

    /**
     * 根据平台名称获取默认配置
     * @param platformName 平台名称
     * @param forMR702 是否为MR702设备
     * @return 平台默认配置，如果不存在则返回null
     */
    fun getDefaultConfigByName(platformName: String, forMR702: Boolean = true): DefaultPlatformConfig? {
        return DefaultPlatformConfig.getConfigByPlatformName(platformName, forMR702)
    }

    /**
     * 获取MR702支持的平台列表
     * @return 支持的平台名称列表
     */
    fun getMR702SupportedPlatforms(): List<DataCenterPlatform> {
        return DefaultPlatformConfig.getMR702DefaultConfigs().keys.toList()
    }

    /**
     * 获取MR702支持的平台名称列表
     * @return 支持的平台名称列表
     */
    fun getMR702SupportedPlatformNames(): List<String> {
        return DefaultPlatformConfig.getMR702SupportedPlatformNames()
    }

    /**
     * 根据数据协议获取支持的平台
     * @param protocol 数据协议
     * @param forMR702 是否为MR702设备
     * @return 支持的平台列表
     */
    fun getPlatformsByProtocol(protocol: PlatformDataProtocol, forMR702: Boolean = true): List<DataCenterPlatform> {
        val allPlatforms = if (forMR702) {
            DefaultPlatformConfig.getMR702DefaultConfigs().keys
        } else {
            DefaultPlatformConfig.getUniversalDefaultConfigs().keys
        }

        return allPlatforms.filter { platform ->
            val config = getDefaultConfig(platform, forMR702)
            config?.dataProtocol == protocol
        }
    }

    /**
     * 根据数据协议获取支持的平台名称
     * @param protocol 数据协议
     * @param forMR702 是否为MR702设备
     * @return 支持的平台名称列表
     */
    fun getPlatformNamesByProtocol(protocol: PlatformDataProtocol, forMR702: Boolean = true): List<String> {
        return getPlatformsByProtocol(protocol, forMR702).map { it.getPlatName() }
    }

    /**
     * 应用默认配置到状态对象
     * @param config 默认配置
     * @param states ViewModel状态对象
     */
    fun applyDefaultConfig(config: DefaultPlatformConfig, states: Any) {
        when (states) {
            is MR702DataCenterParamViewModel -> {
                applyToMR702ViewModel(config, states)
            }
            is DataCenterParamViewModel -> {
                applyToUniversalViewModel(config, states)
            }
        }
    }

    /**
     * 应用配置到MR702 ViewModel
     */
    private fun applyToMR702ViewModel(config: DefaultPlatformConfig, states: MR702DataCenterParamViewModel) {
        states.centerServerAddress.set(config.address)
        states.centerServerPort.set(config.port)
        states.dataProtocol.set(config.dataProtocol.toString())
        states.platformType.set(config.platform.getPlatName())

        // MQTT/MQTTS 协议参数
        if (config.dataProtocol == PlatformDataProtocol.MQTT || config.dataProtocol == PlatformDataProtocol.MQTTS) {
            states.productId.set(config.productId)
            states.deviceId.set(config.deviceId)
            states.deviceKey.set(config.deviceKey)
            states.registerCode.set(config.registerCode)
            states.registerAddress.set(config.registerAddress)
            states.registerPort.set(config.registerPort)
        }

        // SL651/SZY206 协议参数
        if (config.dataProtocol == PlatformDataProtocol.SL651 || config.dataProtocol == PlatformDataProtocol.SZY206) {
            states.stationType.set(config.stationType)
            states.centerStationAddr.set(config.centerStationAddr)
            states.password.set(config.password)
            states.telemetryStationAddr.set(config.telemetryStationAddr)
            states.hourlyReport.set(config.hourlyReport)
            states.timingReport.set(config.timingReport)
            states.addReport.set(config.addReport)
            states.maintainReport.set(config.maintainReport)
            states.maintainReportInterval.set(config.maintainReportInterval)
            states.reissuingDataValidDays.set(config.reissuingDataValidDays)
            states.reissuingDataInterval.set(config.reissuingDataInterval)
        }

        // 广东水文平台特有参数
        if (config.platform == DataCenterPlatform.GUANGDONG_WATER_PLATFORM) {
            states.guangdongWaterPlatformStationType.set(GuangdongWaterPlatformStationType.valueByCode(config.packType).getStationName())
        }
    }

    /**
     * 应用配置到通用ViewModel
     */
    private fun applyToUniversalViewModel(config: DefaultPlatformConfig, states: DataCenterParamViewModel) {
        states.centerServerAddress.set(config.address)
        states.centerServerPort.set(config.port)
        states.dataProtocol.set(config.dataProtocol.toString())
        states.platformType.set(config.platform.getPlatName())

        // MQTT 协议参数
        if (config.dataProtocol == PlatformDataProtocol.MQTT || config.dataProtocol == PlatformDataProtocol.MQTTS) {
            states.productId.set(config.productId)
            states.deviceId.set(config.deviceId)
            states.deviceKey.set(config.deviceKey)
            states.registerCode.set(config.registerCode)
            states.registerAddress.set(config.registerAddress)
            states.registerPort.set(config.registerPort)
        }

        // SL651 协议参数
        if (config.dataProtocol == PlatformDataProtocol.SL651) {
            states.stationType.set(config.stationType)
            states.centerStationAddr.set(config.centerStationAddr)
            states.password.set(config.password)
            states.telemetryStationAddr.set(config.telemetryStationAddr)
            states.hourlyReport.set(config.hourlyReport)
            states.maintainReportInterval.set(config.maintainReportInterval)
            states.reissuingDataValidDays.set(config.reissuingDataValidDays)
            states.reissuingDataInterval.set(config.reissuingDataInterval)
        }
    }
}