package com.shmedo.lib.cmd.base.iot_cmd.model.u_product

/**
 * 创建者：gonghe
 * 创建时间：2024/11/7
 * 描述： TODO
 */
data class UDRainGaugeSerialPortInfo(
    var sw: String = "", //开关   0 关闭 1 启用
    var res: String = "", //雨量计分辨率  毫米
    var total_rain: String = "", //雨量累计值  毫米
)
