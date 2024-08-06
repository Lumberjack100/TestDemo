package com.shmedo.lib.cmd.base.iot_cmd.model.das

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/19 <br></br>
 * 描述：    数字水位计参数
 */
@JsonClass(generateAdapter = true)
data class DasDigitalPiezometerInfo(
    var sw: String = "", //0：关闭数字水位计采集功能 1：打开数字水位计采集功能
    var addr: String = "", //地址
    var threshold: String = "", //触发阈值
    var corrval: String = "",  //修正值
    var ropelen: String = "",  //绳长（渗压计到管口的距离）
    var tubealti: String = "",  //安装高程
)