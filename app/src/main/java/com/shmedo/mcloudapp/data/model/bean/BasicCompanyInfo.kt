package com.shmedo.mcloudapp.data.model.bean

import com.squareup.moshi.Json

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/9/19 <br></br>
 * 描述：    公司简单信息实体
 *
 * <br></br>
 * <pre>
 * 以下是示例 JSON 数据:
 * {
 *   "companyID": 138,
 *   "companyName": "上海米度测控科技有限公司"
 * }
 * </pre>
 */
data class BasicCompanyInfo(
    val companyID: Int = 0,
    var companyName: String = "",
    @Json(ignore = true)
    var isChecked: Boolean = false,
)
