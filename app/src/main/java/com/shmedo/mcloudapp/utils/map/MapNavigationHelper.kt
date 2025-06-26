package com.shmedo.mcloudapp.utils.map

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.model.LatLng
import com.hjq.toast.Toaster
import timber.log.Timber

/**
 * @author: gonghe
 * @time: 2024/12/23
 * @desc: 地图导航工具类
 */
object MapNavigationHelper {

    // 地图应用包名
    private const val BAIDU_MAP_PACKAGE = "com.baidu.BaiduMap"
    private const val GAODE_MAP_PACKAGE = "com.autonavi.minimap"
    private const val TENCENT_MAP_PACKAGE = "com.tencent.map"
    private const val GOOGLE_MAP_PACKAGE = "com.google.android.apps.maps"

    data class MapApp(
        val name: String,
        val packageName: String,
        val navigateAction: (Context, LatLng?, LatLng, String) -> Unit
    )

    private val mapApps = listOf(
        MapApp("百度地图", BAIDU_MAP_PACKAGE) { context, origin, destination, destinationName ->
            navigateWithBaidu(context, origin, destination, destinationName)
        },
        MapApp("高德地图", GAODE_MAP_PACKAGE) { context, origin, destination, destinationName ->
            navigateWithGaode(context, origin, destination, destinationName)
        },
        MapApp("腾讯地图", TENCENT_MAP_PACKAGE) { context, origin, destination, destinationName ->
            navigateWithTencent(context, origin, destination, destinationName)
        },
        MapApp("谷歌地图", GOOGLE_MAP_PACKAGE) { context, origin, destination, destinationName ->
            navigateWithGoogle(context, origin, destination, destinationName)
        }
    )

    /**
     * 显示地图应用选择对话框并导航
     * @param context 上下文
     * @param destinationLatLng 目的地坐标（GCJ-02坐标系）
     * @param destinationName 目的地名称
     */
    fun showMapAppSelector(
        context: Context,
        destinationLatLng: LatLng,
        destinationName: String = "设备位置"
    ) {
        val installedMapApps = getInstalledMapApps(context)

        if (installedMapApps.isEmpty()) {
            // 没有找到已安装的地图应用，尝试使用隐式Intent
            navigateWithImplicitIntent(context, destinationLatLng, destinationName)
            return
        }

        if (installedMapApps.size == 1) {
            // 只有一个地图应用，直接打开
            val mapApp = installedMapApps[0]
            mapApp.navigateAction(context, null, destinationLatLng, destinationName)
        } else {
            // 多个地图应用，显示选择对话框
            showMapAppDialog(
                context,
                installedMapApps,
                null,
                destinationLatLng,
                destinationName
            )
        }
    }

