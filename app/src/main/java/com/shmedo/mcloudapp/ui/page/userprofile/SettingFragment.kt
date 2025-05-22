package com.shmedo.mcloudapp.ui.page.userprofile

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.CleanUtils
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentSettingBinding
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.page.webview.WebViewFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.LogViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.ui.viewmodel.state.SettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingFragment : BaseFragment() {
    private lateinit var binding: FragmentSettingBinding
    private lateinit var mMessenger: PageMessenger
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: SettingViewModel by viewModels()
    private val logViewModel: LogViewModel by viewModel()


    override fun initViewModel() {
        mMessenger = getAppViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_setting, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentSettingBinding
        toolbarViewModel.toolbarTitleText.set("设置")
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

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        mStates.isAllowNotification.set(
            XXPermissions.isGranted(
                requireContext(),
                Permission.POST_NOTIFICATIONS
            )
        )
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 消息通知
         */
        fun onMessageNotificationClick() {
            XXPermissions.startPermissionActivity(
                this@SettingFragment,
                Permission.POST_NOTIFICATIONS
            )
        }

        /**
         * 权限设置
         */
        fun onPermissionSettingClick() {

        }

        /**
         * 个人信息收集清单
         */
        fun onPersonalInformationCollectionListClick() {

        }

        /**
         * 第三方信息共享清单
         */
        fun onThirdPartyInformationSharingListClick() {
//            val url = "file:///android_asset/private/third_party_sharing_list.html"
            val url = "https://appassets.androidplatform.net/assets/private/third_party_sharing_list.html"
            val bundle = WebViewFragment.newBundleArguments(
                "第三方信息共享清单",
                url
            )
            nav().safeNavigate(
                R.id.action_aboutFragment_to_webViewFragment,
                bundle
            )
        }

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
         * 关于
         */
        fun onAboutUsClick() {
            nav().safeNavigate(R.id.action_settingFragment_to_aboutFragment)
        }

        /**
         * 应用日志查看
         */
        fun onLogViewClick() {
            nav().safeNavigate(R.id.action_global_to_logSessionListFragment)
        }

    }

}