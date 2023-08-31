package com.shmedo.mcloudapp.common.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.shmedo.lib.core.base.fragment.BaseVmDbFragment
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.dismissLoadingExt
import com.shmedo.mcloudapp.common.ext.showLoadingExt


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/11/29 <br/>
 * 描述：     TODO
 */
abstract class BaseFragment : BaseVmDbFragment() {

    abstract override fun initView(savedInstanceState: Bundle?)

    override fun initData() {

    }


    /**
     * 打开等待框
     */
    override fun showLoading(message: String) {
        showLoadingExt(message)
//        showWaitDialog(message)
    }

    /**
     * 关闭等待框
     */
    override fun dismissLoading() {
        dismissLoadingExt()
//        dismissWaitDialog()
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.let { KeyboardUtils.hideSoftInput(it.decorView) }
    }

    open fun initImmersionBar(
        bar: View,
        isDarkFont: Boolean = true,
        isDarkIcon: Boolean = true,
        navigationBarColor: Int = R.color.white,
        statusBarColor: Int = -1
    ) {
        immersionBar {
            titleBar(bar)
            statusBarDarkFont(isDarkFont)
            navigationBarDarkIcon(isDarkIcon)
            navigationBarColor(navigationBarColor)
            if (statusBarColor != -1)
                statusBarColor(statusBarColor)
        }
    }
}