package com.shmedo.mcloudapp.ui.dialog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Process
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.View
import androidx.fragment.app.viewModels
import com.baidu.location.LocationClient
import com.baidu.mapapi.SDKInitializer
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil.setAgreePrivate
import com.shmedo.core.model.ContentType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.PrivacyDialogBinding
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseVmDbDialogFragment
import com.shmedo.mcloudapp.ui.page.webview.WebviewActivity
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.ui.viewmodel.state.PolicyViewModel

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/11/28 <br></br>
 * 描述：     TODO
 */
class PolicyDialog : BaseVmDbDialogFragment() {
    private val binding: PrivacyDialogBinding by lazy { mDatabind as PrivacyDialogBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: PolicyViewModel by viewModels()

    override val dataBindingConfig: DataBindingConfig
        get() = DataBindingConfig(R.layout.privacy_dialog, BR.vm, mStates).addBindingParam(
            BR.click,
            ClickProxy()
        )

    override fun setWindowStyle(gravity: Int) {
        super.setWindowStyle(Gravity.CENTER)
    }

    override fun initView(savedInstanceState: Bundle?) {
        val text = StringUtils.getString(R.string.privacy_agreement_desc)
        val spannableString = SpannableString(text)
        val start1 = text.indexOf("《用户协议和免责条款》")
        val end1 = start1 + "《用户协议和免责条款》".length
//        val start2 = text.indexOf("《隐私政策》")
//        val end2 = start2 + "《隐私政策》".length
        spannableString.setSpan(
            MyClickText(mActivity, ContentType.USER_PROTOCOL),
            start1,
            end1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
//        spannableString.setSpan(
//            MyClickText(mActivity, ContentType.PRIVACY_POLICY),
//            start2,
//            end2,
//            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//        )

        //当然这里也可以通过setSpan来设置哪些位置的文本哪些颜色
        binding.tvPrivacyDesc.text = spannableString
        binding.tvPrivacyDesc.movementMethod = LinkMovementMethod.getInstance() //不设置 没有点击事件
        binding.tvPrivacyDesc.highlightColor = Color.TRANSPARENT //设置点击后的颜色为透明
    }

    inner class ClickProxy {
        fun agree() {
            //更新同意隐私状态,需要在初始化地图之前完成
//            SDKInitializer.setAgreePrivacy(Utils.getApp(), true)
//            LocationClient.setAgreePrivacy(true)
            setAgreePrivate(true)
            mMessenger.updateIsAgreePolicy(true)
            dismiss()
        }

        fun disAgree() {
//            SDKInitializer.setAgreePrivacy(Utils.getApp(), false)
//            LocationClient.setAgreePrivacy(false)
            setAgreePrivate(false)
            Process.killProcess(Process.myPid())
        }
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
}
