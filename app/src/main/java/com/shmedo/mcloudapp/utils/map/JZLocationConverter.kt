package com.shmedo.mcloudapp.utils.map

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/17/20 <br></br>
 * 描述：     WGS-84世界标准坐标、GCJ-02中国国测局(火星坐标)、BD-09百度坐标系转换
 */
object JZLocationConverter {
    private const val RANGE_LON_MAX = 137.8347
    private const val RANGE_LON_MIN = 72.004
    private const val RANGE_LAT_MAX = 55.8271
    private const val RANGE_LAT_MIN = 0.8293
    // Krasovsky 1940
    // 长半轴a = 6378245.0, 1/f = 298.3
    // b = a * (1 - f)
    // 扁率ee = (a^2 - b^2) / a^2;

    // 长半轴
    private const val SEMI_MAJOR = 6378245.0
    // 扁率
    private const val FLATTENING = 0.00669342162296594323

    private fun LAT_OFFSET_0(x: Double, y: Double): Double {
        return -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * sqrt(abs(x))
    }

    private fun LAT_OFFSET_1(x: Double, y: Double): Double {
        return (20.0 * sin(6.0 * x * Math.PI) + 20.0 * sin(2.0 * x * Math.PI)) * 2.0 / 3.0
    }

    private fun LAT_OFFSET_2(x: Double, y: Double): Double {
        return (20.0 * sin(y * Math.PI) + 40.0 * sin(y / 3.0 * Math.PI)) * 2.0 / 3.0
    }

    private fun LAT_OFFSET_3(x: Double, y: Double): Double {
        return (160.0 * sin(y / 12.0 * Math.PI) + 320 * sin(y * Math.PI / 30.0)) * 2.0 / 3.0
    }

    private fun LON_OFFSET_0(x: Double, y: Double): Double {
        return 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * sqrt(abs(x))
    }

    private fun LON_OFFSET_1(x: Double, y: Double): Double {
        return (20.0 * sin(6.0 * x * Math.PI) + 20.0 * sin(2.0 * x * Math.PI)) * 2.0 / 3.0
    }

    private fun LON_OFFSET_2(x: Double, y: Double): Double {
        return (20.0 * sin(x * Math.PI) + 40.0 * sin(x / 3.0 * Math.PI)) * 2.0 / 3.0
    }

    private fun LON_OFFSET_3(x: Double, y: Double): Double {
        return (150.0 * sin(x / 12.0 * Math.PI) + 300.0 * sin(x / 30.0 * Math.PI)) * 2.0 / 3.0
    }

    fun transformLat(x: Double, y: Double): Double {
        var ret = LAT_OFFSET_0(x, y)
        ret += LAT_OFFSET_1(x, y)
        ret += LAT_OFFSET_2(x, y)
        ret += LAT_OFFSET_3(x, y)
        return ret
    }

    fun transformLon(x: Double, y: Double): Double {
        var ret = LON_OFFSET_0(x, y)
        ret += LON_OFFSET_1(x, y)
        ret += LON_OFFSET_2(x, y)
        ret += LON_OFFSET_3(x, y)
        return ret
    }

    fun outOfChina(lat: Double, lon: Double): Boolean {
        if (lon < RANGE_LON_MIN || lon > RANGE_LON_MAX) return true
        return if (lat < RANGE_LAT_MIN || lat > RANGE_LAT_MAX) true else false
    }

    fun gcj02Encrypt(ggLat: Double, ggLon: Double): CustomLatLng {
        val resPoint = CustomLatLng()
        val mgLat: Double
        val mgLon: Double
        if (outOfChina(ggLat, ggLon)) {
            resPoint.latitude = ggLat
            resPoint.longitude = ggLon
            return resPoint
        }
        var dLat = transformLat(ggLon - 105.0, ggLat - 35.0)
        var dLon = transformLon(ggLon - 105.0, ggLat - 35.0)
        val radLat = ggLat / 180.0 * Math.PI
        var magic = sin(radLat)
        magic = 1 - FLATTENING * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat = dLat * 180.0 / (SEMI_MAJOR * (1 - FLATTENING) / (magic * sqrtMagic) * Math.PI)
        dLon = dLon * 180.0 / (SEMI_MAJOR / sqrtMagic * cos(radLat) * Math.PI)
        mgLat = ggLat + dLat
        mgLon = ggLon + dLon
        resPoint.latitude = mgLat
        resPoint.longitude = mgLon
        return resPoint
    }

