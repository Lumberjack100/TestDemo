package com.shmedo.lib.network.response

import com.squareup.moshi.JsonClass


/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/29
 *
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class PageList<T>(
    var totalCount: Int = 0,
    var totalPage: Int = 0,
    var currentPageData: List<T>? = null
)