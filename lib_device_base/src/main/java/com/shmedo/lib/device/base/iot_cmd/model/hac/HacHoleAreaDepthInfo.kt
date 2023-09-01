package com.shmedo.lib.device.base.iot_cmd.model.hac

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/11 <br></br>
 * 描述：     孔号、区号、孔深对应实体类
 */
@JsonClass(generateAdapter = true)
class HacHoleAreaDepthInfo(
    var holeno: String = "", //孔号
    var areano: String = "",//区号
    var holedepth: String = "", // 孔深
    var measdepth: String = "" // 孔深

)