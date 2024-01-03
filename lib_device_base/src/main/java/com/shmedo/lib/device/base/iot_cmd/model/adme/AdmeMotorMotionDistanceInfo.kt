package com.shmedo.lib.device.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/7/21 <br></br>
 * 描述：     ADME电机实时运动数据
 */
data class AdmeMotorMotionDistanceInfo(
    var pulsenumber: String = "",//脉冲数
    var realmovedistance: String = "",//实时运动距离
    var realholedepth: String = "",//实时测量孔深
    var recoholedepth: String = "",//推荐测量孔深
    var realmoveangle: String = "",//实时运动角度
)