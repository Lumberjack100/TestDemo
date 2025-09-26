package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.app.TimePickerDialog
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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.DataReportTypeEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.DataReportType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.SaveConfigMode
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentDasTerminalParameterBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasTerminalParameterViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2025/6/13
 * @desc: 物联网采集器(DAS)上报参数配置页面 - 支持 4G 通讯方式
 *
 */
class DasReportConfigFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasTerminalParameterBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: DasTerminalParameterViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val mdParseManager: MDParserManager by inject()
    private val reportMethodList: MutableList<String> = arrayListOf("固定间隔上报", "定时定点上报")


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_das_terminal_parameter, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasTerminalParameterBinding
        binding.llToolbar.toolbar.title = "上报配置"
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
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.reportMethod.set(reportMethodList[0])
        mStates.reportStartTimeHour.set("0")
        mStates.reportStartTimeMinute.set("0")
        mStates.interval.set("")
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 上报方式
         */
        fun onReportingMethodClick() {
            val selectedIndex = reportMethodList.indexOf(mStates.reportMethod.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择上报方式", reportMethodList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.reportMethod.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 上报起始时间
         */
        fun onReportingStartTimeClick() {
            TimePickerDialog(
                mActivity,
                { view, hourOfDay, minute ->
                    val time = String.Companion.format(Locale.getDefault(), "%2d", hourOfDay)
                    mStates.reportStartTimeHour.set(hourOfDay.toString())
                }, 0, 0, true
            ).show()
        }


        /**
         * 恢复默认配置
         */
        override fun onResetButtonClick() {
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

    private fun initSaveCommand() {
        // 参数验证
        if (!validateInputs()) {
            return
        }

        if (communicateWay == BleConnect) {
            initBleSaveCommand()
        } else {
            init4GSaveCommand()
        }
    }

    /**
     * 输入参数验证
     */
    private fun validateInputs(): Boolean {
        if (mStates.reportMethod.get().contains("定时定点")) {
            if (mStates.reportStartTimeHour.get().isEmpty()) {
                showMessageDialog("请选择起始时间（小时）!")
                return false
            }
            if (mStates.reportStartTimeMinute.get().isEmpty()) {
                showMessageDialog("请输入起始时间（分钟）!")
                return false
            }
            try {
                val value = mStates.reportStartTimeMinute.get().toDouble()
                if (value < 0 || value > 60) {
                    showMessageDialog("起始时间（分钟）数值范围[0,60]!")
                    return false
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的起始时间（分钟）!")
                return false
            }
        }

        if (mStates.interval.get().isEmpty()) {
            showMessageDialog("请输入时间间隔（分钟）!")
            return false
        }

        try {
            val value = mStates.interval.get().toDouble()
            if (value < 0 || value > 1440) {
                showMessageDialog("时间间隔（分钟）数值范围[0,1440]!")
                return false
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的时间间隔（分钟）!")
            return false
        }

        return true;
    }

    /**
     * 蓝牙通讯模式保存指令
     */
    private fun initBleSaveCommand() {
        val commands = mutableListOf<String>()

        var command = MDCommandUtil.getCommand(
            MDCommandType.DATA_REPORT_TYPE,
            if (mStates.reportMethod.get().contains("定时定点")) "11" else "10"
        )
        commands.add(command)
        Timber.d("设置上报方式===%s", command)

        if (mStates.reportMethod.get().contains("定时定点")) {
            command = MDCommandUtil.getCommand(
                MDCommandType.DATA_REPORT_TYPE, "2${mStates.reportStartTimeHour.get()}"
            )
            commands.add(command)
            Timber.d("设置起始时间（小时）===%s", command)

            command = MDCommandUtil.getCommand(
                MDCommandType.DATA_REPORT_TYPE, "3${mStates.reportStartTimeMinute.get()}"
            )
            commands.add(command)
            Timber.d("设置起始时间（分钟）===%s", command)
        }

        command = MDCommandUtil.getCommand(
            MDCommandType.DATA_REPORT_INTERVAL,
            mStates.interval.get()
        )
        Timber.d("设置上报时间间隔===%s", command)
        commands.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_REBOOT.toString()
        )
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 4G通讯模式保存指令
     */
    private fun init4GSaveCommand() {
        val entity = DataReportTypeEntity(
            type = (reportMethodList.indexOf(mStates.reportMethod.get())).toString(),
            timepoint = if (mStates.reportMethod.get().contains("定时定点"))
                mStates.reportStartTimeHour.get() else IOTConstants.NULL_KEY,
            timemin = if (mStates.reportMethod.get().contains("定时定点"))
                mStates.reportStartTimeMinute.get() else IOTConstants.NULL_KEY,
            timegap = mStates.interval.get(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_DATA_REPORT_TYPE,
            entity.toCommandString()
        )

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        if (communicateWay == BleConnect) {
            queryBleInfo()
        } else {
            query4GInfo()
        }
    }

    private fun queryBleInfo() {
        val command = MDCommandUtil.getCommand(MDCommandType.DATA_REPORT_TYPE, "0")

        Timber.d("查询上报配置参数===%s", command)
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    private fun query4GInfo() {
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_REPORT_TYPE)

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        if (communicateWay == BleConnect) {
            handleBleCommandResult(cmdStr)
        } else {
            handle4GCommandResult(cmdStr)
        }
    }

    /**
     * 处理蓝牙通讯指令结果
     */
    private fun handleBleCommandResult(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.DATA_REPORT_TYPE -> {
                val result = if (cmdStr.contains("${MDCommandType.DATA_REPORT_TYPE}0"))
                    mdParseManager.parse<DataReportType>(
                        cmdStr,
                        MDCommandType.DATA_REPORT_TYPE
                    )
                else mdParseManager.parse<String>(cmdStr)

                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "出错了"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is MDCommandResult.Success -> {
                        if (result.data is DataReportType) {
                            (result.data as? DataReportType)?.let { initReportMethod(it) }
                        }
                    }
                }
            }

            MDCommandType.DATA_REPORT_INTERVAL -> {//设置数据上报间隔
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "时间间隔配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.SAVE_CONFIG_INFO -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "保存出错!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            processNavigateUp()
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun handle4GCommandResult(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DATA_REPORT_TYPE -> {
                val result = iotParseManager.parse<DataReportType>(
                    cmdStr,
                    IOTCommandType.MD_GET_DATA_REPORT_TYPE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initReportMethod(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_DATA_REPORT_TYPE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting())
                            processNavigateUp()
                    }
                }
            }

            else -> {
                // 其他指令类型不做处理
            }
        }
    }

    private fun initReportMethod(info: DataReportType) {
        try {
            info.type.toIntOrNull()?.let {
                if (it in reportMethodList.indices) {
                    mStates.reportMethod.set(reportMethodList[it])
                }
            }
            mStates.reportStartTimeHour.set(info.timepoint)
            mStates.reportStartTimeMinute.set(info.timemin)
            mStates.interval.set(info.timegap)

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.Forest.e(e)
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