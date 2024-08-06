package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/20 <br/>
 * 描述：     RS485-3-串口下传感器参数
 */
data class MRRS485Port3SensorParam(
    var device: String = "",//设备类型  1太阳能控制器  2 声光报警器  3 LED屏
    var switch: String = "",//开关  1 开 0关
    var addr: String = "",//设置地址  数字
    var baud: String = "",//波特率  bps 数字
    var databit: String = "",//数据位   数字(5 6 7 8)
    var paritybit: String = "",//校验位 0:NONE  1:ODD  2:EVEN  3:MARK 4:SPACE
    var stopbit: String = "",//停止位   0: 1  1: 1.5  2: 2  或 数字(1 1.5 2)
    var status: String = "",//状态  1 接入 0未接入
    var duration: String = "",//声光报警器(语音播放时长  s 数字)、LED屏(显示时长  s 数字)
    var interval: String = "",//声光报警器(切换间隔 ms(s) 数字)、LED屏(更新间隔  s 数字)

    //太阳能控制器
    var svolt: String = "",//太阳能板电压  V 数字
    var bvolt: String = "",//电池电压   V 数字
    var spower: String = "",//太阳能板功率   W 数字
    var lpower: String = "",//负载功率  W 数字

    //声光报警器
    var volume: String = "",//音量   数字  百分比
    var rlevel1: String = "",//降雨量一级报警值 mm 数字
    var rlevel2: String = "",//降雨量二级报警值 mm 数字
    var rlevel3: String = "",//降雨量三级报警值 mm 数字
    var wlevel1: String = "",//水位一级报警值 mm 数字
    var wlevel2: String = "",//水位二级报警值 mm 数字
    var wlevel3: String = "",//水位三级报警值 mm 数字

    //LED屏
    var type: String = "",//显示配置   1 类型1  2 类型2
    var stime: String = "",//亮屏时间 s  数字
)
