package com.shmedo.lib.core.base.model

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/12/22 <br></br>
 * 描述：     基本用户信息
 */
@JsonClass(generateAdapter = true)
data class BasicUserInfo(
    val subjectID: Int = 0,//用户ID
    val subjectName: String = "",//用户名称
    val companyID: Int = 0,//公司ID
    val subjectType: String = "",//类型,默认USER
    val imageUrl: String = "",//用户头像
    val phone: String = "",//用户电话
    val email: String = ""//用户名称
)