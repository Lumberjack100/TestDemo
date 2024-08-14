package com.shmedo.lib.cmd.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/12/13 <br></br>
 * 描述：      ADME 电压配置参数
 */
data class AdmeVoltageConfigInfo(
    var volt_power_standard: String = "", //驱动器标压阈值
    var volt_power_low: String = "", //驱动器低压阈值
    var volt_power_under: String = "", //驱动器欠压阈值
    var volt_sensor_standard: String = "", //测斜仪标压阈值
    var volt_sensor_low: String = "", //测斜仪低压阈值
    var volt_sensor_under: String = "", //测斜仪欠压阈值
    var rope_length: String = "", //钢丝绳长
    var antifdis: String = "", //防冻距离
)