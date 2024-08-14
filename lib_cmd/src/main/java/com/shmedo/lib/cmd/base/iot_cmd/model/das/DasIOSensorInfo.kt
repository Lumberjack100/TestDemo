package com.shmedo.lib.cmd.base.iot_cmd.model.das

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/19 <br></br>
 * 描述：     开关量传感器参数
 */
@JsonClass(generateAdapter = true)
data class DasIOSensorInfo(
    var type: String = "", //0：关闭开关量功能 1：雨量站模式 2：断线报警器模式
    var value: String = "", //当type取1时，value代表雨量计精度  当type取2时，value代表断线报警器状态，0：常开，1：常关
    var min_time: String = "",//雨量计翻斗翻转最小间隔
)