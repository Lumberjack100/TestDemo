package com.shmedo.mcloudapp.common.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.blankj.utilcode.util.KeyboardUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.shmedo.lib.core.base.activity.BaseVmDbActivity
import com.shmedo.mcloudapp.R

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/11/28 <br></br>
 * 描述：     TODO
 */
abstract class BaseActivity : BaseVmDbActivity() {

    override fun initViewModel() {}

    override fun initView(savedInstanceState: Bundle?) {}

    override fun initData() {}

    /**
     * 创建liveData观察者
     */
    override fun createObserver() {}

    /**
     * Use a Toolbar as an Action Bar
     */
    protected open fun setToolBar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    open fun hideTitleBar() {
        supportActionBar?.hide()
    }

    open fun initImmersionBar(
        bar: View,
        isDarkFont: Boolean = true,
        isDarkIcon: Boolean = true,
        navigationBarColor: Int = R.color.white
    ) {
        immersionBar {
            titleBar(bar)
            statusBarDarkFont(isDarkFont)
            navigationBarDarkIcon(isDarkIcon)
            navigationBarColor(navigationBarColor)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //ActionBar Home按钮返回事件
        if (item.itemId == R.id.home) {
            KeyboardUtils.hideSoftInput(this.window.decorView)
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}