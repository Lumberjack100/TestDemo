package com.shmedo.mcloudapp.utils

/**
 * 创建者：gonghe
 * 创建时间：2024/10/21
 * 描述： TODO
 */

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

object CurrentActivityProvider : Application.ActivityLifecycleCallbacks {

    @Volatile
    private var currentActivityRef: WeakReference<FragmentActivity?>? = null

    /**
     * 获取当前前台的 FragmentActivity。
     */
    fun getCurrentActivity(): FragmentActivity? {
        return currentActivityRef?.get()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        setCurrentActivityIfPossible(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        setCurrentActivityIfPossible(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        setCurrentActivityIfPossible(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        // 不需要处理
    }

    override fun onActivityStopped(activity: Activity) {
        // 不需要处理
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // 不需要处理
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivityRef?.get() === activity) {
            currentActivityRef = null
        }
    }

    private fun setCurrentActivityIfPossible(activity: Activity) {
        if (activity is FragmentActivity) {
            currentActivityRef = WeakReference(activity)
        }
    }
}