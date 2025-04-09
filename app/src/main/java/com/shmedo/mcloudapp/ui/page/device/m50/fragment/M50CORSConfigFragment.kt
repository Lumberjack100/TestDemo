package com.shmedo.mcloudapp.ui.page.device.m50.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentM50CorsConfigBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50CORSConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2024/8/28
 * @desc: M50 CORS接入页面
 *
 */
class M50CORSConfigFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50CorsConfigBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50CORSConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var queryMeasureResultTimeoutJob: Job? = null
    private var repeatPollNum = 0 //重复轮询次数
    private var measureAltitudeLoadingDialogId = ""


    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_m50_cors_config,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM50CorsConfigBinding
        toolbarViewModel.toolbarTitleText.set("CORS接入")
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
        }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        resetDefaultParams()
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.isOpened.set(false)
        mStates.domain.set("rtk.ntrip.qxwz.com")
        mStates.port.set("8002")
        mStates.diffAccount.set("")
        mStates.diffPassword.set("")
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetDefaultParams()
        }

        /**
         * 更新海拔高度
         */
        fun onMeasureHeightClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }

        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_UD_DIFF_LOCATE, "method=0"
        )
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }



    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_UD_DIFF_LOCATE -> {
                val result = iotParseManager.parse<Map<String, String>>(
                    cmdStr,
                    IOTCommandType.MD_UD_DIFF_LOCATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "出错啦: ${result.message}"
                        dismissLoadingDialog(measureAltitudeLoadingDialogId)
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {

                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

}



