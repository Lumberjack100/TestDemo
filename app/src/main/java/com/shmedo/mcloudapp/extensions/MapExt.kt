package com.shmedo.mcloudapp.extensions

import com.baidu.location.BDLocation
import com.baidu.mapapi.model.LatLng
import com.shmedo.mcloudapp.utils.map.CustomLatLng
import com.shmedo.mcloudapp.utils.map.JZLocationConverter

/**
 * 创建者：gonghe
 * 创建时间：2024/7/3
 * 描述：
 */


/**
 * 将GCJ-02火星坐标转换为WGS-84世界标准地理坐标
 */
fun BDLocation.toWgsLatLng(): LatLng {
    return JZLocationConverter.gcj02ToWgs84(CustomLatLng(latitude, longitude)).toLatLng()
}

/**
 * 将WGS-84世界标准地理坐标转换为GCJ-02火星坐标
 */
fun CustomLatLng.toGcj02LatLng(): LatLng {
    return JZLocationConverter.wgs84ToGcj02(this).toLatLng()
}

/**
 * 将WGS-84世界标准地理坐标转换为BD09l火星坐标
 */
fun CustomLatLng.toBD09lLatLng(): LatLng {
    return JZLocationConverter.wgs84ToBd09(this).toLatLng()
}

private fun CustomLatLng.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

