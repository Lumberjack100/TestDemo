package com.shmedo.lib.cmd.base.iot_cmd.model.adme

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/10/21 <br></br>
 * 描述：     ADME 设备当前状态
 */
data class AdmeCurrentStateInfo(
    var sn: String = IOTConstants.NULL_KEY, //设备SN号
    var productid: String = IOTConstants.NULL_KEY, //产品型号
    var simid: String = IOTConstants.NULL_KEY, //物联网卡号
    var imeid: String = IOTConstants.NULL_KEY, //IMEI卡号
    var firversion: String = IOTConstants.NULL_KEY, //固件版本
    var ctrinputv: String = IOTConstants.NULL_KEY, //CTR驱动器输入电压
    var driveinputv: String = IOTConstants.NULL_KEY, //CTR驱动器输入电压
    var temperature: String = IOTConstants.NULL_KEY, //CTR驱动器温度
    var humidity: String = IOTConstants.NULL_KEY, //CTR驱动器湿度
    var inctype: String = IOTConstants.NULL_KEY, //测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    var incnum: String = IOTConstants.NULL_KEY, //测斜仪信道号
    var incvoltage: String = IOTConstants.NULL_KEY, //测斜仪电压
    var intertempe: String = IOTConstants.NULL_KEY, //测斜仪管温度
    var signalstr: String = IOTConstants.NULL_KEY, //CTR 4G信号强度
    var incloc: String = IOTConstants.NULL_KEY, //测斜仪位置信息
    var abndiasis: String = IOTConstants.NULL_KEY, //设备异常诊断
    var downnum: String = IOTConstants.NULL_KEY, //设备下降次数
    var runmileage: String = IOTConstants.NULL_KEY, //设备里程
    var nexttime: String = IOTConstants.NULL_KEY,//预计下次测量时间
    var testway: String = IOTConstants.NULL_KEY, //工作模式(0:常规测量模式，1：特定点位模式，2：静态测量模式，3：设备停用模式)）
    var scsq: String = IOTConstants.NULL_KEY, //4G信号强度
    var bcsq: String = IOTConstants.NULL_KEY, //测斜仪蓝牙信号强度

    var verticalswitchnum: String = IOTConstants.NULL_KEY,//竖向磁开关触发次数
    var rotaryswitchnum: String = IOTConstants.NULL_KEY, //旋转磁开关触发次数
    var brakepadnum: String = IOTConstants.NULL_KEY, //刹车片启闭次数
)