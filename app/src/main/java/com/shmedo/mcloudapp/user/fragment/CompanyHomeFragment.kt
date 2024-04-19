package com.shmedo.mcloudapp.user.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.CompanyInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.dismissLoadingDialog
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentCompanyHomeBinding
import com.shmedo.mcloudapp.user.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.user.viewmodel.state.CompanyHomeViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel

class CompanyHomeFragment : BaseFragment() {
    private lateinit var binding: FragmentCompanyHomeBinding
    private lateinit var mStates: CompanyHomeViewModel
    private lateinit var loginRequestViewModel: LoginRequestViewModel
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
        loginRequestViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_company_home, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentCompanyHomeBinding
        binding.llToolbar.toolbar.title = "企业详情"
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
        binding.llToolbar.tvAction.visibility = View.VISIBLE
        binding.llToolbar.tvAction.text = "切换企业"
        binding.llToolbar.tvAction.setOnClickListener {

        }
    }

    override fun initData() {

    }

    override fun createObserver() {
        loginRequestViewModel.companyInfoResult.observe(viewLifecycleOwner) { dataResult: DataResult<CompanyInfo> ->
            dismissLoadingDialog()
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            dataResult.result?.let { updateView(it) }
        }
    }

    override fun lazyLoadData() {
        showLoadingDialog("加载中...")
        loginRequestViewModel.queryCompanyInfoByID(userInfo.companyID)
    }

    private fun updateView(info: CompanyInfo) {
        mStates.companyName.set(info.fullName)
        mStates.companyType.set(info.nature)
        mStates.industryName.set(info.industry)
        mStates.companyPhone.set(info.phone)
        mStates.companyAddress.set(info.address)
        mStates.companyWebsite.set(info.webSite)
        mStates.companyIntro.set(info.desc)
    }

    inner class ClickProxy {

    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}