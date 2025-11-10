package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product.UDAlarmReportModeEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product.UDInitialValueEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product.UDModuleGapParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUdProductSensorParamBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDSensorParamViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc: 一体式雷达水位/泥位计传感参数
 *
 * 优化特点：
 * 1. 使用新的通信架构，代码更简洁
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 保持原有的复杂业务逻辑不变（包括初始值测量、轮询机制）
 */
class UDSensorParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdProductSensorParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDSensorParamViewModel by activityViewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val captureFrequencyList =
        arrayListOf("15分钟/次", "30分钟/次", "1小时/次", "2小时/次")
    private val captureFrequencyMinList =
        arrayListOf("15", "30", "60", "120")//抓拍频率
    private val imageResolutionList = arrayListOf("1024x768", "1280x960", "1600x1200", "1920x1080")

    private var queryMeasureResultTimeoutJob: Job? = null
    private var repeatPollNum = 0 //重复轮询次数
    private var measureInitialValueLoadingDialogId = ""


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ud_product_sensor_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdProductSensorParamBinding
        binding.llToolbar.toolbar.title = "传感配置"
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
        //保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.measureInterval.set("5")//测量间隔
        mStates.installAngleOffsetThreshold.set("3")//安装角度偏移阈值
        mStates.airAltitudeInitialValue.set("")//海拔

        mStates.captureFrequency.set(captureFrequencyList[captureFrequencyList.lastIndex])
        mStates.imageResolution.set(imageResolutionList[2])

        mStates.locationInitialValue.set("")//位置
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 更新空高测量初始值
         */
        fun onUpdateAirAltitudeInitialValueClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            measureInitialValue("1", "1")
        }

        /**
         * 更新位置初始值
         */
        fun onUpdateLocationInitialValueClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            measureInitialValue("1", "2")
        }

        /**
         * 抓拍频率
         */
        fun onCaptureFrequencyClick() {
            val selectedIndex = captureFrequencyList.indexOf(mStates.captureFrequency.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", captureFrequencyList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.captureFrequency.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 图片分辨率
         */
        fun onImageResolutionClick() {
            val selectedIndex = imageResolutionList.indexOf(mStates.imageResolution.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", imageResolutionList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.imageResolution.set(text)
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

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    /**
     * 保存配置 - 使用新的通信架构
     */
    private fun initSaveCommand() {
        if (mStates.measureInterval.get().isEmpty()) {
            showMessageDialog("请输入测量间隔!")
            return
        }
        if (mStates.installAngleOffsetThreshold.get().isEmpty()) {
            showMessageDialog("请输入安装角度偏移阈值!")
            return
        }

        val commands = mutableListOf<String>()

        // 设置模块间隔
        val entity = UDModuleGapParamEntity(
            ld_module = mStates.measureInterval.get(),
            cam_module = captureFrequencyMinList[captureFrequencyList.indexOf(mStates.captureFrequency.get())],
        )
        val moduleGapCommand = IOTCommandUtil.getCommand(
            IOTCommandType.UD_MD_SET_MODULE_GAP,
            entity.toCommandString()
        )
        commands.add(moduleGapCommand)

        // 设置泥位计传感器
        val reportModeEntity = UDAlarmReportModeEntity(
            angle_threshol = mStates.installAngleOffsetThreshold.get(),
            pixx = mStates.imageResolution.get().split("x")[0],
            pixy = mStates.imageResolution.get().split("x")[1]
        )
        val sensorCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR,
            reportModeEntity.toCommandString()
        )
        commands.add(sensorCommand)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 测量初始值 - 使用新的通信架构
     */
    private fun measureInitialValue(method: String, type: String) {
        val entity = UDInitialValueEntity(
            method = method,
            type = type,
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_SENSOR_INITIAL,
            entity.toCommandString()
        )

        // 显示特殊的加载对话框（用于更新初始值）
        if (method == "1") {
            measureInitialValueLoadingDialogId =
                showLoadingWithUUID(StringUtils.getString(R.string.processing))
        } else {
            Timber.d("查询更新初始值结果轮询次数：$repeatPollNum")
        }

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 已经显示特殊的加载对话框了
                errorConfig = ErrorConfig.customConfig { error ->
                    dismissLoadingDialog(measureInitialValueLoadingDialogId)
                    if (command.contains("method=1")) {
                        val errorMsg = if (command.contains("type=1")) {
                            "更新测量初始值出错: ${error.message}"
                        } else {
                            "更新位置初始值出错: ${error.message}"
                        }
                        showMessageDialog(errorMsg)
                    } else {
                        showMessageDialog("查询测量信息出错: ${error.message}")
                    }
                }
            )
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询数据 - 使用新的通信架构
     */
    private fun queryData() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_DEVICE_STATUS, "method=4"
        )

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 状态查询失败显示Dialog
            )
        )
    }



    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_SENSOR_INITIAL -> {
                val result = iotParseManager.parse<Map<String, String>>(
                    cmdStr,
                    IOTCommandType.MD_SET_SENSOR_INITIAL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        dismissLoadingDialog(measureInitialValueLoadingDialogId)
                        val errMsg =
                            if (cmdStr.contains("method=0")) "查询测量信息出错: ${result.message}" else if (cmdStr.contains(
                                    "type=1"
                                )
                            ) "更新测量初始值出错: ${result.message}" else "更新位置初始值出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        processResponse(result.data)
                    }
                }
            }

            IOTCommandType.UD_MD_SET_MODULE_GAP -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting())
                            processNavigateUp()
                    }
                }
            }

            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting())
                            processNavigateUp()
                    }
                }
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    private fun initParamData(content: String) {
        launchWithViewLifecycle {
            try {
                val udCurrentStateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle

                mStates.measureInterval.set(udCurrentStateInfo.radarMeasureInterval)
                mStates.installAngleOffsetThreshold.set(udCurrentStateInfo.installAngleOffsetThreshold)
                mStates.airAltitudeInitialValue.set(udCurrentStateInfo.airAltitudeInitialValue)

                captureFrequencyMinList.indexOf(udCurrentStateInfo.captureFrequency)
                    .let { index ->
                        if (index in captureFrequencyList.indices) {
                            mStates.captureFrequency.set(captureFrequencyList[index])
                        }
                    }
                mStates.imageResolution.set("${udCurrentStateInfo.pixx}x${udCurrentStateInfo.pixy}")

                mStates.locationInitialValue.set(udCurrentStateInfo.locationInitialValue.ifEmpty { AppContants.PLACE_HOLDER_VALUE }
                    .replace(",", ", "))

                //添加这行来保存初始状态
                mStates.saveInitialState()
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 处理初始值响应数据
     */
    private fun processResponse(resultMap: Map<String, String>) {
        try {
            val method = resultMap["method"] ?: ""
            val type = resultMap["type"] ?: ""
            if (method == "0") {//轮询测得的初始值
                //已经有数据
                if (resultMap.containsKey("initvalue")) {
                    dismissLoadingDialog(measureInitialValueLoadingDialogId)
                    showMessageDialog("初始值更新成功")

                    val initValue = resultMap["initvalue"] ?: ""
                    if (type == "1") {
                        mStates.airAltitudeInitialValue.set(initValue)
                    } else {
                        mStates.locationInitialValue.set(initValue.ifEmpty { AppContants.PLACE_HOLDER_VALUE }
                            .replace(",", " , "))
                    }
                    return
                }

                // 无数据，继续轮询
                startQueryMeasureResultJob(type)
            } else { //更新初始值指令
                clearQueryMeasureResultTimeoutJob()
                startQueryMeasureResultJob(type)
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun startQueryMeasureResultJob(type: String) {
        //启动一个新的协程作为超时Job
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = launchWithViewLifecycle {
            if (repeatPollNum >= REPEAT_POLL_NUM) {
                dismissLoadingDialog(measureInitialValueLoadingDialogId)
                val errorMessage =
                    if (type == "1") "更新雷达测量初始值失败，请稍后重试" else "更新位置初始值失败，请稍后重试"
                showMessageDialog(errorMessage)
                return@launchWithViewLifecycle
            }
            delay(AppContants.Communication.DELAY_5000_MILLIS) //延迟 timeMillis 秒
            repeatPollNum++
            measureInitialValue("0", type)
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