package com.shmedo.lib.cmd.base.md_cmd.model.das

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 *
 * $$041,(1),(2),(3),(4),(5),(6)\r\n
 * （1）SN号
 * （2）IMEI号
 * （3）SIM卡号
 * （4）启动代码1
 * （5）启动代码2
 * （6）信号强度，1~11为1格信号，12~18为2格信号，19~25为3格信号，26~31为4格信号
 * 示例：
 * $$041,150000L,865860047575320,898604061918C0643348,20,1,9
 */
data class DeviceStatusInfoOne(
    val snNumber: String = "",
    val imeiNumber: String = "",
    val simNumber: String = "",
    val startCodeOne: String = "",
    val startCodeTwo: String = "",
    val signalStrength: String = ""
)
