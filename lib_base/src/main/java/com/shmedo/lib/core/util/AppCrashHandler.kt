package com.shmedo.lib.core.util

import android.os.Looper
import android.os.Process
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import timber.log.Timber

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.logging
 * 创建者:   gonghe
 * 创建时间:  2019-09-03
 *
 */
class AppCrashHandler private constructor() : Thread.UncaughtExceptionHandler {
    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 当程序中有未被捕获的异常，系统将会自动调用uncaughtException方法
     *
     * @param t  出现未捕获异常的线程
     * @param ex 未捕获的异常，有了这个ex，我们就可以得到异常信息
     */
    override fun uncaughtException(t: Thread, ex: Throwable) {
        Timber.e(ex, "米易通异常退出：%s", ex.message)
        killProcess()
    }

    /**
     * 退出应用
     */
    fun killProcess() {
        //结束应用
        Thread {
            Looper.prepare()
            ToastUtils.showShort("哎呀，程序发生异常啦...")
            Looper.loop()
        }.start()

        try {
            Thread.sleep(2000)
        } catch (ex: InterruptedException) {
            Timber.e("CrashHandler.InterruptedException--->%s", ex.toString())
        }
        ActivityUtils.finishAllActivities()
        //退出程序
        Process.killProcess(Process.myPid())
    }

    companion object {
        val INSTANCE: AppCrashHandler by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AppCrashHandler()
        }
    }
}
