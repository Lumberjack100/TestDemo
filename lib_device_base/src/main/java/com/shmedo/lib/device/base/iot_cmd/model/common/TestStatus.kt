package com.shmedo.lib.device.base.iot_cmd.model.common

import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class TestStatus(
    var sn: String = IOTConstants.NULL_KEY
)
