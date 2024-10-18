package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product.UDCORSParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUdCorsParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDCORSParamViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/8/28
 * @desc:  一体化雷达泥位计RTK测高参数
 *
 */
class UDCORSParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdCorsParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDCORSParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val modelList = arrayListOf("自动", "手动")

    private var queryMeasureResultTimeoutJob: Job? = null
    private var repeatPollNum = 0 //重复轮询次数

    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ud_cors_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdCorsParamBinding
        toolbarViewModel.toolbarTitleText.set("海拔配置")
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack()
        }
        registerOnBackPressedDispatcher {
            processBack()
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
        //保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.altitudeMeasureMode.set("自动")
        mStates.domain.set("rtk.ntrip.qxwz.com")
        mStates.port.set("8002")
        mStates.diffAccount.set("")
        mStates.diffPassword.set("")
        mStates.altitude.set("")
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择模式
         */
        fun onModelChooseClick() {
            val selectedIndex = modelList.indexOf(mStates.altitudeMeasureMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", modelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.altitudeMeasureMode.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

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
            measureAltitude()
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

    private fun measureAltitude() {
        commandItems.clear()
        if (mStates.altitudeMeasureMode.get() == "自动") {
            if (mStates.domain.get().isEmpty()) {
                showMessageDialog("请输入域名!")
                return
            }
            if (mStates.port.get().isEmpty()) {
                showMessageDialog("请输入端口!")
                return
            }
            if (mStates.diffAccount.get().isEmpty()) {
                showMessageDialog("请输入差分账号!")
                return
            }
            if (mStates.diffPassword.get().isEmpty()) {
                showMessageDialog("请输入差分密码!")
                return
            }
        } else {
            if (mStates.altitude.get().isEmpty()) {
                showMessageDialog("请输入海拔高度!")
                return
            }
        }

        val entity = UDCORSParamEntity(
            alt_get_mode = if (mStates.altitudeMeasureMode.get() == "自动") "0" else "1",
            host = if (mStates.altitudeMeasureMode.get() == "自动") mStates.domain.get() else IOTConstants.NULL_KEY,
            port = if (mStates.altitudeMeasureMode.get() == "自动") mStates.port.get() else IOTConstants.NULL_KEY,
            username = if (mStates.altitudeMeasureMode.get() == "自动") mStates.diffAccount.get() else IOTConstants.NULL_KEY,
            password = if (mStates.altitudeMeasureMode.get() == "自动") mStates.diffPassword.get() else IOTConstants.NULL_KEY,
            alt = if (mStates.altitudeMeasureMode.get() == "手动") mStates.altitude.get() else IOTConstants.NULL_KEY
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_UD_DIFF_LOCATE,
            "method=1&${entity.toCommandString()}"
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 轮询测得的海拔高度结果
     */
    private fun pollMeasureResult() {
        Timber.d("查询测量结果轮询次数：$repeatPollNum")
        commandItems.clear()
        val command =
            IOTCommandUtil.getCommand(IOTCommandType.MD_UD_DIFF_LOCATE, "method=2")
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 4G 下发指令响应错误
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_UD_DIFF_LOCATE -> {
                if (cmdStr.contains("method=0")) {
                    super.doCmdResponseResultError(
                        cmdStr = cmdStr,
                        errMsg = "查询配置参数信息出错: $errMsg",
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                } else {
                    super.doCmdResponseResultError(
                        cmdStr = cmdStr,
                        errMsg = "更新海拔高度指令下发出错: $errMsg",
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                }
            }

            else -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog
                )
            }
        }
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
        super.doCmdResponseResultTimeOut(
            cmdStr = cmdStr,
            errMsg = "设备未响应",
            isShowErrMsg = true,
            isMessageDialog = true
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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_UD_DIFF_LOCATE -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = true,
                    isMessageDialog = isMessageDialog,
                    errMsg = "设备未响应"
                )
            }

            else -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }
        }
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
                        val errMsg =
                            if (cmdStr.contains("method=0")) "查询参数出错: ${result.message}" else "更新海拔高度出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        processResponse(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    /**
     * 处理
     */
    private fun processResponse(resultMap: Map<String, String>) {
        try {
            resultMap["method"]?.let { code ->
                when (code) {
                    "0" -> {//查询海拔配置页面信息
                        cancelNearbyCommunicationTimeoutJob()

                        val altitudeMeasureMode = resultMap["alt_get_mode"] ?: ""
                        val domain = resultMap["host"] ?: ""
                        val port = resultMap["port"] ?: ""
                        val diffAccount = resultMap["username"] ?: ""
                        val diffPassword = resultMap["username"] ?: ""
                        val altitude = resultMap["alt"] ?: ""

                        mStates.altitudeMeasureMode.set(
                            if (altitudeMeasureMode == "0") "自动" else "手动"
                        )
                        mStates.domain.set(domain)
                        mStates.port.set(port)
                        mStates.diffAccount.set(diffAccount)
                        mStates.diffPassword.set(diffPassword)
                        mStates.altitude.set(altitude)

                        //添加这行来保存初始状态
                        mStates.saveInitialState()
                    }

                    "1" -> {//更新海拔高度
                        clearQueryMeasureResultTimeoutJob()
                        startQueryMeasureResultJob()
                    }

                    "2" -> {//轮询测得的海拔高度结果
                        //已经有数据
                        if (resultMap.containsKey("alt")) {
                            cancelNearbyCommunicationTimeoutJob()
                            Toaster.show("更新海拔高度成功")

                            val altitude = resultMap["alt"] ?: ""
                            mStates.altitude.set(altitude)

                            //添加这行来保存初始状态
                            mStates.saveInitialState()
//                            processNavigateUp()
                            return
                        }

                        cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog = false)
                        startQueryMeasureResultJob()
                    }

                    else -> {}
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun startQueryMeasureResultJob() {
        //启动一个新的协程作为超时Job
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = launchWithViewLifecycle {
            if (repeatPollNum >= REPEAT_POLL_NUM) {
                cancelNearbyCommunicationTimeoutJob()
                showMessageDialog("更新海拔高度失败，请稍后重试")
                return@launchWithViewLifecycle
            }
            delay(AppContants.Communication.DELAY_5000_MILLIS) //延迟 timeMillis 秒
            repeatPollNum++
            pollMeasureResult()
        }
    }

    private fun clearQueryMeasureResultTimeoutJob() {
        repeatPollNum = 0
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = null
    }

    private fun processNavigateUp() {
        launchWithViewLifecycle {
            delay(1500)
            nav().navigateUp()
        }
    }

    private fun processBack() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

    private fun showExitConfirmationDialog() {
        showMessage(
            StringUtils.getString(R.string.data_modified_warn),
            "提示",
            "确定",
            {
                nav().navigateUp()
            },
            "取消"
        )
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        const val REPEAT_POLL_NUM = 10
    }
}