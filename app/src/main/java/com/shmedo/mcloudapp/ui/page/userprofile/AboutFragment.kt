package com.shmedo.mcloudapp.ui.page.userprofile

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.AppUtils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAboutBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
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
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAboutBinding
        toolbarViewModel.toolbarTitleText.set("关于")
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


    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}