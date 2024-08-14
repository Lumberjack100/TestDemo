package com.shmedo.lib.cmd.base.iot_cmd.model.mr

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     MR 设备的串口、模拟量接口状态
 */
data class MRInterfaceStatusInfo(
    var rs485_1: String = IOTConstants.NULL_KEY,//RS485状态  1 正常  0 异常
    var rs485_2: String = IOTConstants.NULL_KEY,//RS485状态  1 正常  0 异常
    var rs485_3: String = IOTConstants.NULL_KEY,//RS485状态  1 正常  0 异常
    var rs232_1: String = IOTConstants.NULL_KEY,//RS232状态  1 正常  0 异常
    var rs232_2: String = IOTConstants.NULL_KEY,//RS232状态  1 正常  0 异常
    var adc_a1: String = IOTConstants.NULL_KEY,//电流  范围4-20mA  保留3位有效位数
    var adc_a2: String = IOTConstants.NULL_KEY,//电流  范围4-20mA  保留3位有效位数
    var adc_a3: String = IOTConstants.NULL_KEY,//电流  范围4-20mA  保留3位有效位数
    var adc_a4: String = IOTConstants.NULL_KEY,//电流  范围4-20mA  保留3位有效位数
    var adc_v1: String = IOTConstants.NULL_KEY,//电压  范围0-5V  保留1位有效位数
    var adc_v2: String = IOTConstants.NULL_KEY,//电压  范围0-5V  保留1位有效位数
    var adc_v3: String = IOTConstants.NULL_KEY,//电压  范围0-5V  保留1位有效位数
    var adc_v4: String = IOTConstants.NULL_KEY,//电压  范围0-5V  保留1位有效位数
)
