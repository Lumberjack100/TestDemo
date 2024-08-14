package com.shmedo.lib.cmd.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/27/20 <br></br>
 * 描述：    ADME 计米轮配置参数
 */
data class AdmeMeterWheelInfo(
    var enclinenum: String = "", //编码器线数
    var outline: String = "", //外径
    var uptiona: String = "", //上拉一次修正参数
    var uptionb: String = "",//上拉二次修正参数
    var upconstant: String = "", //上拉常数
    var upfilter: String = "", //上拉滤波器系数
    var downtiona: String = "", //下放一次修正参数
    var downtionb: String = "",//下放二次修正参数
    var downconstant: String = "", //下放常数
    var downfilter: String = "", //下放滤波器系数
)