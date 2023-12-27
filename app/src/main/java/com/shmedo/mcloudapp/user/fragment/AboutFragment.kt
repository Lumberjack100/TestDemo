package com.shmedo.mcloudapp.user.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.AppUtils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.activity.WebviewActivity
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentAboutBinding
import com.shmedo.mcloudapp.user.viewmodel.state.AboutViewModel

class AboutFragment : BaseFragment() {
    private val binding: FragmentAboutBinding by lazy { getBinding() as FragmentAboutBinding }
    private val mStates: AboutViewModel by viewModels()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_about, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
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
        mStates.appVersion.set(String.format("v%s", AppUtils.getAppVersionName()))
    }

    inner class ClickProxy {
        /**
         * 用户协议
         */
        fun onViewUserProtocolClick() {
            val url = "file:///android_asset/private/UserProtocol.html"
            WebviewActivity.startActivity(mActivity, "用户协议与免责条款", url)
        }

        /**
         * 应用日志
         */
        fun onViewLogClick() {
            nav().navigate(R.id.action_aboutFragment_to_logSessionListFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}