package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMotorMotionAngleInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/1/4
 * 描述： TODO
 */
class AdmeMotorMotionAngleInfoParser  : IOTCommandParser<AdmeMotorMotionAngleInfo> {

    override fun parseInstance(keyValueMap: Map<String, String>): AdmeMotorMotionAngleInfo {
        return AdmeMotorMotionAngleInfo().apply {
            pulsenumber = keyValueMap.getOrDefault("pulsenumber", pulsenumber)
            realmoveangle = keyValueMap.getOrDefault("realmoveangle", realmoveangle)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE
}