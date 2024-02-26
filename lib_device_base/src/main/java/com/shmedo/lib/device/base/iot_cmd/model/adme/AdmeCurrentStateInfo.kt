package com.shmedo.lib.device.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/10/21 <br></br>
 * 描述：     ADME 设备当前状态
 */
data class AdmeCurrentStateInfo(
    var sn: String = "", //设备SN号
    var productid: String = "", //产品型号
    var simid: String = "", //物联网卡号
    var imeid: String = "", //IMEI卡号
    var firversion: String = "", //固件版本
    var ctrinputv: String = "0", //CTR驱动器输入电压
    var driveinputv: String = "0", //CTR驱动器输入电压
    var temperature: String = "0", //CTR驱动器温度
    var humidity: String = "0", //CTR驱动器湿度
    var inctype: String = "", //测斜仪类型（0：433测斜仪，1：蓝牙测斜仪）
    var incnum: String = "", //测斜仪信道号
    var incvoltage: String = "0", //测斜仪电压
    var intertempe: String = "0", //测斜仪管温度
    var signalstr: String = "0", //CTR 4G信号强度
    var incloc: String = "", //测斜仪位置信息
    var abndiasis: String = "", //设备异常诊断
    var downnum: String = "", //设备下降次数
    var runmileage: String = "0", //设备里程
    var nexttime: String = "0",//预计下次测量时间
    var testway: String = "", //工作模式(0:常规测量模式，1：特定点位模式，2：静态测量模式，3：设备停用模式)）
    var scsq: String = "0", //4G信号强度
    var bcsq: String = "0", //测斜仪蓝牙信号强度

    var verticalswitchnum: String = "0",//竖向磁开关触发次数
    var rotaryswitchnum: String = "0", //旋转磁开关触发次数
    var brakepadnum: String = "0", //刹车片启闭次数
)