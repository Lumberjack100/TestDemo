package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.AlarmTriggerValueEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product.UDInitialValueEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmMonitorPointInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmTriggerValueInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentM50SensorConfigBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50SensorConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/6/10
 * @desc: 一体式自供电 GNSS 接收机(M50)倾斜触发配置页面
 *
 */
class M50SensorConfigFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50SensorConfigBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50SensorConfigViewModel by activityViewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var queryMeasureResultTimeoutJob: Job? = null
    private var repeatPollNum = 0 //重复轮询次数
    private var measureInitialValueLoadingDialogId = ""


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_m50_sensor_config,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM50SensorConfigBinding
        binding.llToolbar.toolbar.title = "倾角触发配置"
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
        mStates.xCurrentAngle.set("")
        mStates.yCurrentAngle.set("")
        mStates.zCurrentAngle.set("")

        mStates.xInitialAngle.set("")
        mStates.yInitialAngle.set("")
        mStates.zInitialAngle.set("")

        mStates.xOffsetAngle.set("")
        mStates.yOffsetAngle.set("")
        mStates.zOffsetAngle.set("")

        mStates.isTriggerEnable.set(false)
        mStates.angleTrigger.set("")
    }

    inner class ClickProxy : BaseClickProxy() {

        /**
         * 更新倾角初始值
         */
        fun onUpdateTiltInitialValueClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            measureInitialValue("1", "2")
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
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL,
            if (mStates.isTriggerEnable.get()) "memsAlarmSw=1" else "memsAlarmSw=0"
        )
        commandItems.add(command)

        if (mStates.isTriggerEnable.get()) {
            if (mStates.angleTrigger.get().isEmpty()) {
                showMessageDialog("请输入角度触发值!")
                return
            }

            val triggerValueEntity = AlarmTriggerValueEntity(level1 = mStates.angleTrigger.get())
            val triggerValueCommand = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_ALRAM_BROADCAST_TRIGGER_VALUE,
                triggerValueEntity.toCommandString()
            )
            commandItems.add(triggerValueCommand)
        }

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
            measureInitialValueLoadingDialogId =
                showLoadingWithUUID(StringUtils.getString(R.string.processing))
        } else {
            Timber.d("查询更新初始值结果轮询次数：$repeatPollNum")
        }
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        //获取当前角度值，通过遥测获取，物模型103_1
        var command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE)
        commandItems.add(command)

        //获取初始角度值
        val entity = UDInitialValueEntity(
            method = "0",
            type = "2",
        )
        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_SENSOR_INITIAL,
            entity.toCommandString()
        )
        commandItems.add(command)

        //获取倾斜触发功能开关状态
        command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL)
        commandItems.add(command)

        //获取角度触发值
        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE
        )
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.SAMPLE)
                || (commandType == IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL)
                || (commandType == IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE)
                || (commandType == IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL)
                || (commandType == IOTCommandType.MD_SET_ALRAM_BROADCAST_TRIGGER_VALUE)

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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_SET_SENSOR_INITIAL -> {
                dismissLoadingDialog(measureInitialValueLoadingDialogId)
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
                        errMsg = "更新倾角初始值指令下发出错: $errMsg",
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                }
            }

            else -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowMessage,
                    isMessageDialog = isShowMessage
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
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))

        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_SET_SENSOR_INITIAL -> {
                dismissLoadingDialog(measureInitialValueLoadingDialogId)
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            else -> {
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowMessage,
                    isMessageDialog = isShowMessage
                )
            }
        }
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

        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_SET_SENSOR_INITIAL -> {
                dismissLoadingDialog(measureInitialValueLoadingDialogId)
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = true,
                    isMessageDialog = true,
                    errMsg = errMsg
                )
            }

            else -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = isShowMessage,
                    isMessageDialog = isShowMessage,
                    errMsg = errMsg
                )
            }
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.SAMPLE -> { // 召测
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.SAMPLE
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
                        initCurrentAngle(result.data)
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
                            if (cmdStr.contains("method=0")) "查询测量信息出错: ${result.message}" else "更新倾角初始值出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        processInitialAngle(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL -> {
                val result = iotParseManager.parse<AlarmMonitorPointInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL
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
                        initTriggerEnableData(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE -> {
                val result = iotParseManager.parse<AlarmTriggerValueInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initAngleTrigger(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL -> {//
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

            IOTCommandType.MD_SET_ALRAM_BROADCAST_TRIGGER_VALUE -> {//
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

    /**
     * 处理当前角度值显示
     */
    private fun initCurrentAngle(content: String) {
        try {
            // $cmd=sample&datastreams={"103_1":"0.012,-0.020,89.985,3.788,-28.510,-1022.529","224_1":"0.000,0.000,0.000"}
            val resultMap = MoshiUtil.fromJson<Map<String, String>>(content) ?: return
            val currentAngle = resultMap["103_1"] ?: AppContants.PLACE_HOLDER_VALUE
            currentAngle.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .let {
                    if (it.size >= 3) {
                        // 设置当前角度值
                        mStates.xCurrentAngle.set(it[0].formatDoubleValue("", 3))
                        mStates.yCurrentAngle.set(it[1].formatDoubleValue("", 3))
                        mStates.zCurrentAngle.set(it[2].formatDoubleValue("", 3))

                        // 如果已有初始角度值，立即计算偏移角度值
                        if (mStates.xInitialAngle.get().isNotEmpty() &&
                            mStates.yInitialAngle.get().isNotEmpty() &&
                            mStates.zInitialAngle.get().isNotEmpty()
                        ) {
                            calculateOffsetAngles()
                        }
                    }
                }

        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 处理查询初始角度值显示、更新倾角初始值
     */
    private fun processInitialAngle(resultMap: Map<String, String>) {
        try {
            val method = resultMap["method"] ?: ""
            val type = resultMap["type"] ?: ""
            if (method == "0") {//轮询测得的初始值
                // 已经有数据，处理倾角初始值
                if (resultMap.containsKey("xAxis") && resultMap.containsKey("yAxis") && resultMap.containsKey(
                        "zAxis"
                    )
                ) {
                    dismissLoadingDialog(measureInitialValueLoadingDialogId)
                    cancelNearbyCommunicationTimeoutJob()
                    showMessageDialog("初始值更新成功")

                    val xAxis = resultMap["xAxis"] ?: ""
                    val yAxis = resultMap["yAxis"] ?: ""
                    val zAxis = resultMap["zAxis"] ?: ""

                    mStates.xInitialAngle.set(xAxis.formatDoubleValue("", 3))
                    mStates.yInitialAngle.set(yAxis.formatDoubleValue("", 3))
                    mStates.zInitialAngle.set(zAxis.formatDoubleValue("", 3))

                    //处理角度偏移值：当前角度值减去初始角度值
                    calculateOffsetAngles()
                    return
                }

                cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog = false)
                startQueryMeasureResultJob(type)
            } else {//更新初始值指令
                clearQueryMeasureResultTimeoutJob()
                startQueryMeasureResultJob(type)
            }

        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 计算偏移角度值：当前角度值减去初始角度值
     */
    private fun calculateOffsetAngles() {
        try {
            // 获取当前角度值
            val xCurrent = mStates.xCurrentAngle.get().toDoubleOrNull() ?: 0.0
            val yCurrent = mStates.yCurrentAngle.get().toDoubleOrNull() ?: 0.0
            val zCurrent = mStates.zCurrentAngle.get().toDoubleOrNull() ?: 0.0

            // 获取初始角度值
            val xInitial = mStates.xInitialAngle.get().toDoubleOrNull() ?: 0.0
            val yInitial = mStates.yInitialAngle.get().toDoubleOrNull() ?: 0.0
            val zInitial = mStates.zInitialAngle.get().toDoubleOrNull() ?: 0.0

            // 计算偏移角度值 = 当前角度值 - 初始角度值
            val xOffset = xCurrent - xInitial
            val yOffset = yCurrent - yInitial
            val zOffset = zCurrent - zInitial

            // 设置偏移角度值（保留3位小数）
            mStates.xOffsetAngle.set(xOffset.formatDoubleValue("", 3))
            mStates.yOffsetAngle.set(yOffset.formatDoubleValue("", 3))
            mStates.zOffsetAngle.set(zOffset.formatDoubleValue("", 3))

            Timber.d("偏移角度计算完成: X偏移=${xOffset}, Y偏移=${yOffset}, Z偏移=${zOffset}")
        } catch (e: Exception) {
            Timber.e(e, "计算偏移角度值时出错")
            addDeviceLogItem(Log.ERROR, "计算偏移角度值时出错: ${e.message}")
        }
    }

    /**
     * 处理角度触发状态
     */
    private fun initTriggerEnableData(info: AlarmMonitorPointInfo) {
        try {
            info.memsAlarmSw.notNullKey {
                mStates.isTriggerEnable.set(it == "1")
            }

            //添加这行来保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * 处理角度触发值
     */
    private fun initAngleTrigger(info: AlarmTriggerValueInfo) {
        mStates.angleTrigger.set(info.devlevel1.formatDoubleValue("", 3))

        //添加这行来保存初始状态
        mStates.saveInitialState()
    }

    private fun startQueryMeasureResultJob(type: String) {
        //启动一个新的协程作为超时Job
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = launchWithViewLifecycle {
            if (repeatPollNum >= REPEAT_POLL_NUM) {
                dismissLoadingDialog(measureInitialValueLoadingDialogId)
                cancelNearbyCommunicationTimeoutJob()
                val errorMessage = "更新倾角初始值失败，请稍后重试"
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

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        const val REPEAT_POLL_NUM = 10
    }
} 