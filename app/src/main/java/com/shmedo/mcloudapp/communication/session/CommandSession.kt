package com.shmedo.mcloudapp.communication.session

/**
 * 指令优先级枚举
 * 数值越小优先级越高
 */
enum class CommandPriority(val value: Int) {
    /** 紧急指令（如重启、急停） */
    URGENT(0),
    /** 高优先级（如实时数据查询） */
    HIGH(1),
    /** 普通指令 */
    NORMAL(2),
    /** 低优先级（如批量数据） */
    LOW(3)
}


