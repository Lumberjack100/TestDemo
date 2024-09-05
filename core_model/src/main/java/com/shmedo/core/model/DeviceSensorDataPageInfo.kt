package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/9/3
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class DeviceSensorDataPageInfo(
    val pageResult: PageResultInfo? = null,//
)

@JsonClass(generateAdapter = true)
data class PageResultInfo(
    var totalCount: Int = 0,
    var totalPage: Int = 0,
    var currentPageData: List<Map<String, String>>? = null
)
