package com.shmedo.mcloudapp.extensions

import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType

/**
 * 创建者：gonghe
 * 创建时间：2025/10/24
 * 描述： 产品类型校验
 */

 /**
  * 判断是否为 M50 系列产品
  * @return 是否为 M50 系列产品
  */
 fun ProductType.isM50Series(): Boolean {
    return this == ProductType.GNSS_M_5 || this == ProductType.GNSS_M_6 || this == ProductType.GNSS_M_7 || this == ProductType.GNSS_M_8
 }


 /**
  * 判断是否为 UD 系列产品
  * @return 是否为 UD 系列产品
  */
 fun ProductType.isUDSeries(): Boolean {
    return this == ProductType.U_D_1 || this == ProductType.U_D_2 || this == ProductType.U_D_3
 }


/**
 * 判断是否为 DR030(雷达水位计) 系列产品
 * @return 是否为 DR030 系列产品
 */
fun ProductType.isDR030Series(): Boolean {
    return this == ProductType.U_D_1 || this == ProductType.U_D_2 
}

/**
 * 判断是否为 LL030(雷达流量计) 系列产品
 * @return 是否为 LL030 系列产品
 */
fun ProductType.isLL030Series(): Boolean {
    return this == ProductType.U_D_3
}