package com.shmedo.lib.cmd.base.iot_cmd.model.das

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/15 <br></br>
 * 描述：      DAS 状态页面辅传感器状态
 */
@JsonClass(generateAdapter = true)
data class DasSubSensorStatusInfo(
    val io: IoBean? = null,
    var vwp: VwpBean? = null,
    var mems: MemsBean? = null,
)