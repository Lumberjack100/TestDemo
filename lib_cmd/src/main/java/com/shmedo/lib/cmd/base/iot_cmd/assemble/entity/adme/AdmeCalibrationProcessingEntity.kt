package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.adme

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/2/26
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class AdmeCalibrationProcessingEntity(
    var rivswitch: String = IOTConstants.NULL_KEY, //运动信息校验开关（0:关  1:开）
    var zerodiffer: String = IOTConstants.NULL_KEY, //归零差值 mm
    var posterrorint: String = IOTConstants.NULL_KEY, //定位误差区间值 mm
    var rtrynum: String = IOTConstants.NULL_KEY, //尝试次数

    var kcswitch: String = IOTConstants.NULL_KEY, //k值校验开关（0:关  1:开）
    var kthreshold: String = IOTConstants.NULL_KEY, //k值阈值  mm
    var methreshold: String = IOTConstants.NULL_KEY, //中误差阈值  mm
    var ktrynum: String = IOTConstants.NULL_KEY, //尝试次数

    var dpswitch: String = IOTConstants.NULL_KEY, //数据处理开关（0:关  1:开）
    var accudiff: String = IOTConstants.NULL_KEY, //累积值差值  mm
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        val jsonStr = jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }

        return jsonStr
    }
}

