package com.shmedo.mcloudapp.common.activity

import android.R
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.blankj.utilcode.util.KeyboardUtils
import com.shmedo.lib.core.base.activity.BaseVmDbActivity
import com.shmedo.mcloudapp.ext.dismissWaitDialog
import com.shmedo.mcloudapp.ext.showWaitDialog

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/11/28 <br></br>
 * 描述：     TODO
 */
abstract class BaseActivity : BaseVmDbActivity() {

    override fun initViewModel() {}

    abstract override fun initView(savedInstanceState: Bundle?)

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

    /**
     * 打开等待框
     */
    fun showLoading(message: String) {
//        showLoadingExt(message)
        showWaitDialog(message)
    }

    /**
     * 关闭等待框
     */
    fun dismissLoading() {
//        dismissLoadingExt()
        dismissWaitDialog()
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