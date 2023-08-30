package com.shmedo.mcloudapp.common.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.activity.viewModels
import com.amap.api.maps.MapsInitializer
import com.gyf.immersionbar.ktx.immersionBar
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.MmkvCacheUtil.getPassword
import com.shmedo.lib.core.util.MmkvCacheUtil.getUserName
import com.shmedo.lib.core.util.MmkvCacheUtil.isAgreePrivate
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.login.activity.LoginActivity
import com.shmedo.mcloudapp.login.fragment.PolicyDialog
import com.shmedo.mcloudapp.login.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.login.viewmodel.state.SplashViewModel
import com.tencent.bugly.crashreport.CrashReport

class SplashActivity : BaseActivity() {
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: SplashViewModel by viewModels()
    private val loginRequestViewModel: LoginRequestViewModel by viewModels()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_splash, BR.vm, mStates)
    }

    private fun initImmersionBar() {
        immersionBar {
            transparentBar()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initImmersionBar()
    }

    override fun initData() {
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
                dismissLoading()
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