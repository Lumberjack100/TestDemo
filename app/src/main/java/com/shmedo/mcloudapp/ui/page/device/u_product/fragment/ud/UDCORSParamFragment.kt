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
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentUdCorsParamBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDCORSParamViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/8/28
 * @desc:  一体式雷达水位/泥位计RTK测高参数
 *
 * 优化特点：
 * 1. 使用新的通信架构，代码更简洁
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 保持原有的复杂业务逻辑不变
 */
class UDCORSParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdCorsParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDCORSParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val modelList = arrayListOf("自动", "手动")

    private var queryMeasureResultTimeoutJob: Job? = null
    private var repeatPollNum = 0 //重复轮询次数
    private var measureAltitudeLoadingDialogId = ""


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
        binding.llToolbar.toolbar.title = "海拔配置"
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
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                finishRefresh()
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
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            measureAltitude()
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询数据 - 使用新的通信架构
     */
    private fun queryData() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_DIFF_LOCATE, "method=0"
        )

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig(), // 状态查询失败显示Dialog
            )
        )
    }

    /**
     * 测量海拔高度 - 使用新的通信架构
     */
    private fun measureAltitude() {
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
            IOTCommandType.MD_DIFF_LOCATE,
            "method=1&${entity.toCommandString()}"
        )

        // 显示特殊的加载对话框（用于更新海拔）
        measureAltitudeLoadingDialogId =
            showLoadingWithUUID(StringUtils.getString(R.string.processing))

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 已经显示特殊的加载对话框了
                errorConfig = ErrorConfig.customConfig { error ->
                    dismissLoadingDialog(measureAltitudeLoadingDialogId)
                    showMessageDialog("更新海拔高度出错: ${error.message}")
                }
            )
        )
    }

    /**
     * 轮询测得的海拔高度结果 - 使用新的通信架构
     */
    private fun pollMeasureResult() {
        Timber.d("查询测量海拔高度结果轮询次数：$repeatPollNum")
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_DIFF_LOCATE, "method=2")

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 已经显示了加载对话框
                errorConfig = ErrorConfig.customConfig { error ->
                    dismissLoadingDialog(measureAltitudeLoadingDialogId)
                    showMessageDialog("查询测量海拔高度结果出错: ${error.message}")
                }
            )
        )
    }



    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_DIFF_LOCATE -> {
                val result = iotParseManager.parse<Map<String, String>>(
                    cmdStr,
                    IOTCommandType.MD_DIFF_LOCATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "出错啦: ${result.message}"
                        dismissLoadingDialog(measureAltitudeLoadingDialogId)
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        processResponse(result.data)
                    }
                }
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    /**
     * 处理指令响应数据
     */
    private fun processResponse(resultMap: Map<String, String>) {
        try {
            resultMap["method"]?.let { code ->
                when (code) {
                    "0" -> {//查询海拔配置页面信息
                        val altitudeMeasureMode = resultMap["alt_get_mode"] ?: ""
                        val domain = resultMap["host"] ?: ""
                        val port = resultMap["port"] ?: ""
                        val diffAccount = resultMap["username"] ?: ""
                        val diffPassword = resultMap["password"] ?: ""
                        val altitude = resultMap["alt"] ?: ""

                        mStates.altitudeMeasureMode.set(
                            if (altitudeMeasureMode == "0") "自动" else "手动"
                        )
                        mStates.domain.set(domain)
                        mStates.port.set(port)
                        mStates.diffAccount.set("")
                        mStates.diffPassword.set("")
                        mStates.altitude.set(altitude)

                        mStates.saveInitialState()
                    }

                    "1" -> {//更新海拔高度
                        if (mStates.altitudeMeasureMode.get() == "自动") {
                            clearQueryMeasureResultTimeoutJob()
                            startQueryMeasureResultJob()
                        } else {
                            dismissLoadingDialog(measureAltitudeLoadingDialogId)
                            Toaster.show("更新海拔高度成功")
                            mStates.saveInitialState()
                        }
                    }

                    "2" -> {//轮询测得的海拔高度结果
                        //已经有数据
                        if (resultMap.containsKey("alt")) {
                            dismissLoadingDialog(measureAltitudeLoadingDialogId)
                            Toaster.show("更新海拔高度成功")

                            val altitude = resultMap["alt"] ?: ""
                            mStates.altitude.set(altitude)

                            //添加这行来保存初始状态
                            mStates.saveInitialState()
                            return
                        }

                        // 无数据，继续轮询
                        startQueryMeasureResultJob()
                    }

                    else -> {}
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun startQueryMeasureResultJob() {
        //启动一个新的协程作为超时Job
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = launchWithViewLifecycle {
            if (repeatPollNum >= REPEAT_POLL_NUM) {
                dismissLoadingDialog(measureAltitudeLoadingDialogId)
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

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }


    companion object {
        const val REPEAT_POLL_NUM = 10
    }
}