    /**
     * 获取已安装的地图应用列表
     */
    private fun getInstalledMapApps(context: Context): List<MapApp> {
        val packageManager = context.packageManager
        return mapApps.filter { mapApp ->
            try {
                packageManager.getPackageInfo(mapApp.packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    /**
     * 显示地图应用选择对话框
     */
    private fun showMapAppDialog(
        context: Context,
        installedMapApps: List<MapApp>,
        currentLocation: LatLng?,
        destinationLatLng: LatLng,
        destinationName: String
    ) {
        val appNames = installedMapApps.map { it.name }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("选择地图应用")
            .setItems(appNames) { _, which ->
                val selectedApp = installedMapApps[which]
                selectedApp.navigateAction(
                    context,
                    currentLocation,
                    destinationLatLng,
                    destinationName
                )
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 获取当前位置
     */
    private fun getCurrentLocation(context: Context, callback: (LatLng?) -> Unit) {
        try {
            val locationClient = LocationClient(context)
            val option = LocationClientOption().apply {
                setIsNeedAddress(false)
                isOpenGps = true
                coorType = "gcj02" // 设置返回的定位结果坐标系为GCJ02
                setScanSpan(0) // 单次定位
                locationMode = LocationClientOption.LocationMode.Hight_Accuracy
            }
            locationClient.locOption = option

            locationClient.registerLocationListener(object : BDAbstractLocationListener() {
                override fun onReceiveLocation(location: BDLocation?) {
                    locationClient.stop()
                    if (location != null && location.locType != BDLocation.TypeServerError) {
                        callback(LatLng(location.latitude, location.longitude))
                    } else {
                        // 定位失败，返回null，让导航应用自己定位
                        callback(null)
                    }
                }
            })

            locationClient.start()
        } catch (e: Exception) {
            Timber.e(e, "获取当前位置失败")
            // 定位失败，返回null
            callback(null)
        }
    }

    /**
     * 使用百度地图导航
     */
    private fun navigateWithBaidu(
        context: Context,
        originLatLng: LatLng?,
        destinationLatLng: LatLng,
        destinationName: String
    ) {
        try {
            val uri = if (originLatLng != null) {
                // 有起点坐标
                "baidumap://map/direction?" +
                        "origin=latlng:${originLatLng.latitude},${originLatLng.longitude}|name:我的位置" +
                        "&destination=latlng:${destinationLatLng.latitude},${destinationLatLng.longitude}|name:$destinationName" +
                        "&mode=driving" +
                        "&coord_type=gcj02" +
                        "&src=${context.packageName}"
            } else {
                // 无起点坐标，使用当前位置
                "baidumap://map/direction?" +
                        "destination=latlng:${destinationLatLng.latitude},${destinationLatLng.longitude}|name:$destinationName" +
                        "&mode=driving" +
                        "&coord_type=gcj02" +
                        "&src=${context.packageName}"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "打开百度地图失败")
            Toaster.show("打开百度地图失败")
        }
    }

    /**
     * 使用高德地图导航
     */
    private fun navigateWithGaode(
        context: Context,
        originLatLng: LatLng?,
        destinationLatLng: LatLng,
        destinationName: String
    ) {
        try {
            val uri = if (originLatLng != null) {
                // 有起点坐标
                "amapuri://route/plan/?" +
                        "slat=${originLatLng.latitude}" +
                        "&slon=${originLatLng.longitude}" +
                        "&sname=我的位置" +
                        "&dlat=${destinationLatLng.latitude}" +
                        "&dlon=${destinationLatLng.longitude}" +
                        "&dname=$destinationName" +
                        "&dev=0" +
                        "&t=0"
            } else {
                // 无起点坐标，使用当前位置
                "amapuri://route/plan/?" +
                        "dlat=${destinationLatLng.latitude}" +
                        "&dlon=${destinationLatLng.longitude}" +
                        "&dname=$destinationName" +
                        "&dev=0" +
                        "&t=0"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "打开高德地图失败")
            Toaster.show("打开高德地图失败")
        }
    }

    /**
     * 使用腾讯地图导航
     */
    private fun navigateWithTencent(
        context: Context,
        originLatLng: LatLng?,
        destinationLatLng: LatLng,
        destinationName: String
    ) {
        try {
            val uri = if (originLatLng != null) {
                // 有起点坐标
                "qqmap://map/routeplan?" +
                        "type=drive" +
                        "&from=我的位置" +
                        "&fromcoord=${originLatLng.latitude},${originLatLng.longitude}" +
                        "&to=$destinationName" +
                        "&tocoord=${destinationLatLng.latitude},${destinationLatLng.longitude}" +
                        "&coord_type=2" +
                        "&referer=${context.packageName}"
            } else {
                // 无起点坐标，使用当前位置
                "qqmap://map/routeplan?" +
                        "type=drive" +
                        "&to=$destinationName" +
                        "&tocoord=${destinationLatLng.latitude},${destinationLatLng.longitude}" +
                        "&coord_type=2" +
                        "&referer=${context.packageName}"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "打开腾讯地图失败")
            Toaster.show("打开腾讯地图失败")
        }
    }

    /**
     * 使用谷歌地图导航
     */
    private fun navigateWithGoogle(
        context: Context,
        originLatLng: LatLng?,
        destinationLatLng: LatLng,
        destinationName: String
    ) {
        try {
            // 将GCJ02坐标转换为WGS84坐标（谷歌地图使用WGS84）
            val wgs84Destination = JZLocationConverter.gcj02ToWgs84(
                CustomLatLng(destinationLatLng.latitude, destinationLatLng.longitude)
            )

            val uri = if (originLatLng != null) {
                val wgs84Origin = JZLocationConverter.gcj02ToWgs84(
                    CustomLatLng(originLatLng.latitude, originLatLng.longitude)
                )
                "google.navigation:q=${wgs84Destination.latitude},${wgs84Destination.longitude}" +
                        "&origin=${wgs84Origin.latitude},${wgs84Origin.longitude}"
            } else {
                "google.navigation:q=${wgs84Destination.latitude},${wgs84Destination.longitude}"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.setPackage(GOOGLE_MAP_PACKAGE)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "打开谷歌地图失败")
            Toaster.show("打开谷歌地图失败")
        }
    }

    /**
     * 使用隐式Intent导航（备用方案）
     * 当无法获取已安装的地图应用时使用
     */
    private fun navigateWithImplicitIntent(
        context: Context,
        destinationLatLng: LatLng,
        destinationName: String
    ) {
        try {
            // 使用geo URI格式，大多数地图应用都支持
            val geoUri =
                "geo:${destinationLatLng.latitude},${destinationLatLng.longitude}?q=${destinationLatLng.latitude},${destinationLatLng.longitude}($destinationName)"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // 检查是否有应用可以处理这个Intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // 如果没有地图应用，尝试在浏览器中打开
                openInBrowser(context, destinationLatLng, destinationName)
            }
        } catch (e: Exception) {
            Timber.e(e, "使用隐式Intent导航失败")
            Toaster.show("打开地图失败")
        }
    }

    /**
     * 在浏览器中打开地图（最后的备用方案）
     */
    private fun openInBrowser(
        context: Context,
        destinationLatLng: LatLng,
        destinationName: String
    ) {
        try {
            // 使用百度地图网页版
            val webUrl =
                "https://map.baidu.com/mobile/webapp/place/detail/qt=s&c=1&searchFlag=bigBox&wd=${destinationName}&center=${destinationLatLng.longitude},${destinationLatLng.latitude}&radius=1000"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "在浏览器中打开地图失败")
            Toaster.show("未找到可用的地图应用或浏览器")
        }
    }
} 