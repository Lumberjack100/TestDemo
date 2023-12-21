package com.shmedo.lib.device.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/8/21 <br></br>
 * 描述：      ADME电机实时运动数据
 */
data class AdmeMotorMotionAngleInfo(
    var pulsenumber: String = "",//脉冲数
    var realmoveangle: String = "",//实时运动角度
)
