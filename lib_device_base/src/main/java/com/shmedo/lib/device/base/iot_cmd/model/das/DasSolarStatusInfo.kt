package com.shmedo.lib.device.base.iot_cmd.model.das

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/15 <br></br>
 * 描述：        DAS 状态页面太阳能控制器状态
 */
@JsonClass(generateAdapter = true)
data class DasSolarStatusInfo(
    val solar: SolarBean = SolarBean()
)