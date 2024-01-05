package com.shmedo.mcloudapp.user.fragment

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.CleanUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.activity.WebviewActivity
import com.shmedo.lib.core.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.databinding.FragmentSettingBinding
import com.shmedo.mcloudapp.user.viewmodel.state.SettingViewModel

class SettingFragment : BaseFragment() {
    private val binding: FragmentSettingBinding by lazy { getBinding() as FragmentSettingBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: SettingViewModel by viewModels()
    private val loginViewModel: LogViewModel by viewModels()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_setting, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "设置"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
    }

    override fun initData() {
    }

    override fun createObserver() {

    }

    inner class ClickProxy {
        /**
         * 清除缓存和历史日志
         */
        fun onClearLogClick() {
            launchAndRepeatWithViewLifecycle {
                //清除缓存
                CleanUtils.cleanInternalCache()
                CleanUtils.cleanExternalCache()
                //清除历史日志
                loginViewModel.clearHistoryLog()
                Toaster.show("清除缓存成功")
            }
        }

        /**
         * 应用日志查看
         */
        fun onViewLogClick() {
            nav().navigate(R.id.action_global_to_logSessionListFragment)
        }

        /**
         * 服务协议
         */
        fun onUserProtocolClick() {
            val url = "file:///android_asset/private/UserProtocol.html"
            WebviewActivity.startActivity(mActivity, "用户协议与免责条款", url)
        }

        /**
         * 隐私政策
         */
        fun onPrivacyPolicyClick() {

        }

        /**
         * 帮助与反馈
         */
        fun onHelpAndFeedbackClick() {
//            nav().navigate(R.id.action_mainFragment_to_helpAndFeedbackFragment)
        }

        /**
         * 关于
         */
        fun onAboutUsClick() {
            nav().navigate(R.id.action_settingFragment_to_aboutFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}