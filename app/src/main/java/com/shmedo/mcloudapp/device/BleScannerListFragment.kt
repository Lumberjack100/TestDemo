package com.shmedo.mcloudapp.device

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.databinding.FragmentBleScannerListBinding
import com.shmedo.mcloudapp.device.viewmodel.state.BleScannerListViewModel

class BleScannerListFragment : BaseFragment() {
    private val binding: FragmentBleScannerListBinding by lazy { getBinding() as FragmentBleScannerListBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: BleScannerListViewModel by viewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ble_scanner_list, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData() {

    }

    override fun createObserver() {

    }

    inner class ClickProxy {

    }

    companion object {
        fun newInstance() = BleScannerListFragment()
    }
}