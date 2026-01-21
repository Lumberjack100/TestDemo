package com.shmedo.lib.cmd.base.iot_cmd.model.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/24
 * 描述： Lora通讯参数信息
 */
data class LoraCommunicateInfo(
    var loratype: String = "",//LORA模组型号 1：F8L10C  2：TP1107
    var chl: String = "",//信道 [0~19] 载波频率以410Mhz为起始，间隔1Mhz，进行信道划分，共划分30个信道，默认10
    var outpwr: String = "",//发射功率 [5~20] 默认20
    var airbaud: String = "", //空中速率  [1~6] 默认3
    var netid: String = "",//网络号 [1~10] 默认1
    var localid: String = "",//本机地址 [1~20] 网关默认1,监测设备默认2
    var dstid: String = "",//目标地址 [1~20] 网关默认2,监测设备默认1
)
