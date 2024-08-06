package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/27 <br/>
 * 描述：     终端参数-本机屏幕
 */
data class MRScreenParam(
    var interval: String = "",//屏幕更新周期  秒 ,数字
    var otime: String = "",//亮屏时间   秒 ,数字
    var ptime: String = "",//通电时间   秒 ,数字
    var bproport: String = "10",//屏幕亮度设置  数字(10-100), 转换比例  10%-100%
)
