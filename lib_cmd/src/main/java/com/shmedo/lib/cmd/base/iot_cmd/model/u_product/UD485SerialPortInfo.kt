package com.shmedo.lib.cmd.base.iot_cmd.model.u_product

/**
 * 创建者：gonghe
 * 创建时间：2024/11/7
 * 描述： TODO
 */
data class UD485SerialPortInfo (
    var sw: String = "", //开关   0 关闭 1 启用
    var baud: String = "", //波特率
    var addr: String = "", //本机地址
)