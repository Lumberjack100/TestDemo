package com.shmedo.mcloudapp.model

import com.shmedo.lib.cmd.base.iot_cmd.enums.GuangdongWaterPlatformStationType
import com.shmedo.lib.cmd.base.iot_cmd.enums.NewDataCenterPlatform
import com.shmedo.lib.cmd.base.iot_cmd.enums.PlatformDataProtocol
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.ui.viewmodel.state.DataCenterParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702DataCenterParamViewModel

/**
 * 平台默认配置管理器
 * 用于管理和提供平台类型的默认参数配置
 */
object DefaultPlatformConfigManager {

    /**
     * 获取产品支持的平台名称列表
     * @return 支持的平台名称列表
     */
    fun getSupportedPlatformNames(productType: ProductType): List<String> {
        return when (productType) {
            ProductType.COLLECTOR_R_2 -> arrayListOf(
                NewDataCenterPlatform.MEDO_IOT_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.GUANGDONG_WATER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.GUANGXI_WATER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.HUBEI_WATER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.HUBEI_ECO_PLATFORM.getPlatFormName()
            )

            ProductType.GNSS_M_5, ProductType.GNSS_M_6, ProductType.GNSS_M_7, ProductType.GNSS_M_8 -> arrayListOf(
                NewDataCenterPlatform.MEDO_IOT_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.GUIZHOU_DISASTER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.GUANGXI_DISASTER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.YUNNAN_DISASTER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.MEDO_SOLVER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.CHONGQING_DISASTER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.CORS_PLATFORM.getPlatFormName()
            )

            else -> arrayListOf(
                NewDataCenterPlatform.MEDO_IOT_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.GUIZHOU_DISASTER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.GUANGXI_DISASTER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.YUNNAN_DISASTER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.HENAN_WATER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.HENAN_DAM_MONITOR_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.MEDO_SOLVER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.CHONGQING_DISASTER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.GUANGDONG_WATER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.GUANGXI_WATER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.HUBEI_WATER_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.HUBEI_ECO_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.GUIZHOU_ENCRYPT_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.CORS_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.BEIJING_LUAN_PLATFORM.getPlatFormName(),
                NewDataCenterPlatform.GUANGDONG_FLOOD_PLATFORM.getPlatFormName()
            )
        }
    }

    fun getSupportedDataProtocolNames(
        platformName: String,
        productType: ProductType
    ): List<String> {
        return when (platformName) {
            NewDataCenterPlatform.MEDO_IOT_PLATFORM.getPlatFormName() -> listOf(PlatformDataProtocol.MQTT.getCmdValue())

            NewDataCenterPlatform.GUIZHOU_DISASTER_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.MQTT.getCmdValue()
            )

            NewDataCenterPlatform.GUANGXI_DISASTER_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.MQTT.getCmdValue()
            )

