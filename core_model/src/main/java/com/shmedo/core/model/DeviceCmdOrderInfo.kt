package com.shmedo.core.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2024/11/26 <br/>
 * 描述：    设备配置指令序列信息
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class DeviceCmdOrderInfo(
    val productName: String = "",
    val productId: Int = 0,
    val createTime: String = "",
    val lastModifyDateTime: String = "",
    @Json(name = "cmdOrderInfos")
    val dynamicCmds: List<DynamicCmdInfo> = listOf(),
    val fixedCmds: List<FixedCmdInfo> = listOf()
) : Parcelable

/**
 * 动态参数指令信息
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class DynamicCmdInfo(
    val note: String = "",
    val cmdOrder: String = "",
    val orderIndex: Int = 0,
    val cmdParaInfos: List<CmdParamInfo> = listOf()
) : Parcelable

/**
 * 指令参数信息
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class CmdParamInfo(
    val cmdChnName: String = "", // 中文名称
    val cmdEngName: String = "", // 英文名称
    val defaultValue: String = "", // 默认值
    val fieldType: String = "", // 字段类型：字符、选择等
    val fieldValueInfos: List<FieldValueInfo>? = null // 选择类型的可选值列表
) : Parcelable

/**
 * 字段值信息
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FieldValueInfo(
    val display: String = "", // 显示文本
    val value: String = "" // 实际值
) : Parcelable

/**
 * 固定参数指令信息
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FixedCmdInfo(
    val cmd: String = "",
    val orderIndex: Int = 0
) : Parcelable