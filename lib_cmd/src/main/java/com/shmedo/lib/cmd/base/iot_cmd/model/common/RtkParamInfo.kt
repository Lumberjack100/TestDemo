package com.shmedo.lib.cmd.base.iot_cmd.model.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/24
 * 描述： GNSS-RTK模式参数信息
 */
data class RtkParamInfo(
    var mode: String = "", //当前gnss模式 1:基站  2:测站  默认2
    var sw: String = "", //解算盒子使用配置开关 0:停止使用解算盒子  1:开始使用解算盒子  默认0
    var frontCalc: String = "", //前端解算开关 0:关闭前端解算  1:打开前端解算  2:根据网络状态开启前端解算  默认2
    var baseStationMode: String = "", //基站坐标模式 0:以精确坐标设置基站模式  1:以自主优化方式设置基准站模式  默认0
    var latitude: String = "", //纬度 基站纬度  （-90~90） 单位 度
    var longitude: String = "", //经度 基站经度  （-180~180）单位 度
    var height: String = "", //海拔高度 基站高度  （-30000~30000）单位米
    var distance: String = "", //距离 自主优化距离 （0~10） 单位 米
    var time: String = "", //gnss前端解算处理时间 自主优化时间 单位 秒
    var id: String = "", //基准站ID号 用于作移动站、基站细分
    var gateAngleVal1: String = "", //倾角报警一级阈值 单位 度
    var gateAngleVal2: String = "", //倾角报警二级阈值
    var gateAngleVal3: String = "", //倾角报警三级阈值
    var gateAngleVal4: String = "", //倾角报警四级阈值
    var gateDevVal1: String = "", //位移报警一级阈值  单位 mm
    var gateDevVal2: String = "", //位移报警二级阈值
    var gateDevVal3: String = "", //位移报警三级阈值
    var gateDevVal4: String = "", //位移报警四级阈值
    var rtkMode: String = "", //解算模式源 1:静态解算  2:动态结算  默认1
    var obs: String = "", //观测数据上报频率 取值为[0-60]
    var alarmSwitch: String = "", //报警开关 0:关闭  1:打开
    var reportMode: String = "", //上报模式  0:常在线  1:低功耗
)
