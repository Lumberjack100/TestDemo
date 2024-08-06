package com.shmedo.lib.cmd.base.iot_cmd.model.adme

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/28/20 <br></br>
 * 描述：     检校、数据处理信息
 */
@JsonClass(generateAdapter = true)
data class AdmeCalibrationProcessingInfo(
    var rivswitch: String = "", //运动信息校验开关（0:关  1:开）
    var zerodiffer: String = "", //归零差值 mm
    var posterrorint: String = "", //定位误差区间值 mm
    var rtrynum: String = "", //尝试次数

    var kcswitch: String = "", //k值校验开关（0:关  1:开）
    var kthreshold: String = "", //k值阈值  mm
    var methreshold: String = "", //中误差阈值  mm
    var ktrynum: String = "", //尝试次数

    var dpswitch: String = "", //数据处理开关（0:关  1:开）
    var accudiff: String = "", //累积值差值  mm
)