package com.shmedo.mcloudapp.device.m20.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.databinding.FragmentM20HomeBinding
import com.shmedo.mcloudapp.device.m20.viewmodel.M20HomeViewModel

class NetM20HomeFragment : BaseFragment() {
    private val binding: FragmentM20HomeBinding by lazy { getBinding() as FragmentM20HomeBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: M20HomeViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    private var statusBarColor = 0

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m20_home, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
    }

    override fun initData() {
        arguments?.let {
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
//            binding.llToolbar.toolbar.title = keyWord
        }
    }

    override fun createObserver() {

    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private const val PAGE_SIZE = 20
        fun newBundleArguments(
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }

}