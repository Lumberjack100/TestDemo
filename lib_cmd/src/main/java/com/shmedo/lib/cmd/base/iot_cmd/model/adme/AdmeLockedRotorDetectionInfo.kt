package com.shmedo.lib.cmd.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  3/2/21 <br></br>
 * 描述：     ADME的电机运动堵转缓停参数
 */
data class AdmeLockedRotorDetectionInfo (
    var lowtbtss: String = "", //下放堵转缓停（0:关闭，1:开启）
    var numpput: String = "", //单位时间脉冲数
    var pdajtime : String = "",//脉冲检测判断时间
    var lowsusrana: String = "", //下放缓停区间起始值
    var lowsusranb: String = "", //下放缓起区间终值
    var detintiona: String = "", //堵转检测区间起始值
    var detintionb : String = "",//堵转检测区间终值
    var lowtorblothr : String = "",//下放力矩堵转阈值
    var lowtordetime: String = "", //下放力矩检测判断时间

    var uptbtss : String = "",//上拉堵转缓停（0:关闭，1:开启）
    var upsusrana : String = "",//上拉缓停区间起始值
    var upsusranb: String = "", //上拉缓起区间终值
    var uptorblothr: String = "", //上拉力矩堵转阈值
    var uptordetime: String = "", //上拉力矩检测判断时间
    var holedepth: String = "", //下放距离
    var measpacing: String = "", //上拉测量间距
)