package com.shmedo.lib.cmd.base.iot_cmd.enums

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2024/8/11 <br></br>
 * 描述：    故障子类型枚举
 */
enum class FailureChildType(private val typeName: String, private val code: Int) {
    // 硬件故障子类型
    COMPONENT_AGING_FAILURE("组件老化失效", 11),
    PHYSICAL_DAMAGE("物理损坏", 12),
    MANUFACTURING_DEFECT("制造缺陷", 13),
    
    // 软件故障子类型
    SECURITY_VULNERABILITY("安全漏洞", 21),
    SYSTEM_CRASH("系统崩溃", 22),
    
    // 网络与通讯故障子类型
    WIRELESS_CONNECTION_INTERRUPT("无线连接中断", 31),
    
    // 环境适应性故障子类型
    TEMPERATURE_HUMIDITY_EXCEEDED("温湿度超标", 41),
    
    // 人为与运维故障子类型
    CONFIGURATION_ERROR("配置错误", 51),
    
    // 其他故障子类型
    OTHER_ISSUE("其他问题", 61),
    
    UNKNOWN_CHILD_FAILURE("未知子类型", 100)
    ;

    fun getTypeName(): String {
        return typeName
    }

    fun getCode(): Int {
        return code
    }

    companion object {
        @JvmStatic
        fun valueByTypeName(name: String): FailureChildType {
            if (TextUtils.isEmpty(name)) return UNKNOWN_CHILD_FAILURE
            for (type in entries) {
                if (type.typeName == name) return type
            }
            return UNKNOWN_CHILD_FAILURE
        }

        @JvmStatic
        fun valueByCode(code: Int): FailureChildType {
            for (type in entries) {
                if (type.code == code) return type
            }
            return UNKNOWN_CHILD_FAILURE
        }

        @JvmStatic
        val typeNames: List<String>
            get() = entries.map { it.typeName }
            
        @JvmStatic
            val typeCodes: List<Int>
            get() = entries.map { it.code }

        /**
         * 根据主类型获取对应的子类型列表
         */
        @JvmStatic
        fun getChildTypesByMainType(mainType: FailureMainType): List<FailureChildType> {
            return when (mainType) {
                FailureMainType.HARDWARE_FAILURE -> listOf(
                    COMPONENT_AGING_FAILURE,
                    PHYSICAL_DAMAGE,
                    MANUFACTURING_DEFECT
                )
                
                FailureMainType.SOFTWARE_FAILURE -> listOf(
                    SECURITY_VULNERABILITY,
                    SYSTEM_CRASH
                )
                
                FailureMainType.NETWORK_COMMUNICATION_FAILURE -> listOf(
                    WIRELESS_CONNECTION_INTERRUPT
                )
                
                FailureMainType.ENVIRONMENTAL_FAILURE -> listOf(
                    TEMPERATURE_HUMIDITY_EXCEEDED
                )
                
                FailureMainType.HUMAN_OPERATION_FAILURE -> listOf(
                    CONFIGURATION_ERROR
                )
                
                FailureMainType.OTHER_FAILURE -> listOf(
                    OTHER_ISSUE
                )
                
                else -> entries.filter { it != UNKNOWN_CHILD_FAILURE }
            }
        }

        /**
         * 根据主类型获取对应的子类型名称列表
         */
        @JvmStatic
        fun getChildTypeNamesByMainType(mainType: FailureMainType): List<String> {
            return getChildTypesByMainType(mainType).map { it.typeName }
        }

        /**
         * 根据子类型代码获取对应的主类型
         */
        @JvmStatic
        fun getMainTypeByChildType(childType: FailureChildType): FailureMainType {
            return when (childType) {
                COMPONENT_AGING_FAILURE, PHYSICAL_DAMAGE, MANUFACTURING_DEFECT -> 
                    FailureMainType.HARDWARE_FAILURE
                    
                SECURITY_VULNERABILITY, SYSTEM_CRASH -> 
                    FailureMainType.SOFTWARE_FAILURE
                    
                WIRELESS_CONNECTION_INTERRUPT -> 
                    FailureMainType.NETWORK_COMMUNICATION_FAILURE
                    
                TEMPERATURE_HUMIDITY_EXCEEDED -> 
                    FailureMainType.ENVIRONMENTAL_FAILURE
                    
                CONFIGURATION_ERROR -> 
                    FailureMainType.HUMAN_OPERATION_FAILURE
                    
                OTHER_ISSUE -> 
                    FailureMainType.OTHER_FAILURE
                    
                else -> FailureMainType.UNKNOWN_FAILURE
            }
        }
    }
}