    fun gcj02Decrypt(gjLat: Double, gjLon: Double): CustomLatLng {
        val gPt = gcj02Encrypt(gjLat, gjLon)
        val dLon = gPt.longitude - gjLon
        val dLat = gPt.latitude - gjLat
        val pt = CustomLatLng()
        pt.latitude = gjLat - dLat
        pt.longitude = gjLon - dLon
        return pt
    }

    fun bd09Decrypt(bdLat: Double, bdLon: Double): CustomLatLng {
        val gcjPt = CustomLatLng()
        val x = bdLon - 0.0065
        val y = bdLat - 0.006
        val z = sqrt(x * x + y * y) - 0.00002 * sin(y * Math.PI)
        val theta = atan2(y, x) - 0.000003 * cos(x * Math.PI)
        gcjPt.longitude = z * cos(theta)
        gcjPt.latitude = z * sin(theta)
        return gcjPt
    }

    fun bd09Encrypt(ggLat: Double, ggLon: Double): CustomLatLng {
        val bdPt = CustomLatLng()
        val z = sqrt(ggLon * ggLon + ggLat * ggLat) + 0.00002 * sin(ggLat * Math.PI)
        val theta = atan2(ggLat, ggLon) + 0.000003 * cos(ggLon * Math.PI)
        bdPt.longitude = z * cos(theta) + 0.0065
        bdPt.latitude = z * sin(theta) + 0.006
        return bdPt
    }

    /**
     * @param location 世界标准地理坐标(WGS-84)
     * @return 中国国测局地理坐标（GCJ-02）<火星坐标>
     * @brief 世界标准地理坐标(WGS-84) 转换成 中国国测局地理坐标（GCJ-02）<火星坐标>
     *
     * ####只在中国大陆的范围的坐标有效，以外直接返回世界标准坐标
    </火星坐标></火星坐标> */
    fun wgs84ToGcj02(location: CustomLatLng): CustomLatLng {
        return gcj02Encrypt(location.latitude, location.longitude)
    }

    /**
     * @param location 中国国测局地理坐标（GCJ-02）
     * @return 世界标准地理坐标（WGS-84）
     * @brief 中国国测局地理坐标（GCJ-02） 转换成 世界标准地理坐标（WGS-84）
     *
     * ####此接口有1－2米左右的误差，需要精确定位情景慎用
     */
    fun gcj02ToWgs84(location: CustomLatLng): CustomLatLng {
        return gcj02Decrypt(location.latitude, location.longitude)
    }

    /**
     * @param location 世界标准地理坐标(WGS-84)
     * @return 百度地理坐标（BD-09)
     * @brief 世界标准地理坐标(WGS-84) 转换成 百度地理坐标（BD-09)
     */
    fun wgs84ToBd09(location: CustomLatLng): CustomLatLng {
        val gcj02Pt = gcj02Encrypt(location.latitude, location.longitude)
        return bd09Encrypt(gcj02Pt.latitude, gcj02Pt.longitude)
    }

    /**
     * @param location 中国国测局地理坐标（GCJ-02）<火星坐标>
     * @return 百度地理坐标（BD-09)
     * @brief 中国国测局地理坐标（GCJ-02）<火星坐标> 转换成 百度地理坐标（BD-09)
    </火星坐标></火星坐标> */
    fun gcj02ToBd09(location: CustomLatLng): CustomLatLng {
        return bd09Encrypt(location.latitude, location.longitude)
    }

    /**
     * @param location 百度地理坐标（BD-09)
     * @return 中国国测局地理坐标（GCJ-02）<火星坐标>
     * @brief 百度地理坐标（BD-09) 转换成 中国国测局地理坐标（GCJ-02）<火星坐标>
    </火星坐标></火星坐标> */
    fun bd09ToGcj02(location: CustomLatLng): CustomLatLng {
        return bd09Decrypt(location.latitude, location.longitude)
    }

    /**
     * @param location 百度地理坐标（BD-09)
     * @return 世界标准地理坐标（WGS-84）
     * @brief 百度地理坐标（BD-09) 转换成 世界标准地理坐标（WGS-84）
     *
     * ####此接口有1－2米左右的误差，需要精确定位情景慎用
     */
    fun bd09ToWgs84(location: CustomLatLng): CustomLatLng {
        val gcj02 = bd09ToGcj02(location)
        return gcj02Decrypt(gcj02.latitude, gcj02.longitude)
    }
    
}

@Parcelize
data class CustomLatLng(var latitude: Double = 0.0, var longitude: Double = 0.0): Parcelable
