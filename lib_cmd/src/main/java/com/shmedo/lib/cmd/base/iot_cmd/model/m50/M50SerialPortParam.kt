package com.shmedo.lib.cmd.base.iot_cmd.model.m50

/**
 * 创建者：gonghe
 * 创建时间：2024/9/10
 * 描述： M50 串口参数
 */
data class M50SerialPortParam(
    var out_power: String = "", //外部供电  开关  0 关闭 1 开启
    var rs232_mode: String = "", //232 外接设备 0：无  1：抓拍相机  2：卫星通信终端
    var cam_module: String = "", //抓拍频率(分钟)： "15", "30", "60", "120"
    var pixx: String = "", //抓拍图片水平分辨率
    var pixy: String = "", //抓拍图片垂直分辨率
    var rs485_mode: String = "", //485 外接设备  0：无  1：压电式雨量计
    var rs485_baud: String = "", //485 波特率
    var rs485_addr: String = "", //485 外接设备地址
)
