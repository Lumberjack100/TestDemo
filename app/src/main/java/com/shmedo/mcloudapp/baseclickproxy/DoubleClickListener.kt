package com.shmedo.mcloudapp.baseclickproxy

import android.view.View

/**
 * 创建者：gonghe
 * 创建时间：2024/10/18
 * 描述： TODO
 */
abstract class DoubleClickListener : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
            lastClickTime = 0
        } else {
            lastClickTime = clickTime
        }
    }

    abstract fun onDoubleClick(v: View)

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}