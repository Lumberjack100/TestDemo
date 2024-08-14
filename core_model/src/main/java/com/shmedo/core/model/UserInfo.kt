package com.shmedo.core.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/29
 *
 * 描述： TODO
 *
 *
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class UserInfo(
    val userID: Int = 0,
    val companyID: Int = 0,
    val companyName: String = "",
    val account: String = "",
    val name: String = "",
    val cellPhone: String = "",
    val position: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val allowAccessType: Int = 0,
    val headPhotoPath: String = "",
    val isUserEnable: Boolean = false,
    val createUserID: Int = 0,
    val createTime: String = "",
    val updateUserID: Int = 0,
    val updateTime: String = "",
    val expireTime: String = "",
    val ssoUser: Boolean = false,
    val ssoToken: String = ""
) : Parcelable
