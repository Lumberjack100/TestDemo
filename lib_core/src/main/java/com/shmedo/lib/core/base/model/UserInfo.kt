package com.shmedo.lib.core.base.model

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
data class UserInfo(
    var userID: Int = 0,
    var companyID: Int = 0,
    var companyName: String? = "",
    var account: String? = "",
    var name: String? = "",
    var cellPhone: String? = "",
    var position: String? = "",
    var email: String? = "",
    var phone: String? = "",
    var address: String? = "",
    var allowAccessType: Int = 0,
    var headPhotoPath: String? = "",
    var isUserEnable: Boolean = false,
    var createUserID: Int = 0,
    var createTime: String? = "",
    var updateUserID: Int = 0,
    var updateTime: String? = "",
    val expireTime: String? = "",
    val ssoUser: Boolean = false,
    val ssoToken: String? = ""
) : java.io.Serializable
