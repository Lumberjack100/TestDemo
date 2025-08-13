package com.shmedo.lib.cmd.base.iot_cmd.enums

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2024/8/11 <br></br>
 * 描述：    故障主类型枚举
 */
enum class FailureMainType(private val typeName: String, private val code: Int) {
    HARDWARE_FAILURE("硬件故障", 1),
    SOFTWARE_FAILURE("软件故障", 2),
    NETWORK_COMMUNICATION_FAILURE("网络与通讯故障", 3),
    ENVIRONMENTAL_FAILURE("环境适应性故障", 4),
    HUMAN_OPERATION_FAILURE("人为与运维故障", 5),
    OTHER_FAILURE("其他", 6),
    
    UNKNOWN_FAILURE("未知", 100)
    ;

    fun getTypeName(): String {
        return typeName
    }

    fun getCode(): Int {
        return code
    }

    companion object {
        @JvmStatic
        fun valueByTypeName(name: String): FailureMainType {
            if (TextUtils.isEmpty(name)) return UNKNOWN_FAILURE
            for (type in entries) {
                if (type.typeName == name) return type
            }
            return UNKNOWN_FAILURE
        }

        @JvmStatic
        fun valueByCode(code: Int): FailureMainType {
            for (type in entries) {
                if (type.code == code) return type
            }
            return UNKNOWN_FAILURE
        }

        @JvmStatic
        val typeNames: List<String>
            get() = entries.map { it.typeName }
            
        @JvmStatic
        val typeCodes: List<Int>
            get() = entries.map { it.code }
    }
}