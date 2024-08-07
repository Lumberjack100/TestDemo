package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/30 <br></br>
 * 描述：    指令透传接口入参
 */
@JsonClass(generateAdapter = true)
class DispatchRawCmdParam(
    val cmdContent: String = "", //透传内容，须以$cmd=开头
    val deviceTokenList: List<String> = arrayListOf() //设备SN号列表
)
