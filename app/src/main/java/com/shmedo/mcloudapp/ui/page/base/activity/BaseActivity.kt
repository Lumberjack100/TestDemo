package com.shmedo.mcloudapp.ui.page.base.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.blankj.utilcode.util.KeyboardUtils

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
    open fun setToolBar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    open fun hideTitleBar() {
        supportActionBar?.hide()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //ActionBar Home按钮返回事件
        if (item.itemId == android.R.id.home) {
            KeyboardUtils.hideSoftInput(this.window.decorView)
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}