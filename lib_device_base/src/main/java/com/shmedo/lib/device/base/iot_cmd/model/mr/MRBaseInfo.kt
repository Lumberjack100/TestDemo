package com.shmedo.lib.device.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/27/21 <br></br>
 * 描述：     MR 设备的基本信息
 */
data class MRBaseInfo(
    var productname: String = "",//产品名称
    var producttype: String = "", //产品型号
    var regcode: String = "",//产品注册码
    var sn: String = "",
    var ver: String = "",//软件版本
    var imei: String = "",//SIM卡号
    var temp: String = "",//温度 保留一位小数位数
    var hum: String = "",//湿度 保留一位小数位数
    var volt: String = "",//供电电压  保留一位小数位数  供电电压是设备自检出来的电压值
    var csq: String = "",//4G信号强度  1(优)，2(良好)，3(较差)
    var local: String = "",//设备位置 经度,纬度
    var regtime: String = "",//注册时间
)