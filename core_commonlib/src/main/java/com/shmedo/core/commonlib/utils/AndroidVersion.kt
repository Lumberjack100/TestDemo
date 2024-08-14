package com.shmedo.core.commonlib.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/XXPermissions
 * time   : 2022/01/22
 * desc   : Android 版本判断
 */
@SuppressLint("AnnotateVersionCheck")
object AndroidVersion {
//    val ANDROID_14: Int = Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    const val ANDROID_13 = Build.VERSION_CODES.TIRAMISU
    const val ANDROID_12_L = Build.VERSION_CODES.S_V2
    const val ANDROID_12 = Build.VERSION_CODES.S
    const val ANDROID_11 = Build.VERSION_CODES.R
    const val ANDROID_10 = Build.VERSION_CODES.Q
    const val ANDROID_9 = Build.VERSION_CODES.P
    const val ANDROID_8_1 = Build.VERSION_CODES.O_MR1
    const val ANDROID_8 = Build.VERSION_CODES.O
    const val ANDROID_7_1 = Build.VERSION_CODES.N_MR1
    const val ANDROID_7 = Build.VERSION_CODES.N
    const val ANDROID_6 = Build.VERSION_CODES.M
    const val ANDROID_5_1 = Build.VERSION_CODES.LOLLIPOP_MR1
    const val ANDROID_5 = Build.VERSION_CODES.LOLLIPOP
    const val ANDROID_4_4 = Build.VERSION_CODES.KITKAT
    const val ANDROID_4_3 = Build.VERSION_CODES.JELLY_BEAN_MR2
    const val ANDROID_4_2 = Build.VERSION_CODES.JELLY_BEAN_MR1
    const val ANDROID_4_1 = Build.VERSION_CODES.JELLY_BEAN
    const val ANDROID_4_0 = Build.VERSION_CODES.ICE_CREAM_SANDWICH
    val androidVersionCode: Int
        /**
         * 获取 Android 版本码
         */
        get() = Build.VERSION.SDK_INT

    /**
     * 获取 targetSdk 版本码
     */
    fun getTargetSdkVersionCode(context: Context): Int {
        return context.applicationInfo.targetSdkVersion
    }

    val isAndroid14: Boolean
        /**
         * 是否是 Android 14 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= 34
    val isAndroid13: Boolean
        /**
         * 是否是 Android 13 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_13
    val isAndroid12: Boolean
        /**
         * 是否是 Android 12 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_12
    val isAndroid11: Boolean
        /**
         * 是否是 Android 11 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_11
    val isAndroid10: Boolean
        /**
         * 是否是 Android 10 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_10
    val isAndroid9: Boolean
        /**
         * 是否是 Android 9.0 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_9
    val isAndroid8: Boolean
        /**
         * 是否是 Android 8.0 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_8
    val isAndroid7_1: Boolean
        /**
         * 是否是 Android 7.1 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_7_1
    val isAndroid7: Boolean
        /**
         * 是否是 Android 7.0 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_7
    val isAndroid6: Boolean
        /**
         * 是否是 Android 6.0 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_6
    val isAndroid5_1: Boolean
        /**
         * 是否是 Android 5.1 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_5_1
    val isAndroid5: Boolean
        /**
         * 是否是 Android 5.0 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_5
    val isAndroid4_4: Boolean
        /**
         * 是否是 Android 4.4 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_4_4
    val isAndroid4_3: Boolean
        /**
         * 是否是 Android 4.3 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_4_3
    val isAndroid4_2: Boolean
        /**
         * 是否是 Android 4.2 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_4_2
    val isAndroid4: Boolean
        /**
         * 是否是 Android 4.0 及以上版本
         */
        get() = Build.VERSION.SDK_INT >= ANDROID_4_0
}