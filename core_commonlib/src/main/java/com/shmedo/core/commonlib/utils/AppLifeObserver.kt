package com.shmedo.core.commonlib.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.kunminx.architecture.domain.message.MutableResult

/**
 * 作者　: hegaojian
 * 时间　: 20120/1/7
 * 描述　:
 */
object AppLifeObserver : LifecycleEventObserver {
    var isForeground = MutableResult<Boolean>()


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START) {
            //在前台
            isForeground.value = true

        } else if (event == Lifecycle.Event.ON_STOP) {
            //在后台
            isForeground.value = false
        }
    }

}