package com.shmedo.mcloudapp.common.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.activity.viewModels
import com.amap.api.maps.MapsInitializer
import com.blankj.utilcode.util.NetworkUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.util.MmkvCacheUtil.isAgreePrivate
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.login.activity.LoginActivity
import com.shmedo.mcloudapp.login.fragment.PolicyDialog
import com.shmedo.mcloudapp.login.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.login.viewmodel.state.SplashViewModel
import org.json.JSONException
import org.json.JSONObject

class SplashActivity : BaseActivity() {
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
        loginRequestViewModel.loginResult.observe(this) { dataResult: DataResult<String> ->
            if (!dataResult.responseStatus.isSuccess) {
                dismissLoading()
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
//            CrashReport.setUserId(mStates.name.get()) //该用户本次启动后的异常日志用户account
//            setToken(dataResult.result!!.token)
//            setUserName(mStates.name.get())
//            setPassword(mStates.password.get())
        }
    }

    private fun goToLogin() {
        val mAccount = "medo_gh"//getUserName()
        val mPassword ="medo123456"// getPassword()
        //自动登录
        if (!TextUtils.isEmpty(mAccount) && !TextUtils.isEmpty(mPassword)) {
            if (!NetworkUtils.isConnected()) {
                redirectToMainActivity(300)
            } else {
                val jsonObjectRequest = JSONObject()//接口请求参数
                try {
                    jsonObjectRequest.put("account", mAccount)
                    jsonObjectRequest.put("password", mPassword)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                loginRequestViewModel.requestLogin(jsonObjectRequest.toString())
            }
        } else {
            redirectToLoginActivity(0)
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