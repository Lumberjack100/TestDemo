package com.shmedo.mcloudapp.user.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.AppUtils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentAboutBinding
import com.shmedo.mcloudapp.user.viewmodel.state.AboutViewModel

class AboutFragment : BaseFragment() {
    private lateinit var binding: FragmentAboutBinding
    private lateinit var mStates: AboutViewModel


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_about, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAboutBinding
        binding.llToolbar.toolbar.title = "关于"
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
        mStates.appVersion.set(String.format("米易通 V%s", AppUtils.getAppVersionName()))
        mStates.caseNumber.set("沪ICP备12047248号-4A")
    }

    inner class ClickProxy {

    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}