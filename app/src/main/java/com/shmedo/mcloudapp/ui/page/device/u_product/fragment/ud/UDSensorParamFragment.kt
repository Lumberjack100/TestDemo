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
import com.shmedo.mcloudapp.databinding.FragmentUDProductSensorParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
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
 * @desc: 一体化雷达泥位计传感参数
 *
 */
class UDSensorParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUDProductSensorParamBinding
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

    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_u_d_product_sensor_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUDProductSensorParamBinding
        toolbarViewModel.toolbarTitleText.set("传感配置")
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
            if (isBleDisconnected()) {
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
            if (isBleDisconnected()) {
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
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        if (mStates.measureInterval.get().isEmpty()) {
            showMessageDialog("请输入测量间隔!")
            return
        }
        if (mStates.installAngleOffsetThreshold.get().isEmpty()) {
            showMessageDialog("请输入安装角度偏移阈值!")
            return
        }
        commandItems.clear()
        val entity = UDModuleGapParamEntity(
            ld_module = mStates.measureInterval.get(),
            cam_module = captureFrequencyMinList[captureFrequencyList.indexOf(mStates.captureFrequency.get())],
        )
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_MODULE_GAP,
            entity.toCommandString()
        )
        commandItems.add(command)

        val reportModeEntity = UDAlarmReportModeEntity(
            angle_threshol = mStates.installAngleOffsetThreshold.get(),
            pixx = mStates.imageResolution.get().split("x")[0],
            pixy = mStates.imageResolution.get().split("x")[1]
        )
        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR,
            reportModeEntity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun measureInitialValue(method: String, type: String) {
        commandItems.clear()
        val entity = UDInitialValueEntity(
            method = method,
            type = type,
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_SENSOR_INITIAL,
            entity.toCommandString()
        )
        commandItems.add(command)

        if (method == "1") {
            showLoadingDialog(StringUtils.getString(R.string.processing))
        } else {
            Timber.d("查询测量结果轮询次数：$repeatPollNum")
        }
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_DEVICE_STATUS, "method=4"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = "查询参数出错: $errMsg",
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            IOTCommandType.MD_SET_SENSOR_INITIAL -> {
                if (cmdStr.contains("method=0")) {
                    super.doCmdResponseResultError(
                        cmdStr = cmdStr,
                        errMsg = "查询测量信息出错: $errMsg",
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                } else {
                    super.doCmdResponseResultError(
                        cmdStr = cmdStr,
                        errMsg = if (cmdStr.contains("type=1")) "更新测量初始值指令下发出错: $errMsg" else "更新位置初始值指令下发出错: $errMsg",
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                }
            }

            IOTCommandType.MD_SET_MODULE_GAP,
            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = "数据保存出错: $errMsg",
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
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
            errMsg = errMsg,
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
            IOTCommandType.MD_GET_DEVICE_STATUS,
            IOTCommandType.MD_SET_SENSOR_INITIAL,
            IOTCommandType.MD_SET_MODULE_GAP,
            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR -> {
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
            IOTCommandType.MD_GET_DEVICE_STATUS -> {//
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
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_SENSOR_INITIAL -> {//
                val result = iotParseManager.parse<Map<String, String>>(
                    cmdStr,
                    IOTCommandType.MD_SET_SENSOR_INITIAL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
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

            IOTCommandType.MD_SET_MODULE_GAP -> {//设置测量间隔
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            processNavigateUp()
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR -> {//设置角度偏移阈值
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            processNavigateUp()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
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

                mStates.locationInitialValue.set(udCurrentStateInfo.locationInitialValue.ifEmpty { AppContants.PLACE_HOLDER_VALUE })

                //添加这行来保存初始状态
                mStates.saveInitialState()
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 处理
     */
    private fun processResponse(resultMap: Map<String, String>) {
        try {
            val method = resultMap["method"] ?: ""
            val type = resultMap["type"] ?: ""
            if (method == "0") {//轮询测得的初始值
                //已经有数据
                if (resultMap.containsKey("initvalue")) {
                    cancelNearbyCommunicationTimeoutJob()
                    showMessageDialog("初始值更新成功")

                    val initValue = resultMap["initvalue"] ?: ""
                    if (type == "1") {
                        mStates.airAltitudeInitialValue.set(initValue)
                    } else {
                        mStates.locationInitialValue.set(initValue.ifEmpty { AppContants.PLACE_HOLDER_VALUE })
                    }
                    return
                }

                cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog = false)
                startQueryMeasureResultJob(type)
            } else { //更新初始值指令
                clearQueryMeasureResultTimeoutJob()
                startQueryMeasureResultJob(type)
            }
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun startQueryMeasureResultJob(type: String) {
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

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        const val REPEAT_POLL_NUM = 10
    }
}