package com.shmedo.mcloudapp.data.model.bean

import com.squareup.moshi.JsonClass

/**
 * 项目名：  mobileAndroid
 * 包名：    com.shmedo.mobileandroid.entity
 * 文件名:   UserInfo
 * 创建者:   dpc
 * 创建时间:  2018/11/27 14:48
 */
@JsonClass(generateAdapter = true)
data class UserWrapperInfo(
    var user: UserInfo? = null,
    var departments: List<DepartmentInfo>? = ArrayList()
)