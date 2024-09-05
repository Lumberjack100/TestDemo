package com.shmedo.mcloudapp.extensions

import com.amap.api.location.AMapLocation
import com.amap.api.maps.model.LatLng
import com.shmedo.mcloudapp.utils.map.CustomLatLng
import com.shmedo.mcloudapp.utils.map.JZLocationConverter

/**
 * 创建者：gonghe
 * 创建时间：2024/7/3
 * 描述：
 */


/**
 * 将高德坐标(即GCJ-02火星坐标)转换为WGS-84世界标准地理坐标
 */
fun AMapLocation.toWgsLatLng(): LatLng {
    return JZLocationConverter.gcj02ToWgs84(CustomLatLng(latitude, longitude)).toLatLng()
}

/**
 * 将WGS-84世界标准地理坐标转换为高德坐标(即GCJ-02火星坐标)
 */
fun CustomLatLng.toGcj02LatLng(): LatLng {
    return JZLocationConverter.wgs84ToGcj02(this).toLatLng()
}

private fun CustomLatLng.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

fun Int.toGpsSignalLevel(): Int {
    return when (this) {
        AMapLocation.GPS_ACCURACY_GOOD -> 4
        AMapLocation.GPS_ACCURACY_BAD -> 2
        AMapLocation.GPS_ACCURACY_UNKNOWN -> 1
        else -> 1
    }
}
