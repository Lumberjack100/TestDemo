package com.shmedo.lib.device.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/24/20 <br></br>
 * 描述：    ADME 基础配置参数
 */
data class AdmeBasicConfigInfo (
    var inctype: String = "", //测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    var address: String = "", //采集器 / MAC 地址
    var interdeep: String = "", //测斜管孔深
    var downspeed : String = "",//下放速度
    var downwaitetime : String = "",//下放等待时间
    var datatype: String = "", //数据解算方式（0:顶部固定法，1底部固定法）
)