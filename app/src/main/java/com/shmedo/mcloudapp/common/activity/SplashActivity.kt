package com.shmedo.mcloudapp.common.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.amap.api.maps.MapsInitializer
import com.gyf.immersionbar.ktx.immersionBar
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.LogLevel
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.ext.getActivityScopeViewModel
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.ext.getLogItem
import com.shmedo.lib.core.ext.getSystemLogSession
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.core.util.MmkvCacheUtil.getPassword
import com.shmedo.lib.core.util.MmkvCacheUtil.getUserName
import com.shmedo.lib.core.util.MmkvCacheUtil.isAgreePrivate
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.ext.dismissLoadingDialog
import com.shmedo.mcloudapp.user.activity.LoginActivity
import com.shmedo.mcloudapp.user.fragment.PolicyDialog
import com.shmedo.mcloudapp.user.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.utils.LogHelper
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
        MapsInitializer.updatePrivacyShow(this, true, true)
        //更新同意隐私状态,需要在初始化地图之前完成
        MapsInitializer.updatePrivacyAgree(this, true)
        logViewModel.insertSession(getSystemLogSession())
        logViewModel.insertLog(
            getLogItem(
                sessionId = MmkvCacheUtil.getAppLogSessionId(),
                priority = LogLevel.INFO,
                data = LogHelper.printDeviceInfo()
            )
        )
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
                redirectToLoginActivity(500)
                return@observe
            }
            CrashReport.setUserId(getUserName()) //该用户本次启动后的异常日志用户account
            redirectToMainActivity(500)
        }
    }

    private fun goToLogin() {
        val mAccount = getUserName()
        val mPassword = getPassword()
        //自动登录
        if (!TextUtils.isEmpty(mAccount) && !TextUtils.isEmpty(mPassword)) {
            loginRequestViewModel.requestLogin(mAccount, mPassword)
        } else {
            redirectToLoginActivity(500)
        }
    }

    private fun showPrivateDialog() {
        //更新隐私合规状态,需要在初始化地图之前完成
        MapsInitializer.updatePrivacyShow(this, true, true)
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