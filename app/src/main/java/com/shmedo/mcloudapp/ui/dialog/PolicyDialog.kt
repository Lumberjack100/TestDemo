package com.shmedo.mcloudapp.ui.dialog

import android.os.Bundle
import android.os.Process
import android.view.Gravity
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
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

    }

    inner class ClickProxy {
        /** 隐私政策 */
        fun onPrivacyPolicyClick() {
            // val url = "https://mduser.shmedo.cn/privacy.html"
            val url = "https://appassets.androidplatform.net/assets/private/privacy.html"
            WebviewActivity.startActivity(mActivity, "米易通", url)
        }

        fun agree() {
            CommonMMKVOwner.isAgreePrivate = true
            mMessenger.updateIsAgreePolicy(true)
            dismiss()
        }

        fun disAgree() {
            CommonMMKVOwner.isAgreePrivate = false
            Process.killProcess(Process.myPid())
        }
    }
}
