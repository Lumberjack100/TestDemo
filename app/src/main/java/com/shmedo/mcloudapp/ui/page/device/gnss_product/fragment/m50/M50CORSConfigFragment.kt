package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.m50.M50CorsEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50CorsParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentM50CorsConfigBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50CORSConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/8/28
 * @desc: 一体式自供电 GNSS 接收机(M50)CORS 参数配置页面 - 支持4G和蓝牙两种通讯方式
 *
 */
class M50CORSConfigFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50CorsConfigBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50CORSConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()


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
        binding.llToolbar.toolbar.title = "CORS接入"
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
        // 保存初始状态
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
         * 连接/断开
         */
        fun onConnectClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()

        val networkConfigEntity = M50CorsEntity(
            sw = "1",
            host = mStates.domain.get().ifEmpty { IOTConstants.NULL_KEY },
            port = mStates.port.get().ifEmpty { IOTConstants.NULL_KEY },
            username = mStates.diffAccount.get().ifEmpty { IOTConstants.NULL_KEY },
            password = mStates.diffPassword.get().ifEmpty { IOTConstants.NULL_KEY }
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.M50_MD_SET_CORS,
            networkConfigEntity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.M50_MD_GET_CORS)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.M50_MD_GET_CORS) || (commandType == IOTCommandType.M50_MD_SET_CORS)

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultError(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
        )
    }

    /**
     * 4G 下发指令响应超时
     */
    override fun doCmdResponseResultTimeOut(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultTimeOut(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
        )
    }

    /**
     * 蓝牙下发指令响应超时
     */
    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean,
        errMsg: String
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.showNearbyCommunicationTimeoutAlert(
            cmdStr = cmdStr,
            isDismissLoadingDialog = isDismissLoadingDialog,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage,
            errMsg = errMsg
        )
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.M50_MD_GET_CORS -> {
                val result =
                    iotParseManager.parse<M50CorsParam>(
                        cmdStr,
                        IOTCommandType.M50_MD_GET_CORS
                    )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList { binding.refreshLayout.finish() }
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.M50_MD_SET_CORS -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList { processNavigateUp() }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initParamData(m50CorsParam: M50CorsParam) {
        try {
            // 设置网络类型
            mStates.isOpened.set(m50CorsParam.sw == "1")
            mStates.domain.set(m50CorsParam.host)
            mStates.port.set(m50CorsParam.port)
            mStates.diffAccount.set(m50CorsParam.username)
            mStates.diffPassword.set(m50CorsParam.password)

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }
    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

}



