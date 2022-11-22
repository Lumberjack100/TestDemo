package com.shmedo.configlibrary.iot.model.e40

import com.google.gson.annotations.SerializedName

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/24 <br></br>
 * 描述：     TODO
 */
class SatelitteBean {
    @SerializedName("GPS")
    var gpsBeanList: List<GPSBean>? = null
        get() = if (field == null) ArrayList() else field

    @SerializedName("GLO")
    var gloBeanList: List<GLOBean>? = null
        get() = if (field == null) ArrayList() else field

    @SerializedName("BDS")
    var bdsBeanList: List<BDSBean>? = null
        get() = if (field == null) ArrayList() else field
}