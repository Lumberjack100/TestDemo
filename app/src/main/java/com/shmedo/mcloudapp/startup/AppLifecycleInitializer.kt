package com.shmedo.mcloudapp.startup

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.Initializer
import com.shmedo.core.commonlib.utils.AppLifeObserver

/**
 * 创建者：gonghe
 * 创建时间：2025/9/17
 * 描述： TODO
 */
class AppLifecycleInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        ProcessLifecycleOwner.Companion.get().lifecycle.addObserver(AppLifeObserver)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}