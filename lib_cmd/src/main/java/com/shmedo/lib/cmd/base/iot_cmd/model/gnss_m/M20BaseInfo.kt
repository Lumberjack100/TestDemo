package com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/27/21 <br></br>
 * 描述：     M20的基本信息
 */
data class M20BaseInfo(
    var sn: String = "",
    var productid: String = "", //产品型号
    var firversion: String = "" //固件版本
)