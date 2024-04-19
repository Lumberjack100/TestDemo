package com.shmedo.mcloudapp.common.utils

import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.RomUtils
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/3/29
 * 描述： TODO
 */
object LogHelper {
    fun printDeviceInfo(): String {
        //获取应用包名
        val appPackage = AppUtils.getAppPackageName()

        // 获取应用版本
        val appVersion = AppUtils.getAppVersionName()

        // 前后台状态
        val isForeground =
            ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        val foregroundStatus = if (isForeground) "前台" else "后台"

        val brand = RomUtils.getRomInfo().name
        // 设备机型
        val model = DeviceUtils.getModel()

        // 系统版本和API级别
        val systemVersion = Build.VERSION.RELEASE
        val apiLevel = Build.VERSION.SDK_INT

        // ROM信息
        val rom = RomUtils.getRomInfo().version

        // CPU架构
        val cpuArch = Build.SUPPORTED_ABIS[0]

        // 拼接字符串
        val deviceInfo = """
        应用包名: $appPackage
        应用版本: $appVersion
        前后台状态: $foregroundStatus
        设备机型: $brand/$model
        系统版本: Android $systemVersion,level $apiLevel
        ROM: $rom
        CPU架构: $cpuArch
    """.trimIndent()

        // 打印到 Logcat
        Timber.tag("DeviceInfo").d(deviceInfo)

        return deviceInfo
    }

}