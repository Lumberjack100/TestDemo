package com.shmedo.core.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2024/11/26 <br/>
 * 描述：    设备命令订单信息
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class DeviceCmdOrderInfo(
    val productName: String = "",
    val productId: Int = 0,
    val createTime: String = "",
    val lastModifyDateTime: String = "",
    val cmdOrderInfos: List<CmdOrderInfo> = listOf(),
    val fixedCmds: List<FixedCmd> = listOf()
) : Parcelable

/**
 * 命令订单信息
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class CmdOrderInfo(
    val note: String = "",
    val cmdOrder: String = "",
    val orderIndex: Int = 0,
    val cmdParaInfos: List<CmdParaInfo> = listOf()
) : Parcelable

/**
 * 命令参数信息
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class CmdParaInfo(
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
 * 固定命令
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FixedCmd(
    val cmd: String = "",
    val orderIndex: Int = 0
) : Parcelable