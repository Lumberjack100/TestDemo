package com.shmedo.mcloudapp.ui.page.userprofile

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.setFragmentResultListener
import com.blankj.utilcode.util.AppUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.commonlib.utils.LogoutUtil
import com.shmedo.core.model.UserInfo
import com.shmedo.core.model.UserWrapperInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentMineBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.page.login.LoginActivity
import com.shmedo.mcloudapp.ui.viewmodel.request.AppUpdateViewModel
import com.shmedo.mcloudapp.ui.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MineViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MineFragment : BaseFragment() {
    private lateinit var binding: FragmentMineBinding
    private lateinit var mStates: MineViewModel
    private lateinit var appUpdateViewModel: AppUpdateViewModel
    private lateinit var loginRequestViewModel: LoginRequestViewModel
    private val userInfo: UserInfo by lazy { AuthMMKVOwner.userInfo!! }


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
        appUpdateViewModel = getViewModel()
        loginRequestViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mine, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMineBinding
    }

    override fun initData() {
        mStates.appVersion.set(String.format("V %s", AppUtils.getAppVersionName()))
        refreshUserInfo(userInfo)
    }

    private fun refreshUserInfo(info: UserInfo) {
        if (!TextUtils.isEmpty(info.headPhotoPath))
            mStates.imageUrl.set(info.headPhotoPath)
        mStates.name.set(info.name)
        mStates.title.set(info.position)
        mStates.company.set(info.companyName)
    }

    override fun createObserver() {
        loginRequestViewModel.userWrapperInfoResult.observe(viewLifecycleOwner) { dataResult: DataResult<UserWrapperInfo> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            dataResult.result?.user?.let { refreshUserInfo(it) }
        }
        //从编辑页面返回需要刷新事件详情页面
        setFragmentResultListener(requestKey) { key, bundle ->
            val refresh =
                bundle.getBoolean(com.shmedo.core.commonlib.utils.AppContants.Extras.IS_REFRESH_USER_INFO)
            if (refresh)
                loginRequestViewModel.refreshUserInfo(userInfo.companyID, userInfo.userID)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.statusBarView, isTitleBar = false, isStatusBarDarkFont = true)
    }

    inner class ClickProxy {
        /**
         * 编辑用户信息
         */
        fun onUpdateUserClick() {
            nav().navigate(R.id.action_mainFragment_to_userInfoHomeFragment)
        }

        /**
         *  所属单位
         */
        fun onCompanyClick() {
            nav().navigate(R.id.action_mainFragment_to_companyHomeFragment)
        }

        /**
         * 修改密码
         */
        fun onModifyPasswordClick() {
            nav().navigate(R.id.action_mainFragment_to_resetPasswordFragment)
        }

        /**
         * 版本检查
         */
        fun onVersionCheckClick() {
            appUpdateViewModel.requestCheckAppVersion(true)
        }

        fun onSettingClick() {
            nav().navigate(R.id.action_mainFragment_to_settingFragment)
        }

        fun logout() {
            showMessage("确定退出登录吗", "温馨提示", "退出", {
                LogoutUtil.logout()
                LoginActivity.start(mActivity)
            }, "取消")
        }
    }

    companion object {
        const val requestKey = "MineFragment"
    }
}