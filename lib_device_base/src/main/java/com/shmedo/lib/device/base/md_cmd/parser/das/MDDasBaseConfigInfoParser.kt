package com.shmedo.lib.device.base.md_cmd.parser.das

import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.model.das.DasBaseConfigInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： TODO
 */
class MDDasBaseConfigInfoParser: MDCommandParser<DasBaseConfigInfo> {
    override fun parseInstance(values: List<String>): DasBaseConfigInfo {
        return DasBaseConfigInfo().apply {
            token = values.getOrNull(1) ?: token
            localBGNum = values.getOrNull(2) ?: localBGNum
            targetBGNum = values.getOrNull(3) ?: targetBGNum
            activeStatus = values.getOrNull(4) ?: activeStatus
            dataCommunicateMode = values.getOrNull(5) ?: dataCommunicateMode
            rainStation = values.getOrNull(6) ?: rainStation
            rainAccuracy = values.getOrNull(7) ?: rainAccuracy
            locationSensitivity = values.getOrNull(8) ?: locationSensitivity
            locationAccuracy = values.getOrNull(9) ?: locationAccuracy
            heartbeatTimeInterval = values.getOrNull(10) ?: heartbeatTimeInterval
            debugBandRate = values.getOrNull(11) ?: debugBandRate
            sensorBandRate = values.getOrNull(12) ?: sensorBandRate
            collectorModel = values.getOrNull(13) ?: collectorModel
            dataReportInterval = values.getOrNull(14) ?: dataReportInterval
            batteryOverProtect = values.getOrNull(15) ?: batteryOverProtect
            workModel = values.getOrNull(16) ?: workModel
        }
    }

    override fun commandType(): MDCommandType = MDCommandType.BASE_CONFIG
}