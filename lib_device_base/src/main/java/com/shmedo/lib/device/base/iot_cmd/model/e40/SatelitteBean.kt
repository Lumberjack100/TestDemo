package com.shmedo.lib.device.base.iot_cmd.model.e40

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/24 <br></br>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
class SatelitteBean {
    @Json(name = "GPS")
    var gpsBeanList: List<GPSBean>? = null
        get() = if (field == null) ArrayList() else field

    @Json(name = "GLO")
    var gloBeanList: List<GLOBean>? = null
        get() = if (field == null) ArrayList() else field

    @Json(name = "BDS")
    var bdsBeanList: List<BDSBean>? = null
        get() = if (field == null) ArrayList() else field
}