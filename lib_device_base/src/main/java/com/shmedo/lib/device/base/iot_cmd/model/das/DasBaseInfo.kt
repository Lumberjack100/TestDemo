package com.shmedo.lib.device.base.iot_cmd.model.das

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/15 <br></br>
 * 描述：    DAS 状态页面基本信息
 */
class DasBaseInfo {

    var sn : String = ""//SN号

    var iccid : String = ""//SIM卡识别码

    var imei : String = ""//IMEI号

    var ver : String = ""//固件版本

    var local : String = ""//位置

    var involt : String = ""//内部电量

    var outvolt : String = ""//外部电压

    var csq : String = ""//信号强度

    var isp : String = ""//网络运营商

    var code: String = "" //设备启动代码
}