package com.shmedo.lib.device.base.md_cmd.parser.das

import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.model.das.DeviceStatusInfoTwo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 */
class MDDeviceStatusInfoTwoParser: MDCommandParser<DeviceStatusInfoTwo> {
    override fun parseInstance(values: List<String>): DeviceStatusInfoTwo {
        return DeviceStatusInfoTwo(
            snNumber = values.getOrNull(1) ?: "",
            longitude = values.getOrNull(2) ?: "",
            latitude = values.getOrNull(3) ?: "",
            internalVoltage = values.getOrNull(4) ?: "",
            externalVoltage = values.getOrNull(5) ?: "",
            solarControllerStatus = values.getOrNull(6) ?: "",
            solarPanelVoltage = values.getOrNull(7) ?: "",
            batteryVoltage = values.getOrNull(8) ?: "",
            dailyPowerGeneration = values.getOrNull(9) ?: "",
            dailyPowerConsumption = values.getOrNull(10) ?: "",
            internalTempHumidityStatus = values.getOrNull(11) ?: "",
            internalTemperature = values.getOrNull(12) ?: "",
            internalHumidity = values.getOrNull(13) ?: "",
            externalTempHumidityStatus = values.getOrNull(14) ?: "",
            externalTemperature = values.getOrNull(15) ?: "",
            externalHumidity = values.getOrNull(16) ?: "",
            switchType = values.getOrNull(17) ?: "",
            rainfallStatus = values.getOrNull(18) ?: ""
        )
    }

    override fun commandType(): MDCommandType = MDCommandType.QUERY_DAS_STATUS_2
}