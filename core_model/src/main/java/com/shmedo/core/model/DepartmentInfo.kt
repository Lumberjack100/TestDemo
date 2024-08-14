package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/29
 *
 * 描述： TODO
 *
 *
 */
@JsonClass(generateAdapter = true)
data class DepartmentInfo(
    val id: Int = 0,
    val name: String = "",
    val companyID: Int = 0,
    val parentID: Int = 0,
    val desc: String = "",
    val level: Int = 0,
    val isReadOnly: Boolean = false,
    val displayOrder: Int = 0,
    val createUserID: Int = 0,
    val createTime: String = "",
    val updateUserID: Int = 0,
    val updateTime: String = "",
    val isHasChild: Boolean = false,
    val isDelete: Boolean = false
)