package com.shmedo.mcloudapp.ui.page.userprofile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.AppUtils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAboutBinding
import com.shmedo.mcloudapp.extensions.InsetsManager
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.page.webview.WebViewFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.AboutViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel

class AboutFragment : BaseFragment() {
    private lateinit var binding: FragmentAboutBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: AboutViewModel by viewModels()


    override fun initViewModel() {
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_about, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAboutBinding
        binding.llToolbar.toolbar.title = "关于"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }

        InsetsManager.liftSpecificBottomView(
            binding.root,
            binding.tvCaseNumber,
            applyTo = InsetsManager.ApplyTo.Margin,
        )
    }

    override fun initData() {
        mStates.appVersion.set(String.format("米易通 V%s", AppUtils.getAppVersionName()))
        mStates.caseNumber.set("ICP 备案号：沪ICP备12047248号-4A")
    }


    inner class ClickProxy : BaseClickProxy() {
        /**
         * 用户协议
         */
        fun onUserProtocolClick() {
            val url = "https://appassets.androidplatform.net/assets/private/user_protocol.html"

            val bundle = WebViewFragment.newBundleArguments(
                "米易通",
                url
            )
            nav().safeNavigate(
                R.id.action_global_to_webViewFragment,
                bundle
            )
        }

        /**
         * 隐私协议
         */
        fun onPrivacyProtocolClick() {
            val url = "https://appassets.androidplatform.net/assets/private/privacy.html"

            val bundle = WebViewFragment.newBundleArguments(
                "米易通",
                url
            )
            nav().safeNavigate(
                R.id.action_global_to_webViewFragment,
                bundle
            )
        }
    }
}