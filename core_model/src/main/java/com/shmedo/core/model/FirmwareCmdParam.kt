package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/12/23 <br></br>
 * 描述：     固件升级指令参数
 */
@JsonClass(generateAdapter = true)
class FirmwareCmdParam(
    val deviceTokenList: List<String> = arrayListOf(), //设备SN号列表
    val firmwareID: Int = 0 //产品固件编号
)
