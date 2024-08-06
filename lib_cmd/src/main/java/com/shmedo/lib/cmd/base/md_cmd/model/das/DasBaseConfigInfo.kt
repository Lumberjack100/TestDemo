package com.shmedo.lib.cmd.base.md_cmd.model.das

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： TODO
 */
data class DasBaseConfigInfo(
    var token: String = "",//SN号
    var localBGNum: String = "",//本地北斗卡号
    var targetBGNum: String = "",//目标北斗卡号
    var activeStatus: String = "",//设备状态 1、待机  2、激活
    var dataCommunicateMode: String = "",//数据通讯模式  1、GPRS  2、SMS(短信息模式) 3、BD(北斗短报文模式) 4、BD+GPRS
    var rainStation: String = "",//雨量站 1、OPEN(开启) 2、CLOSE(关闭) 3、ALARM_OPEN(断线报警器打开)
    var rainAccuracy: String = "",//雨量计精度
    var locationSensitivity: String = "",//定位灵敏度
    var locationAccuracy: String = "",//定位精度
    var heartbeatTimeInterval: String = "",//心跳包时间间隔（单位s，为0表示关闭心跳功能）
    var debugBandRate: String = "",//调试口波特率
    var sensorBandRate: String = "",//传感器波特率
    var collectorModel: String = "",//采集器型号
    var dataReportInterval: String = "",//数据上报间隔
    var batteryOverProtect: String = "",//电池过放保护
    var workModel: String = "",//工作模式 0、INITIALZE(初始化模式) 1、WORK(工作模式) 2、DEBUG(debug模式) 3、INFO(info模式)
)
