package com.shmedo.mcloudapp.common.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.shmedo.lib.core.base.fragment.BaseVmDbFragment
import com.shmedo.mcloudapp.R


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/11/29 <br/>
 * 描述：     TODO
 */
abstract class BaseFragment : BaseVmDbFragment() {

    abstract override fun initView(savedInstanceState: Bundle?)

    override fun initData() {

    }

    override fun createObserver() {

    }

    override fun onPause() {
        super.onPause()
        activity?.window?.let { KeyboardUtils.hideSoftInput(it.decorView) }
    }

    open fun initImmersionBar(
        statusBar: View,
        isStatusBarDarkFont: Boolean = true,
        isNavigationBarDarkIcon: Boolean = true,
        navigationBarColor: Int = R.color.white,
        statusBarColor: Int = -1,
        isKeyboardEnable: Boolean = true,

        ) {
        immersionBar {
            titleBar(statusBar)
            statusBarDarkFont(isStatusBarDarkFont)
            navigationBarDarkIcon(isNavigationBarDarkIcon)
            navigationBarColor(navigationBarColor)
            if (statusBarColor != -1)
                statusBarColor(statusBarColor)
            keyboardEnable(isKeyboardEnable)
        }
    }
}