            NewDataCenterPlatform.YUNNAN_DISASTER_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.MQTT.getCmdValue()
            )

            NewDataCenterPlatform.HENAN_WATER_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.SL651.getCmdValue()
            )

            NewDataCenterPlatform.HENAN_DAM_MONITOR_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.MQTT.getCmdValue()
            )


            NewDataCenterPlatform.MEDO_SOLVER_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.TCP_C.getCmdValue()
            )


            NewDataCenterPlatform.CHONGQING_DISASTER_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.MQTT.getCmdValue()
            )


            NewDataCenterPlatform.GUANGDONG_WATER_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.MQTTS.getCmdValue()
            )


            NewDataCenterPlatform.GUANGXI_WATER_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.SL651.getCmdValue()
            )


            NewDataCenterPlatform.HUBEI_WATER_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.SL651.getCmdValue()
            )


            NewDataCenterPlatform.HUBEI_ECO_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.SZY206.getCmdValue()
            )


            NewDataCenterPlatform.GUIZHOU_ENCRYPT_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.MQTT.getCmdValue()
            )


            NewDataCenterPlatform.CORS_PLATFORM.getPlatFormName() -> if (productType == ProductType.GNSS_M_5 || productType == ProductType.GNSS_M_6 || productType == ProductType.GNSS_M_7 || productType == ProductType.GNSS_M_8) listOf(
                PlatformDataProtocol.NTRIP_C.getCmdValue(),
                PlatformDataProtocol.NTRIP_S.getCmdValue()
            ) else listOf(PlatformDataProtocol.NTRIP.getCmdValue())


            NewDataCenterPlatform.BEIJING_LUAN_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.MQTT.getCmdValue()
            )


            NewDataCenterPlatform.GUANGDONG_FLOOD_PLATFORM.getPlatFormName() -> listOf(
                PlatformDataProtocol.HTTP.getCmdValue()
            )

            else -> arrayListOf()
        }

    }

    /**
     * 获取指定平台的默认配置
     * @param platform 平台类型
     * @return 平台默认配置，如果不存在则返回null
     */
    fun getDefaultConfig(platform: NewDataCenterPlatform): DefaultPlatformConfig? {
        return DefaultPlatformConfig.getUniversalDefaultConfigs()[platform]
    }

    /**
     * 根据平台名称获取默认配置
     * @param platformName 平台名称
     * @return 平台默认配置，如果不存在则返回null
     */
    fun getDefaultConfigByName(platformName: String): DefaultPlatformConfig? {
        return DefaultPlatformConfig.getConfigByPlatformName(platformName)
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
    private fun applyToMR702ViewModel(
        config: DefaultPlatformConfig,
        states: MR702DataCenterParamViewModel
    ) {
        states.platformType.set(config.platform.getPlatFormName()) //平台类型
        states.centerServerAddress.set(config.address)//链路地址
        states.centerServerPort.set(config.port)//链路端口
        states.dataProtocol.set(config.dataProtocol.toString())

        // MQTT/MQTTS 协议参数
        if (config.dataProtocol == PlatformDataProtocol.MQTT || config.dataProtocol == PlatformDataProtocol.MQTTS) {
            states.productId.set(config.productId)  //产品ID
            states.deviceId.set(config.deviceId) //设备ID
            states.deviceKey.set(config.deviceKey)//设备key
            states.registerCode.set(config.registerCode)//设备注册码
            states.registerAddress.set(config.registerAddress)//设备注册地址
            states.registerPort.set(config.registerPort)//设备注册端口
        }

        // SL651/SZY206 协议参数
        if (config.dataProtocol == PlatformDataProtocol.SL651 || config.dataProtocol == PlatformDataProtocol.SZY206) {
            states.stationType.set(config.stationType)//SL651 测站分类
            states.centerStationAddr.set(config.centerStationAddr)//SL651 中心站地址
            states.password.set(config.password)//SL651 密码
            states.telemetryStationAddr.set(config.telemetryStationAddr)// 测站编码(遥测站地址)
            states.hourlyReport.set(config.hourlyReport)// 小时报开启标识
            states.timingReport.set(config.timingReport)// 定时报开启标识
            states.addReport.set(config.addReport)// 加报报开启标识
            states.maintainReport.set(config.maintainReport)// 维持报开启标识
            states.maintainReportInterval.set(config.maintainReportInterval)// 维持上报间隔（秒）
            states.reissuingDataValidDays.set(config.reissuingDataValidDays)// 数据补发有效天数
            states.reissuingDataInterval.set(config.reissuingDataInterval)// 数据补发间隔（分钟）
        }

        // 广东水文平台特有参数
        if (config.platform == NewDataCenterPlatform.GUANGDONG_WATER_PLATFORM) {
            states.guangdongWaterPlatformStationType.set(
                GuangdongWaterPlatformStationType.valueByCode(
                    config.packType
                ).getStationName()
            )
        }
    }

    /**
     * 应用配置到通用ViewModel
     */
    private fun applyToUniversalViewModel(
        config: DefaultPlatformConfig,
        states: DataCenterParamViewModel
    ) {
        states.platformType.set(config.platform.getPlatFormName()) //平台类型
        states.centerServerAddress.set(config.address)//链路地址
        states.centerServerPort.set(config.port)//链路端口
        states.dataProtocol.set(config.dataProtocol.toString())

        // MQTT 协议参数
        if (config.dataProtocol == PlatformDataProtocol.MQTT || config.dataProtocol == PlatformDataProtocol.MQTTS) {
            states.productId.set(config.productId)  //产品ID
            states.deviceId.set(config.deviceId) //设备ID
            states.deviceKey.set(config.deviceKey)//设备key
            states.registerCode.set(config.registerCode)//设备注册码
            states.registerAddress.set(config.registerAddress)//设备注册地址
            states.registerPort.set(config.registerPort)//设备注册端口
        }

        // SL651/SZY206 协议参数
        if (config.dataProtocol == PlatformDataProtocol.SL651 || config.dataProtocol == PlatformDataProtocol.SZY206) {
            states.stationType.set(config.stationType)//SL651 测站分类
            states.centerStationAddr.set(config.centerStationAddr)//SL651 中心站地址
            states.password.set(config.password)//SL651 密码
            states.telemetryStationAddr.set(config.telemetryStationAddr)// 测站编码(遥测站地址)
            states.hourlyReport.set(config.hourlyReport)// 小时报开启标识
            states.maintainReportInterval.set(config.maintainReportInterval)// 维持上报间隔（秒）
            states.reissuingDataValidDays.set(config.reissuingDataValidDays)// 数据补发有效天数
            states.reissuingDataInterval.set(config.reissuingDataInterval)// 数据补发间隔（分钟）
        }
    }
}