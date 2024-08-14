package com.shmedo.lib.cmd.base.md_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.cmd.base.md_cmd.model.das.DeviceStatusInfoTwo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 */
class MDDeviceStatusInfoTwoParser : MDCommandParser<DeviceStatusInfoTwo> {
    override fun parseInstance(values: List<String>): DeviceStatusInfoTwo {
        return DeviceStatusInfoTwo(
            snNumber = values.getOrNull(1) ?: "",
            longitude = values.getOrNull(2) ?: "",
            latitude = values.getOrNull(3) ?: "",
            internalVoltage = values.getOrNull(4) ?: IOTConstants.NULL_KEY,
            externalVoltage = values.getOrNull(5) ?: IOTConstants.NULL_KEY,
            solarControllerStatus = values.getOrNull(6) ?: IOTConstants.NULL_KEY,
            solarPanelVoltage = values.getOrNull(7) ?: IOTConstants.NULL_KEY,
            batteryVoltage = values.getOrNull(8) ?: IOTConstants.NULL_KEY,
            dailyPowerGeneration = values.getOrNull(9) ?: IOTConstants.NULL_KEY,
            dailyPowerConsumption = values.getOrNull(10) ?: IOTConstants.NULL_KEY,
            internalTempHumidityStatus = values.getOrNull(11) ?: IOTConstants.NULL_KEY,
            internalTemperature = values.getOrNull(12) ?: IOTConstants.NULL_KEY,
            internalHumidity = values.getOrNull(13) ?: IOTConstants.NULL_KEY,
            externalTempHumidityStatus = values.getOrNull(14) ?: IOTConstants.NULL_KEY,
            externalTemperature = values.getOrNull(15) ?: IOTConstants.NULL_KEY,
            externalHumidity = values.getOrNull(16) ?: IOTConstants.NULL_KEY,
            switchType = values.getOrNull(17) ?: IOTConstants.NULL_KEY,
            rainfallStatus = values.getOrNull(18) ?: IOTConstants.NULL_KEY
        )
    }

    override fun commandType(): MDCommandType = MDCommandType.QUERY_DAS_STATUS_2
}