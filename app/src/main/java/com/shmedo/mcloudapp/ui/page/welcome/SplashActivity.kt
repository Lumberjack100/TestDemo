package com.shmedo.mcloudapp.ui.page.welcome

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.activity.enableEdgeToEdge
import com.baidu.location.LocationClient
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.common.BaiduMapSDKException
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.NetworkUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.ui.dialog.PolicyDialog
import com.shmedo.mcloudapp.ui.page.base.activity.BaseActivity
import com.shmedo.mcloudapp.ui.page.login.LoginActivity
import com.shmedo.mcloudapp.ui.page.main.MainActivity
import com.shmedo.mcloudapp.ui.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.LogViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
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
        enableEdgeToEdge()
    }


    override fun initData() {
        logViewModel.insertSystemLogSession()
        if (!CommonMMKVOwner.isAgreePrivate) {
            showPrivateDialog()
        } else {
            initThirdSDK()
            goToLogin()
        }
    }

    /**
     * 初始化异常上报
     */
    private fun initThirdSDK() {
        initBugly()
        initBaiduMapSDK()
    }

    private fun initBugly() {
        //初始化腾讯Bugly异常上报组件
        CrashReport.initCrashReport(applicationContext)
        //腾讯Bugly 设置设备id
        CrashReport.setDeviceId(applicationContext, DeviceUtils.getUniqueDeviceId())
        //腾讯Bugly 设置手机型号
        CrashReport.setDeviceModel(applicationContext, DeviceUtils.getModel())
    }

    private fun initBaiduMapSDK() {
        try {
            SDKInitializer.setAgreePrivacy(applicationContext, true)
            LocationClient.setAgreePrivacy(true)
            //在使用SDK各组件之前初始化context信息，传入ApplicationContext
            SDKInitializer.initialize(applicationContext)
            SDKInitializer.setCoordType(CoordType.GCJ02);
        } catch (e: BaiduMapSDKException) {
        }
    }

    override fun createObserver() {
        mMessenger.isAgreePolicy.observe(this) { aBoolean: Boolean ->
            if (aBoolean) {
                initThirdSDK()
                goToLogin()
            } else {
                finish()
            }
        }
        loginRequestViewModel.loginResult.observe(this) { dataResult: DataResult<String> ->
            if (!dataResult.responseStatus.isSuccess) {
                dismissLoadingDialog()
                Toaster.show("登录失败: " + dataResult.responseStatus.errorMessage)
                redirectToLoginActivity(0)
                return@observe
            }
            CrashReport.setUserId("${AuthMMKVOwner.account}/${AuthMMKVOwner.realName}") //该用户本次启动后的异常日志用户account
            redirectToMainActivity(0)
        }
    }

    private fun goToLogin() {
        if (!NetworkUtils.isConnected()) {
            redirectToMainActivity(0)
            return
        }

        //自动登录
        if (!TextUtils.isEmpty(AuthMMKVOwner.token)) {
            loginRequestViewModel.requestLoginByToken()
        } else {
            redirectToLoginActivity(0)
        }
    }

    private fun showPrivateDialog() {
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