package com.shmedo.lib.cmd.base.iot_cmd.model.das

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/15 <br></br>
 * 描述：    DAS 状态页面基本信息
 */
data class DasBaseInfo(
    var sn: String = IOTConstants.NULL_KEY,//SN号
    var iccid: String = IOTConstants.NULL_KEY,//SIM卡识别码
    var imei: String = IOTConstants.NULL_KEY,//IMEI号
    var ver: String = IOTConstants.NULL_KEY,//固件版本
    var local: String = IOTConstants.NULL_KEY,//位置
    var involt: String = IOTConstants.NULL_KEY,//内部电量
    var outvolt: String = IOTConstants.NULL_KEY,//外部电压  供电电压是设备自检出来的电压值
    var csq: String = IOTConstants.NULL_KEY,//信号强度
    var isp: String = IOTConstants.NULL_KEY,//网络运营商
    var code: String = IOTConstants.NULL_KEY,//设备启动代码
)