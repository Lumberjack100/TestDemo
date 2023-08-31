package com.shmedo.lib.device.base.iot_cmd.enums

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/18 <br></br>
 * 描述：    设备测量过程状态枚举
 */
enum class AdmeCTRMotionState(val code: String, val description: String, val simpleInfo: String) {
    NOZZLE_WAITING("0", "上拉至管口", "上拉至管口"),
    PAIR_SETTING_PARAM("1", "测斜仪配对,设置参数", "测斜仪配对"),
    DOWN("2", "测斜仪下放", "下放中"),
    BOTTOM_WAITING("3", "管底等待", "管底等待"),
    POINT_MEASUREMENT("4", "测点测量", "测量中"),
    MEASUREMENT_OVER("5", "磁开关触发,测量结束", "测点结束"),
    READ_DATA("6", "测斜仪配对,读取数据", "数据读取中"),
    UPLOAD_DATA("7", "数据上传", "数据上传中"),
    WAITING_NEXT_TESTING("8", "数据上传完成等待下次测量", "等待测量"),
    WAITING_BACK_TESTING("9", "等待反测", "等待反向测量"),
    FAILED("10", "测量失败", "测量失败"),
    UNKNOWN_ERROR("-100", "未知", "未知");

    companion object {
        @JvmStatic
        fun valueByCode(code: String): AdmeCTRMotionState {
            if (TextUtils.isEmpty(code)) return UNKNOWN_ERROR
            for (motionState in values()) {
                if (motionState.code == code) return motionState
            }
            return UNKNOWN_ERROR
        }
    }
}