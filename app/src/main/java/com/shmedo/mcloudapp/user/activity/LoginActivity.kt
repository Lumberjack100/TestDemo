package com.shmedo.mcloudapp.user.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.activity.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.activity.BaseActivity
import com.shmedo.mcloudapp.common.activity.MainActivity
import com.shmedo.mcloudapp.common.activity.WebviewActivity
import com.shmedo.mcloudapp.common.widget.MyCountDownTimer
import com.shmedo.mcloudapp.databinding.ActivityLoginBinding
import com.shmedo.mcloudapp.user.model.ContentType
import com.shmedo.mcloudapp.user.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.user.viewmodel.state.LoginViewModel
import com.tencent.bugly.crashreport.CrashReport
import java.lang.Boolean
import kotlin.Long
import kotlin.String
import kotlin.arrayOf
import kotlin.getValue
import kotlin.lazy
import kotlin.toString

class LoginActivity : BaseActivity() {
    private val binding: ActivityLoginBinding by lazy { getBinding() as ActivityLoginBinding }
    private val mStates: LoginViewModel by viewModels()
    private val loginRequestViewModel: LoginRequestViewModel by viewModels()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_login, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        initImmersionBar(binding.statusBarView, false)
    }

    override fun initData() {
        val filter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!"_0123456789qwertzuiopasdfghjklyxcvbnmQWERTZUIOPASDFGHJKLYXCVBNM".contains(
                        source[i].toString() + ""
                    )
                ) {
                    return@InputFilter ""
                }
            }
            null
        }
        binding.accountET.filters = arrayOf(InputFilter.LengthFilter(20), filter)
//        binding.passwordET.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(25))
        val mAccount = MmkvCacheUtil.getUserName()
        //自动登录
        if (!TextUtils.isEmpty(mAccount)) {
            binding.accountET.setText(mAccount)
            binding.accountET.setSelection(mAccount.length)
        }
        initLoginUserProtocol()
    }

    private fun initLoginUserProtocol() {
        val text = StringUtils.getString(R.string.login_protocol_desc)
        val spannableString = SpannableString(text)
        val start1 = text.indexOf("《用户协议与免责条款》")
        val end1 = start1 + "《用户协议与免责条款》".length
        spannableString.setSpan(
            MyClickText(this, ContentType.USER_PROTOCOL),
            start1,
            end1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //当然这里也可以通过setSpan来设置哪些位置的文本哪些颜色
        binding.tvUserProtocol.text = spannableString
        binding.tvUserProtocol.movementMethod = LinkMovementMethod.getInstance() //不设置 没有点击事件
        binding.tvUserProtocol.highlightColor = Color.TRANSPARENT //设置点击后的颜色为透明
    }

    override fun createObserver() {
        loginRequestViewModel.sendCodeResult.observe(this) { dataResult: DataResult<String> ->
            dismissLoading()
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("验证码已发送")
            val timer = MyCountDownTimer(binding.tvGetCode, 60000, 1000)
            timer.setTextColor(R.color.title_text_color, R.color.text_color_b3b3b3)
            timer.start()
        }
        loginRequestViewModel.loginResult.observe(this) { dataResult: DataResult<String> ->
            dismissLoading()
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show("登录失败: " + dataResult.responseStatus.errorMessage)
                return@observe
            }
            CrashReport.setUserId(MmkvCacheUtil.getUserName()) //该用户本次启动后的异常日志用户account
            redirectToMainActivity(500)
        }
    }

    inner class ClickProxy {
        fun eyeClick() {
            if (Boolean.TRUE == mStates.eyeOpen.get()) {
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
            showLoading("处理中...")
            loginRequestViewModel.requestSendSmsCode(mStates.phone.get())
        }

        fun login() {
            //账号登录
            if (Boolean.TRUE == mStates.isAccountLogin.get()) {
                if (TextUtils.isEmpty(mStates.name.get())) {
                    Toaster.show("请输入账号")
                    binding.accountET.requestFocus()
                    return
                }
                if (TextUtils.isEmpty(mStates.password.get())) {
                    Toaster.show("请输入密码")
                    binding.passwordET.requestFocus()
                    return
                }
                showLoading("正在登录...")
                loginRequestViewModel.requestLogin(mStates.name.get(), mStates.password.get())
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
                showLoading("正在登录...")
                loginRequestViewModel.requestQuickLogin(mStates.phone.get(), mStates.code.get())
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
                val url = "file:///android_asset/private/UserProtocol.html"
                WebviewActivity.startActivity(context, "用户协议与免责条款", url)
            } else {
                val url = "file:///android_asset/private/PrivacyPolicy.html"
                WebviewActivity.startActivity(context, "隐私政策", url)
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