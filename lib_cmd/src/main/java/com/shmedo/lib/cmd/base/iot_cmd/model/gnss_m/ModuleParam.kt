package com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * @author：gonghe
 * @time: 2025/1/22
 * @desc: 模块参数数据模型
 * 响应示例：$cmd=md_getmoduleparam&sw=1&autosw=1&valid=1&borad_type=LG290M&baud=115200&data_type=1&borad_soft=...&altitude_angle=15.000000&result=succ&apikey=...&msgid=...
 */
data class ModuleParam(
    val sw: String = IOTConstants.NULL_KEY,
    val autosw: String = IOTConstants.NULL_KEY,
    val valid: String = IOTConstants.NULL_KEY,
    val borad_type: String = IOTConstants.NULL_KEY,
    val baud: String = IOTConstants.NULL_KEY,
    val data_type: String = IOTConstants.NULL_KEY,
    val borad_soft: String = IOTConstants.NULL_KEY,
    val rtcm_mqtt_sw: String = IOTConstants.NULL_KEY,
    val rtcm_log_sw: String = IOTConstants.NULL_KEY,
    val nmea_log_sw: String = IOTConstants.NULL_KEY,
    val antenna_height: String = IOTConstants.NULL_KEY,
    val altitude_angle: String = IOTConstants.NULL_KEY // 截至高度角
) 