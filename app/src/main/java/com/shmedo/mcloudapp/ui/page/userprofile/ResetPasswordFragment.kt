package com.shmedo.mcloudapp.ui.page.userprofile

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.viewModels
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.model.UserInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentResetPasswordBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ResetPasswordViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel

class ResetPasswordFragment : BaseFragment() {
    private lateinit var binding: FragmentResetPasswordBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private lateinit var mStates: ResetPasswordViewModel
    private lateinit var mAccountRequester: LoginRequestViewModel
    private val userInfo: UserInfo by lazy { AuthMMKVOwner.userInfo!! }


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
        mAccountRequester = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_reset_password, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentResetPasswordBinding
        binding.llToolbar.toolbar.title = "修改密码"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
    }

    override fun initData() {
    }

    override fun createObserver() {
        mAccountRequester.updatePasswordResult.observe(viewLifecycleOwner) { dataResult: DataResult<String> ->
            dismissLoadingDialog()
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("修改成功")
            nav().navigateUp()
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onConfirmClick() {
            if (TextUtils.isEmpty(mStates.newPassword.get())) {
                Toaster.show("请输入新密码")
                return
            }
            if (TextUtils.isEmpty(mStates.confirmPassword.get())) {
                Toaster.show("请输入确认密码")
                return
            }
            if (mStates.newPassword.get() != mStates.confirmPassword.get()) {
                Toaster.show("两次密码输入不一致")
                return
            }
            mAccountRequester.requestUpdatePassword(
                userInfo.companyID,
                userInfo.userID,
                mStates.newPassword.get(),
                mStates.confirmPassword.get()
            )
            showLoadingDialog("处理中...")
        }
    }

}