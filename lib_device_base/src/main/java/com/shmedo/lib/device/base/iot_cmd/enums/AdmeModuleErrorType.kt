package com.shmedo.lib.device.base.iot_cmd.enums

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/11/23 <br></br>
 * 描述：   ADME 模块异常信息
 */
enum class AdmeModuleErrorType(val code: String, val description: String) {
    VOLT_POWER_UNDER("1", "设备欠压"),
    VOLT_POWER_OVER("2", "设备过压"),
    VOLT_SENSOR_UNDER("3", "测斜仪欠压"),
    FAIL("4", "测斜仪配对失败"),
    OVER_C_SF("5", "伺服电机过流"),
    OVER_T_SF("6", "伺服电机过力矩"),
    DZ_SF("7", "伺服电机低力矩"),
    OVER_V_SF("8", "伺服电机超速"),
    ERROR_WIRING_SF("9", "伺服电机接线错误"),
    DZ_JMQ("10", "计米器堵转"),
    FZ_RUN_JMQ("11", "计米器反转"),
    ERROR_WIRING_JMQ("12", "计米器接线错误"),
    INSUFFICIENT_BASE_PULSE_JMQ("13", "计米器基数脉冲不足"),
    XF_TIME_OUT("14", "下放超时"),
    READ_DATA_FAIL("15", "测斜仪读数据失败"),
    CX_FALL("16", "钢丝绳断裂"),
    CX_NON_REVERSE("17", "测斜仪未反转");

    companion object {
        @JvmStatic
        fun valueByCode(code: String): AdmeModuleErrorType? {
            if (TextUtils.isEmpty(code)) return null
            for (errorType in values()) {
                if (errorType.code == code) return errorType
            }
            return null
        }
    }
}