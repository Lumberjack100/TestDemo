package com.shmedo.mcloudapp.common.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.blankj.utilcode.util.ClickUtils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.addSystemLogItem
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.ActivityErrorBinding


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

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "发生错误"
        val config = CustomActivityOnCrash.getConfigFromIntent(intent)
//        binding.errorSendError.clickNoRepeat {
//            CustomActivityOnCrash.getStackTraceFromIntent(intent)?.let {
//                showMessage(it, "发现有Bug不去打作者脸？", "必须打", {
//                    val mClipData = ClipData.newPlainText("errorLog", it)
//                    // 将ClipData内容放到系统剪贴板里。
//                    clipboardManager?.setPrimaryClip(mClipData)
//                    ToastUtils.showShort("已复制错误日志")
//                    try {
//                        val url = "mqqwpa://im/chat?chat_type=wpa&uin=824868922"
//                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
//                    } catch (e: Exception) {
//                        ToastUtils.showShort("请先安装QQ")
//                    }
//                }, "我不敢")
//            }
//        }

        ClickUtils.applySingleDebouncing(binding.errorRestart) {
            config?.run {
                CustomActivityOnCrash.restartApplication(this@ErrorActivity, this)
            }
        }
        CustomActivityOnCrash.getStackTraceFromIntent(intent)?.let {
            addSystemLogItem(priority = Log.ERROR, data = it, mStates.viewModelScope)
        }
    }
}