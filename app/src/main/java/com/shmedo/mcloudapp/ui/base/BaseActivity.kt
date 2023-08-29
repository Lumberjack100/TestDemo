package com.shmedo.mcloudapp.ui.base

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
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

}