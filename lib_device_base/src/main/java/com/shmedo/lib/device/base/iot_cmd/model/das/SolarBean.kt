package com.shmedo.lib.device.base.iot_cmd.model.das

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/16 <br></br>
 * 描述：     DAS 状态页面太阳能控制器状态
 */
class SolarBean {
    var errno //错误码
            = 0
    var solarvolt //太阳能板电压
            = 0.0
    var batvolt //蓄电池电压
            = 0.0
    var solarpwr //太阳能板功率
            = 0.0
    var loadpwr //负载功率
            = 0.0
}