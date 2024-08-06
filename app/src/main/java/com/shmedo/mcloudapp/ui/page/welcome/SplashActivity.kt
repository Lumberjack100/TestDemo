package com.shmedo.mcloudapp.ui.page.welcome

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.amap.api.location.AMapLocationClient
import com.blankj.utilcode.util.NetworkUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.MmkvCacheUtil.getAccount
import com.shmedo.core.commonlib.utils.MmkvCacheUtil.getToken
import com.shmedo.core.commonlib.utils.MmkvCacheUtil.getUserRealName
import com.shmedo.core.commonlib.utils.MmkvCacheUtil.isAgreePrivate
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.page.base.activity.BaseActivity
import com.shmedo.mcloudapp.ui.page.main.MainActivity
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.ui.page.login.LoginActivity
import com.shmedo.mcloudapp.ui.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.LogViewModel
import com.shmedo.mcloudapp.ui.dialog.PolicyDialog
import com.tencent.bugly.crashreport.CrashReport
import org.koin.androidx.viewmodel.ext.android.getViewModel

class SplashActivity : BaseActivity() {
    private lateinit var mMessenger: PageMessenger
    private lateinit var mStates: EmptyViewModel
    private lateinit var loginRequestViewModel: LoginRequestViewModel
    private lateinit var logViewModel: LogViewModel

    override fun initViewModel() {
        mMessenger = getAppViewModel()
        mStates = getActivityScopeViewModel()
        loginRequestViewModel = getViewModel()
        logViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_splash, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        initImmersionBar()
    }

    private fun initImmersionBar() {
        immersionBar {
            transparentBar()
        }
    }

    override fun initData() {
        //更新隐私合规状态,需要在初始化地图之前完成
        AMapLocationClient.updatePrivacyShow(this, true, true)
        //更新同意隐私状态,需要在初始化地图之前完成
        AMapLocationClient.updatePrivacyAgree(this, true)
        logViewModel.insertSystemLogSession()
        if (!isAgreePrivate()) {
            showPrivateDialog()
        } else {
            goToLogin()
        }
    }

    override fun createObserver() {
        mMessenger.isAgreePolicy.observe(this) { aBoolean: Boolean ->
            if (aBoolean) {
                goToLogin()
            } else {
                finish()
            }
        }
        loginRequestViewModel.loginResult.observe(this) { dataResult: DataResult<String> ->
            if (!dataResult.responseStatus.isSuccess) {
                dismissLoadingDialog()
                Toaster.show("登录失败: " + dataResult.responseStatus.errorMessage)
                redirectToLoginActivity(500)
                return@observe
            }
            CrashReport.setUserId("${getAccount()}/${getUserRealName()}") //该用户本次启动后的异常日志用户account
            redirectToMainActivity(500)
        }
    }

    private fun goToLogin() {
        if (!NetworkUtils.isConnected()) {
            redirectToMainActivity(500)
            return
        }

        //自动登录
        if (!TextUtils.isEmpty(getToken())) {
            loginRequestViewModel.requestLoginByToken()
        } else {
            redirectToLoginActivity(500)
        }
    }

    private fun showPrivateDialog() {
        //更新隐私合规状态,需要在初始化地图之前完成
        AMapLocationClient.updatePrivacyShow(this, true, true)
        val privacyTipDialog = PolicyDialog()
        privacyTipDialog.show(supportFragmentManager, "dialog")
    }

    /**
     * 跳转到登录界面
     */
    private fun redirectToLoginActivity(delayMillis: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            LoginActivity.start(this@SplashActivity)
            finish()
        }, delayMillis)
    }

    /**
     * 跳转到主界面
     */
    private fun redirectToMainActivity(delayMillis: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            MainActivity.start(this@SplashActivity)
            finish()
        }, delayMillis)
    }
}