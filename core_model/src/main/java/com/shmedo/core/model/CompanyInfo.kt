package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/7 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class CompanyInfo(
    val address: String = "",
    val companyGroupID: Int = 0,
    val companyGroupName: String = "",
    val companyPeopleCount: Int = 0,
    val createTime: String = "",
    val createUserID: Int = 0,
    val delete: Boolean = false,
    val desc: String = "",
    val displayOrder: Int = 0,
    val fullName: String = "",
    val hasChild: Boolean = false,
    val id: Int = 0,
    val industry: String = "",
    val legalPerson: String = "",
    val level: Int = 0,
    val nature: String = "",
    val parentID: Int = 0,
    val phone: String = "",
    val scale: String = "",
    val shortName: String = "",
    val updateTime: String = "",
    val updateUserID: Int = 0,
    val webSite: String = ""
)