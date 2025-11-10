package com.shmedo.mcloudapp.ui.page.login

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.model.ContentType
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ActivityLoginBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.ui.page.base.activity.BaseActivity
import com.shmedo.mcloudapp.ui.page.main.MainActivity
import com.shmedo.mcloudapp.ui.page.webview.WebviewActivity
import com.shmedo.mcloudapp.ui.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.LoginViewModel
import com.shmedo.mcloudapp.ui.widget.MyCountDownTimer
import com.tencent.bugly.crashreport.CrashReport
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @author：gonghe
 * @time: 2025/9/15
 * @desc: 登录页面
 *
 */
class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val mStates: LoginViewModel by viewModels()
    private val loginRequestViewModel: LoginRequestViewModel by viewModel()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_login, BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        binding = getBinding() as ActivityLoginBinding
    }

    private fun initLoginUserProtocol() {
        val text = StringUtils.getString(R.string.login_protocol_desc)
        val spannableString = SpannableString(text)
        val start1 = text.indexOf("《用户隐私保护指引》")
        val end1 = start1 + "《用户隐私保护指引》".length
        spannableString.setSpan(
            MyClickText(this, ContentType.PRIVACY_POLICY),
            start1,
            end1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //当然这里也可以通过setSpan来设置哪些位置的文本哪些颜色
        binding.tvUserProtocol.text = spannableString
        binding.tvUserProtocol.movementMethod = LinkMovementMethod.getInstance() //不设置 没有点击事件
        binding.tvUserProtocol.highlightColor = Color.TRANSPARENT //设置点击后的颜色为透明
    }

    override fun initData() {
        mStates.account.set(AuthMMKVOwner.account)
//        mStates.agreeProtocol.set(CommonMMKVOwner.isAgreePrivate)
        initLoginUserProtocol()
    }

    override fun createObserver() {
        loginRequestViewModel.sendCodeResult.observe(this) { dataResult: DataResult<String> ->
            dismissLoadingDialog()
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("验证码已发送")
            val timer = MyCountDownTimer(binding.tvGetCode, 60000, 1000)
            timer.setTextColor(R.color.title_text_color, R.color.text_color_B3B3B3)
            timer.start()
        }
        loginRequestViewModel.loginResult.observe(this) { dataResult: DataResult<String> ->
            dismissLoadingDialog()
            if (!dataResult.responseStatus.isSuccess) {
                //32 用户已限制登录   36 本次登录 IP 与常登录 IP 不符合  37 密码超过 90天过期
                when (dataResult.responseStatus.responseCode) {
                    "32", "36" -> {
                        showMessage(
                            "登录失败: ${dataResult.responseStatus.errorMessage},请用手机号登录",
                            "温馨提示",
                            "确定",
                            {
                                mStates.isAccountLogin.set(false)
                            })
                    }

                    "37" -> {
                        showMessage(
                            "登录失败: 密码超过 90 天已过期,请用手机号登录后修改密码",
                            "温馨提示",
                            "确定",
                            {
                                mStates.isAccountLogin.set(false)
                            })
                    }

                    else -> Toaster.show("登录失败: " + dataResult.responseStatus.errorMessage)
                }
                return@observe
            }
            CrashReport.setUserId(AuthMMKVOwner.account) //该用户本次启动后的异常日志用户account
            redirectToMainActivity(500)
        }
    }

    inner class ClickProxy {
        fun eyeClick() {
            if (mStates.eyeOpen.get()) {
                mStates.eyeOpen.set(false)
                binding.passwordET.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                mStates.eyeOpen.set(true)
                binding.passwordET.inputType = InputType.TYPE_CLASS_TEXT
            }
            binding.passwordET.setSelection(binding.passwordET.text.toString().length)
        }

        fun onSwitchLoginWayClick() {
            mStates.isAccountLogin.set(!mStates.isAccountLogin.get())
        }

        fun onGetCodeClick() {
            if (TextUtils.isEmpty(mStates.phone.get())) {
                Toaster.show("请输入手机号")
                binding.phoneET.requestFocus()
                return
            }
            if (!RegexUtils.isMobileExact(mStates.phone.get())) {
                Toaster.show("手机号格式错误！")
                return
            }
            // 校验是否已同意隐私协议
            if (!mStates.agreeProtocol.get()) {
                Toaster.show("请勾选并同意隐私协议。")
                return
            }

            showLoadingDialog("处理中...")
            loginRequestViewModel.requestSendSmsCode(mStates.phone.get())
        }

        fun login() {
            //账号登录
            if (mStates.isAccountLogin.get()) {
                if (TextUtils.isEmpty(mStates.account.get())) {
                    Toaster.show("请输入账号")
                    binding.accountET.requestFocus()
                    return
                }
                if (TextUtils.isEmpty(mStates.password.get())) {
                    Toaster.show("请输入密码")
                    binding.passwordET.requestFocus()
                    return
                }
                // 校验是否已同意隐私协议
                if (!mStates.agreeProtocol.get()) {
                    Toaster.show("请勾选并同意隐私协议。")
                    return
                }

                showLoadingDialog("正在登录...")
                loginRequestViewModel.requestLoginByAccount(
                    mStates.account.get(),
                    mStates.password.get()
                )
            } else {
                //验证码登录
                if (TextUtils.isEmpty(mStates.phone.get())) {
                    Toaster.show("请输入手机号")
                    binding.phoneET.requestFocus()
                    return
                }
                if (!RegexUtils.isMobileExact(mStates.phone.get())) {
                    Toaster.show("手机号格式错误！")
                    return
                }
                if (TextUtils.isEmpty(mStates.code.get())) {
                    Toaster.show("请输入验证码")
                    binding.codeET.requestFocus()
                    return
                }
                // 校验是否已同意隐私协议
                if (!mStates.agreeProtocol.get()) {
                    Toaster.show("请勾选并同意隐私协议。")
                    return
                }
                
                showLoadingDialog("正在登录...")
                loginRequestViewModel.requestLoginByPhoneCaptcha(
                    mStates.phone.get(),
                    mStates.code.get()
                )
            }
        }
    }

    /**
     * 跳转到主界面
     */
    private fun redirectToMainActivity(delayMillis: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            MainActivity.start(this@LoginActivity)
            finish()
        }, delayMillis)
    }

    internal class MyClickText(private val context: Context, private val contentType: ContentType) :
        ClickableSpan() {

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            //设置文本的颜色
            ds.color =
                ColorUtils.getColor(R.color.colorPrimary)
            //超链接形式的下划线，false 表示不显示下划线，true表示显示下划线
            ds.isUnderlineText = false
        }

        override fun onClick(view: View) {
            if (contentType == ContentType.USER_PROTOCOL) {
                val url = "https://appassets.androidplatform.net/assets/private/user_protocol.html"
                WebviewActivity.startActivity(context, "米易通", url)
            } else {
                val url = "https://appassets.androidplatform.net/assets/private/privacy.html"
                WebviewActivity.startActivity(context, "米易通", url)
            }
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }
}
