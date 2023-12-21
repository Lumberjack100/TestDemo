package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/6/21 <br></br>
 * 描述：    ADME的测量孔深配置参数
 */
@Parcelize
data class AdmeMeasuringHoleDepthInfo(
    var morunstate: String = "", //电机运行状态(0:停止，1:运动)
    var movementway: String = "", //运动方式（0:上拉，1:下放）
    var motorspeed: String = "", //电机速度
    var movedistance: String = "", //运动距离
) : Parcelable

