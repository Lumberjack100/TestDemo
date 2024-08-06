package com.shmedo.lib.cmd.base.md_cmd.model.das

/**
 * 创建者：gonghe
 * 创建时间：2024/4/16
 * 描述： 数字水位计参数
 */
data class MDDasDigitalPiezometerInfo(
    var osmometerStatus: String = "", //渗压计功能状态 2：关闭数字水位计采集功能 1：打开数字水位计采集功能
    var osmometerAddress: String = "", //地址
    var depthTrigger: String = "", //水深度触发阈值
    var depthCorrect: String = "", //水深度修正值
    var temperatureTrigger: String = "", //温度触发阈值
    var temperatureCorrect: String = "", //温度修正值
    var wireRopeLength: String = "", //绳长
    var installElevation: String = "", //安装高程
)
