package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/11/26
 * 描述： ADME 配置信息
 */
@JsonClass(generateAdapter = true)
data class AdmeConfigInfo(
    val id: Int, // 375
    val projectID: String, //项目号  "PRJ001"
    val areaNumber: String, // 区域号 "A001"
    val holeNumber: String, // 孔号 "H001"
    val config: String, // 配置信息
    val exValues: String, // 扩展属性
    val createTime: String, // 创建时间
    val updateTime: String // 更新时间
)
