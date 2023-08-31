package com.shmedo.lib.device.base.iot_cmd.enums

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/11/4 <br></br>
 * 描述：    传感器异常信息
 */
enum class SensorErrorType(val code: String, val description: String) {
    NORMAL("0", "正常"),
    ABNORMAL_POWER_SUPPLY("-1", "供电异常"),
    ABNORMAL_DATA("-2", "数据异常"),
    NO_DATA_COLLECTED("-3", "未采集到数据"),
    NOT_CONNECTED("-4", "未接入"),
    SHORT_CIRCUIT("-5", "短路"),
    POOR_CONTACT("-6", "接触不良"),
    COLLECTION_CONFIG_NOT_MATCH("-7", "采集配置类型不匹配"),
    UNKNOWN_ERROR("-100", "未知错误");

    override fun toString(): String {
        return code
    }

    companion object {
        /**
         * 根据传感器错误码返回对应的错误信息
         * @param errorCode
         * @return
         */
        @JvmStatic
        fun getErrorMessageByCode(errorCode: String): String {
            if (TextUtils.isEmpty(errorCode)) return AdmeCTRMotionState.UNKNOWN_ERROR.description
            for (sensorErrorType in values()) {
                if (sensorErrorType.code == errorCode) return sensorErrorType.description
            }
            return AdmeCTRMotionState.UNKNOWN_ERROR.description
        }
    }
}