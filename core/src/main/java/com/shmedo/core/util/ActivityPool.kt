package com.shmedo.core.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.*

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/11/18 <br></br>
 * 描述：     Activity管理池
 */
class ActivityPool : Application.ActivityLifecycleCallbacks {
    fun init(app: Application) {
        app.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activityStack.add(activity)
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        activityStack.remove(activity)
    }

    companion object {
        private val activityStack = Stack<Activity>()
    }
}