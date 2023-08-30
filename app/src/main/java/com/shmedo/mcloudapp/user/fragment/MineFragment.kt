package com.shmedo.mcloudapp.user.fragment

import android.os.Bundle
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.base.model.UserWrapperInfo
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.core.util.MmkvCacheUtil.setPassword
import com.shmedo.lib.core.util.MmkvCacheUtil.setUser
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.databinding.FragmentMineBinding
import com.shmedo.mcloudapp.user.activity.LoginActivity
import com.shmedo.mcloudapp.user.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.user.viewmodel.state.MineViewModel

class MineFragment : BaseFragment() {
    private val binding: FragmentMineBinding by lazy { getBinding() as FragmentMineBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val loginRequestViewModel: LoginRequestViewModel by viewModels()
    private val mStates: MineViewModel by viewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mine, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun initData() {
        mStates.appVersion.set(String.format("v%s", AppUtils.getAppVersionName()))
        updateUserInfo(userInfo)
    }
    private fun updateUserInfo(info: UserInfo) {
        mStates.name.set(info.name!!)
        mStates.title.set(info.position!!)
        mStates.company.set(info.companyName!!)
    }

    override fun createObserver() {
        loginRequestViewModel.userWrapperInfoResult.observe(this) { dataResult: DataResult<UserWrapperInfo> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            dataResult.result?.user?.let { updateUserInfo(it) }
        }
        //从编辑页面返回需要刷新事件详情页面
        setFragmentResultListener(requestKey) { key, bundle ->
            val refresh = bundle.getBoolean(AppContants.Extras.IS_REFRESH_USER_INFO)
            if (refresh)
                loginRequestViewModel.refreshUserInfo(userInfo.userID, userInfo.companyID)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.statusBarView, false)
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
            Toaster.show("正在开发中")
        }

        /**
         * 修改密码
         */
        fun onModifyPasswordClick() {
//            nav().navigate(R.id.action_mainFragment_to_ResetPasswordFragment)
        }

        /**
         * 版本检查
         */
        fun onVersionCheckClick() {
            //清除缓存
//            CleanUtils.cleanInternalCache();
//            CleanUtils.cleanExternalCache();
//            shareViewModel.requestCheckAppVersion(true)
        }

        /**
         * 关于我们
         */
        fun onAboutUsClick() {
//            nav().navigate(R.id.action_mainFragment_to_AboutFragment)
        }

        fun logout() {
            showMessage("确定退出登录吗", "温馨提示", "退出", {
                //appViewModel.userInfo.value = null
                setPassword("")
                setUser(null)
                ActivityUtils.finishAllActivities()
                LoginActivity.start(mActivity)
            }, "取消")
        }
    }

    companion object {
        const val requestKey = "MineFragment"
    }
}