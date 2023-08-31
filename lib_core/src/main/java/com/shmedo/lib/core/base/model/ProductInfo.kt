package com.shmedo.lib.core.base.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/12/23 <br></br>
 * 描述：     产品信息
 */
@JsonClass(generateAdapter = true)
class ProductInfo(
    val id: Int = 0,
    val productName: String = "",
    val productType: String = "",
    val productTypeChName: String = "",
    val useProtocol: String = "",
    val deviceNum: Int = 0,
    val createTime: String = "",
    val productToken: String = "",
    val companyID: Int = 0,
    val tagList: String = "",
    val productSource: Int = 0,
    val dataProtocol: String = "",
    val dataCode: String = "",
    val sensorNum: Int = 0,
    val propertyNum: Int = 0,
    val eventNum: Int = 0,
    val servicesNum: Int = 0,
    val modelNum: Int = 0,
    @Json(ignore = true)
    var isChecked: Boolean = false
) : Comparable<ProductInfo> {

    override fun compareTo(obj: ProductInfo): Int {
        //定义一个中文排序器
//        Comparator collator = Collator.getInstance(Locale.CHINA);
//        int lastCmp = this.productToken.compareTo(obj.getProductToken());
//        return collator.compare(getProductName(), obj.getProductName());
        return productName.compareTo(obj.productName)
    }
}