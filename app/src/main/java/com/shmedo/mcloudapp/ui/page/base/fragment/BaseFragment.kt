package com.shmedo.mcloudapp.ui.page.base.fragment

import android.os.Bundle
import com.blankj.utilcode.util.KeyboardUtils


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

}