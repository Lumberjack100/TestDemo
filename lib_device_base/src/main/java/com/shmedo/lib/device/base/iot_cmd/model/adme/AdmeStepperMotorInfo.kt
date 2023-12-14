package com.shmedo.lib.device.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/28/20 <br></br>
 * 描述：    ADME 步进电机参数
 */
data class AdmeStepperMotorInfo(
    var posnegtest: String = "", //正反测（0:关闭，1:开启）
    var absprsion: String = "", //绝对精度修正值
    var movspeed: String = "", //步进电机运动速度
    var movesm: String = "", //步进电机力矩
)