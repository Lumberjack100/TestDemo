package com.shmedo.mcloudapp.common.activity

import android.os.Bundle
import android.util.Log
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.blankj.utilcode.util.ClickUtils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.viewmodel.LogViewModel
import com.shmedo.lib.core.ext.getActivityScopeViewModel
import com.shmedo.lib.core.ext.getLogItem
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.ActivityErrorBinding
import org.koin.androidx.viewmodel.ext.android.getViewModel


/**
 * 作者　: hegaojian
 * 时间　: 2020/3/12
 * 描述　:
 */
class ErrorActivity : BaseActivity() {
    private lateinit var binding: ActivityErrorBinding
    private lateinit var mStates: EmptyViewModel
    private lateinit var logViewModel: LogViewModel

    override fun initViewModel() {
        mStates = getActivityScopeViewModel()
        logViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_error, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as ActivityErrorBinding
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
            logViewModel.insertLog(
                getLogItem(
                    sessionId = MmkvCacheUtil.getAppLogSessionId(),
                    priority = Log.ERROR,
                    data = it
                )
            )
        }
    }
}