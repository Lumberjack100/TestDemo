package com.shmedo.mcloudapp.common.ext

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.shmedo.lib.core.base.activity.BaseVmDbActivity
import com.shmedo.lib.core.base.fragment.BaseVmDbFragment

/**
 * 作者　: hegaojian
 * 时间　: 2020/5/2
 * 描述　:
 */
fun BaseVmDbActivity.nav(resId: Int): NavController {
    return Navigation.findNavController(this, resId)
}

fun BaseVmDbFragment.nav(): NavController {
    return NavHostFragment.findNavController(this)
}

fun nav(view: View): NavController {
    return Navigation.findNavController(view)
}

var lastNavTime = 0L

/**
 * 防止短时间内多次快速跳转Fragment出现的bug
 * @param resId 跳转的action Id
 * @param bundle 传递的参数
 * @param interval 多少毫秒内不可重复点击 默认0.5秒
 */
fun NavController.navigateAction(resId: Int, bundle: Bundle? = null, interval: Long = 500) {
    val currentTime = System.currentTimeMillis()
    if (currentTime >= lastNavTime + interval) {
        lastNavTime = currentTime
        try {
            navigate(resId, bundle)
        } catch (ignore: Exception) {
            //防止出现 当 fragment 中 action 的 duration设置为 0 时，连续点击两个不同的跳转会导致如下崩溃 #issue53
        }
    }
}

fun  BaseVmDbFragment.registerOnBackPressedDispatcher(backPressedHandle: () -> Unit) {
    activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backPressedHandle()
        }
    })
}

