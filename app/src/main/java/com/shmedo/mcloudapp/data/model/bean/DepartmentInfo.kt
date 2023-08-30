package com.shmedo.mcloudapp.data.model.bean

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
    var id: Int = 0,
    var name: String? = "",
    var companyID: Int = 0,
    var parentID: Int = 0,
    var desc: String? = "",
    var level: Int = 0,
    var isReadOnly: Boolean = false,
    var displayOrder: Int = 0,
    var createUserID: Int = 0,
    var createTime: String? = "",
    var updateUserID: Int = 0,
    var updateTime: String? = "",
    var isHasChild: Boolean = false,
    var isDelete: Boolean = false
)