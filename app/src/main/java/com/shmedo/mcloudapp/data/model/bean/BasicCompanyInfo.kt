package com.shmedo.mcloudapp.data.model.bean

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe
 *
 * 创建时间:  2020/9/19
 *
 * 描述：    公司简单信息实体
 *
 * 以下是示例 JSON 数据:
 *
 * {
 *   "companyID": 138,
 *   "companyName": "上海米度测控科技有限公司"
 * }
 *
 */
@JsonClass(generateAdapter = true)
data class BasicCompanyInfo(
    val companyID: Int = 0,
    var companyName: String = "",
    @Json(ignore = true)
    var isChecked: Boolean = false,
)
