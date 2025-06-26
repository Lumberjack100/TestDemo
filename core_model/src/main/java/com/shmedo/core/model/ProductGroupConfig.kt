package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2025/6/23
 * 描述： 产品分组配置数据类
 */
@JsonClass(generateAdapter = true)
data class ProductGroupConfig(
    val productGroup: String,
    val children: MutableList<SubProductConfig> = arrayListOf(),
)

@JsonClass(generateAdapter = true)
data class SubProductConfig(
    val productSeriesName: String,
    val productIdList: MutableList<Int> = arrayListOf(),
)
