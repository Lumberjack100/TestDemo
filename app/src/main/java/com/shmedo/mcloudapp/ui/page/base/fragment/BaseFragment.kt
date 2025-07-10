package com.shmedo.mcloudapp.ui.page.base.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.shmedo.mcloudapp.R


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/11/29 <br/>
 * 描述：     TODO
 */
abstract class BaseFragment : BaseVmDbFragment() {

    override fun initViewModel() {}

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
        isTitleBar: Boolean = true,
        isStatusBarDarkFont: Boolean = true,
        isNavigationBarDarkIcon: Boolean = true,
        statusBarColor: Int = -1,
        navigationBarColor: Int = R.color.white,
        isKeyboardEnable: Boolean = false
    ) {
        if (isHidden) return
        immersionBar {
            if (isTitleBar)
                titleBar(statusBar)
            else
                statusBarView(statusBar)

            statusBarDarkFont(isStatusBarDarkFont)
            navigationBarDarkIcon(isNavigationBarDarkIcon)
            if (statusBarColor != -1)
                statusBarColor(statusBarColor)
            if (navigationBarColor != -1)
                navigationBarColor(navigationBarColor)
            keyboardEnable(isKeyboardEnable)
        }
    }
}