package com.shmedo.mcloudapp.ui.page.base.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.blankj.utilcode.util.ClickUtils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.data.extensions.getLogItem
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.ActivityErrorBinding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.LogViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel


/**
 * 作者　: hegaojian
 * 时间　: 2020/3/12
 * 描述　:
 */
class ErrorActivity : BaseActivity() {
    private lateinit var binding: ActivityErrorBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private lateinit var mStates: EmptyViewModel
    private lateinit var logViewModel: LogViewModel

    override fun initViewModel() {
        mStates = getActivityScopeViewModel()
        logViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_error, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as ActivityErrorBinding
        setToolBar(binding.llToolbar.toolbar)
        binding.llToolbar.toolbar.title = "发生错误"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun initData() {
        val config = CustomActivityOnCrash.getConfigFromIntent(intent)
        ClickUtils.applySingleDebouncing(binding.errorRestart) {
            config?.run {
                CustomActivityOnCrash.restartApplication(this@ErrorActivity, this)
            }
        }
        CustomActivityOnCrash.getStackTraceFromIntent(intent)?.let {
            logViewModel.insertLogItem(
                getLogItem(
                    sessionId = CommonMMKVOwner.appLogSessionId,
                    priority = Log.ERROR,
                    data = it
                )
            )
        }
    }
}