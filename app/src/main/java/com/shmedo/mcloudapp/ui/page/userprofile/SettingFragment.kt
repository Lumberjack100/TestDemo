package com.shmedo.mcloudapp.ui.page.userprofile

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.CleanUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.ui.viewmodel.state.LogViewModel
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.page.webview.WebviewActivity
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.databinding.FragmentSettingBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.ui.viewmodel.state.SettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.getViewModel

class SettingFragment : BaseFragment() {
    private lateinit var binding: FragmentSettingBinding
    private lateinit var mMessenger: PageMessenger
    private lateinit var mStates: SettingViewModel
    private lateinit var logViewModel: LogViewModel


    override fun initViewModel() {
        mMessenger = getAppViewModel()
        mStates = getFragmentScopeViewModel()
        logViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_setting, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentSettingBinding
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
            launchWithViewLifecycle {
                withContext(Dispatchers.IO) {
                    //清除缓存
                    CleanUtils.cleanInternalCache()
                    CleanUtils.cleanExternalCache()
                }
                //清除历史日志
                logViewModel.clearHistoryLog()
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