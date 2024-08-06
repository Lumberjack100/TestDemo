package com.shmedo.lib.cmd.base.iot_cmd.model.hac

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/25 <br></br>
 * 描述：     HAC 电机实时运动脉冲数据
 */
data class HacMotorMotionDistanceInfo(
    var pulsenumber: String = "",//脉冲数
    var realmovedistance: String = "",//实时运动距离
    var realholedepth: String = "",// 实时测量孔深
    var recoholedepth: String = "",//推荐测量孔深
    var abndiasis: String = "",// 设备异常诊断
)