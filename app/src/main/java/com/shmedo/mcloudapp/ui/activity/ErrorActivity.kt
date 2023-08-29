package com.shmedo.mcloudapp.ui.activity

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.blankj.utilcode.util.ToastUtils
import com.gyf.immersionbar.ktx.immersionBar
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.util.clipboardManager
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ActivityErrorBinding
import com.shmedo.mcloudapp.ext.clickNoRepeat
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.ui.base.BaseActivity
import com.shmedo.mcloudapp.viewmodel.state.EmptyViewModel


/**
 * 作者　: hegaojian
 * 时间　: 2020/3/12
 * 描述　:
 */
class ErrorActivity : BaseActivity() {
    private val binding: ActivityErrorBinding by lazy { getBinding() as ActivityErrorBinding }
    private val mStates: EmptyViewModel by viewModels()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_error, BR.vm, mStates)
    }

    private fun initImmersionBar() {
        immersionBar {
            statusBarView(binding.llToolbar.toolbar)
            statusBarDarkFont(true)
            navigationBarDarkIcon(true)
            navigationBarColor(R.color.white)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initImmersionBar()
        binding.llToolbar.toolbar.title = "发生错误"
        val config = CustomActivityOnCrash.getConfigFromIntent(intent)
        binding.errorRestart.clickNoRepeat {
            config?.run {
                CustomActivityOnCrash.restartApplication(this@ErrorActivity, this)
            }
        }
        binding.errorSendError.clickNoRepeat {
            CustomActivityOnCrash.getStackTraceFromIntent(intent)?.let {
                showMessage(it, "发现有Bug不去打作者脸？", "必须打", {
                    val mClipData = ClipData.newPlainText("errorLog", it)
                    // 将ClipData内容放到系统剪贴板里。
                    clipboardManager?.setPrimaryClip(mClipData)
                    ToastUtils.showShort("已复制错误日志")
                    try {
                        val url = "mqqwpa://im/chat?chat_type=wpa&uin=824868922"
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        ToastUtils.showShort("请先安装QQ")
                    }
                }, "我不敢")
            }
        }
    }
}