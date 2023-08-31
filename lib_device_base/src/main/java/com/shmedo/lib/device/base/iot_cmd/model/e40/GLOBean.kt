package com.shmedo.lib.device.base.iot_cmd.model.e40

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/24 <br></br>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
class GLOBean {
     @Json(name ="SAT")
    var sAT: String? = null

     @Json(name ="AZ")
    var aZ = 0.0

     @Json(name ="EL")
    var eL = 0.0

     @Json(name ="L1")
    var l1 = 0

     @Json(name ="L2")
    var l2 = 0

     @Json(name ="L3")
    var l3 = 0
}