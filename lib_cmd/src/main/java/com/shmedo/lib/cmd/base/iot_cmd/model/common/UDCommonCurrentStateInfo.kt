package com.shmedo.lib.cmd.base.iot_cmd.model.common

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2024/8/27
 * @desc: 一体化雷达设备当前状态
 *
 */
@JsonClass(generateAdapter = true)
data class UDCommonCurrentStateInfo(
    @Json(name = "dev_type")
    val devType: String = IOTConstants.NULL_KEY, //设备型号
    @Json(name = "dev_sn")
    val sn: String = IOTConstants.NULL_KEY, //设备SN号
    @Json(name = "dev_sta")
    val devStatus: String = IOTConstants.NULL_KEY, //生产日期 20240228




)