package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product.UDInitialValueEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50CurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentM50SensorConfigBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50SensorConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/6/10
 * @desc: M50传感配置页面
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
        binding.llToolbar.toolbar.title = "传感配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
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
        mStates.longitude.set("")
        mStates.latitude.set("")
        mStates.altitude.set("")

        mStates.xAxis.set("")
        mStates.yAxis.set("")
        mStates.zAxis.set("")
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 更新位置初始值
         */
        fun onUpdateLocationInitialValueClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            measureInitialValue("1", "1")
        }

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
            processNavigateUp()
        }
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
        val command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
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
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = "查询参数出错: $errMsg",
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

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
                        errMsg = if (cmdStr.contains("type=1")) "更新位置初始值指令下发出错: $errMsg" else "更新倾角初始值指令下发出错: $errMsg",
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                }
            }

            else -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = false,
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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS,
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
                    isShowErrMsg = false,
                    isMessageDialog = isMessageDialog
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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS,
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
                    isShowErrMsg = false,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_DEVICE_STATUS
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
                        initM50StatusInfo(result.data)
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
                            ) "更新位置初始值出错: ${result.message}" else "更新倾角初始值出错: ${result.message}"
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

    private fun initM50StatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<M50CurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle

                //109.709961,31.139160,33.0862
                stateInfo.locationInitialValue.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .let {
                        if (it.size == 3) {
                            mStates.longitude.set("E ${it[0]}")
                            mStates.latitude.set("N ${it[1]}")
                            mStates.altitude.set(it[2])
                        }
                    }

                stateInfo.angleInitialValue.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .let {
                        if (it.size == 3) {
                            // 设置倾角配置参数
                            mStates.xAxis.set(it[0])
                            mStates.yAxis.set(it[1])
                            mStates.zAxis.set(it[2])
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 处理响应
     */
    private fun processResponse(resultMap: Map<String, String>) {
        try {
            val type = resultMap["type"] ?: ""
            resultMap["method"]?.let { code ->
                when (code) {
                    "0" -> {//轮询测得的初始值
                        if (type == "1") {
                            if (resultMap.containsKey("lng") && resultMap.containsKey("lat") && resultMap.containsKey("alt")) {
                                // 处理GNSS位置初始值
                                dismissLoadingDialog(measureInitialValueLoadingDialogId)
                                cancelNearbyCommunicationTimeoutJob()
                                showMessageDialog("初始值更新成功")

                                val longitude = resultMap["lng"] ?: ""
                                val latitude = resultMap["lat"] ?: ""
                                val altitude = resultMap["alt"] ?: ""

                                mStates.longitude.set("E $longitude")
                                mStates.latitude.set("N $latitude")
                                mStates.altitude.set(altitude)
                                return
                            }

                            cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog = false)
                            startQueryMeasureResultJob(type)
                        } else {// 处理倾角初始值
                            if (resultMap.containsKey("xAxis") && resultMap.containsKey("yAxis") && resultMap.containsKey("zAxis")) {
                                dismissLoadingDialog(measureInitialValueLoadingDialogId)
                                cancelNearbyCommunicationTimeoutJob()
                                showMessageDialog("初始值更新成功")

                                val xAxis = resultMap["xAxis"] ?: ""
                                val yAxis = resultMap["yAxis"] ?: ""
                                val zAxis = resultMap["zAxis"] ?: ""

                                mStates.xAxis.set(xAxis)
                                mStates.yAxis.set(yAxis)
                                mStates.zAxis.set(zAxis)
                                return
                            }

                            cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog = false)
                            startQueryMeasureResultJob(type)
                        }
                    }

                    else -> {
                        clearQueryMeasureResultTimeoutJob()
                        startQueryMeasureResultJob(type)
                    }
                }
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
                cancelNearbyCommunicationTimeoutJob()
                val errorMessage =
                    if (type == "1") "更新位置初始值失败，请稍后重试" else "更新倾角初始值失败，请稍后重试"
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