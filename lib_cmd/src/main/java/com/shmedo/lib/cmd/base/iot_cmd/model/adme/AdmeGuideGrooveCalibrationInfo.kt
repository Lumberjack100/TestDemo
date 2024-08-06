package com.shmedo.lib.cmd.base.iot_cmd.model.adme

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/21
 *
 * 描述： TODO
 *
 *
 */
@Parcelize
data class AdmeGuideGrooveCalibrationInfo(
    var morunstate: String = "", //电机运行状态(0:停止，1:运动)
    var movementway: String = "", //运动方式（0:正转，1:反转）
    var motorspeed: String = "", //电机速度
    var movepulse: String = "", //运动脉冲
) : Parcelable