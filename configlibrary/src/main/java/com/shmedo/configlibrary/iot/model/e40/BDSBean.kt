package com.shmedo.configlibrary.iot.model.e40

import com.google.gson.annotations.SerializedName

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/24 <br></br>
 * 描述：     TODO
 */
class BDSBean {
    @SerializedName("SAT")
    var sAT: String? = null

    @SerializedName("AZ")
    var aZ = 0.0

    @SerializedName("EL")
    var eL = 0.0

    @SerializedName("L1")
    var l1 = 0

    @SerializedName("L2")
    var l2 = 0

    @SerializedName("L3")
    var l3 = 0
}