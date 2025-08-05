package com.shmedo.mcloudapp.ui.page.device.common

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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.AlarmMonitorPointEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.AlarmReportIntervalEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.AlarmTriggerValueEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmMonitorPointInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmReportIntervalInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmTriggerValueInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentAlarmParamSettingBinding
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.AlarmParamSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/1/22
 * @desc: 优化后的通用报警参数设置页面
 *
 * 优化特点：
 * 1. 使用新的通信架构，代码更简洁
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 支持多种产品类型的差异化处理
 * 5. 提取数据验证逻辑，提高可读性
 */
class AlarmParamSettingFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAlarmParamSettingBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: AlarmParamSettingViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val monitorPointList: List<String> = (1..10).map { it.toString() }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_alarm_param_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAlarmParamSettingBinding
        binding.llToolbar.toolbar.title = "报警参数配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher { handleBackByCheckDataModified() }
        initRefresh()
    }

    override fun initData() {
        super.initData()
        initTitles()
        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    /**
     * 根据产品类型初始化标题
     */
    private fun initTitles() {
        when (productType) {
            ProductType.GNSS_M_1, // M20S
            ProductType.GNSS_M_2,
            ProductType.U_R_1 // 一体式雨量计
                -> {
                mStates.firstAlarmThresholdTitle.set("一级报警阈值（毫米）")
                mStates.secondAlarmThresholdTitle.set("二级报警阈值（毫米）")
                mStates.thirdAlarmThresholdTitle.set("三级报警阈值（毫米）")
                mStates.fourthAlarmThresholdTitle.set("四级报警阈值（毫米）")

                mStates.firstAlarmReportIntervalTitle.set("一级报警间隔（秒）")
                mStates.secondAlarmReportIntervalTitle.set("二级报警间隔（秒）")
                mStates.thirdAlarmReportIntervalTitle.set("三级报警间隔（秒）")
                mStates.fourthAlarmReportIntervalTitle.set("四级报警间隔（秒）")
            }

            ProductType.U_I_1 -> { // 倾斜仪
                mStates.firstAlarmThresholdTitle.set("一级报警阈值（度）")
                mStates.secondAlarmThresholdTitle.set("二级报警阈值（度）")
                mStates.thirdAlarmThresholdTitle.set("三级报警阈值（度）")
                mStates.fourthAlarmThresholdTitle.set("四级报警阈值（度）")

                mStates.firstAlarmReportIntervalTitle.set("一级报警间隔（秒）")
                mStates.secondAlarmReportIntervalTitle.set("二级报警间隔（秒）")
                mStates.thirdAlarmReportIntervalTitle.set("三级报警间隔（秒）")
                mStates.fourthAlarmReportIntervalTitle.set("四级报警间隔（秒）")
            }

            else -> {}
        }
    }

    /**
     * 根据产品类型重置默认参数
     */
    private fun resetDefaultParams() {
        // 监测点编号 [1~15] 默认01
        mStates.monitorPoint.set("1")
        // 播报次数 [0~255] 其中0表示关闭当前报警，255表示一直报警，默认03
        mStates.broadcastTimes.set("3")

        // 一级报警上报间隔 默认60,单位s
        mStates.firstAlarmReportInterval.set("60")
        // 二级报警上报间隔 默认300,单位s
        mStates.secondAlarmReportInterval.set("300")
        // 三级报警上报间隔 默认1800,单位s
        mStates.thirdAlarmReportInterval.set("1800")
        // 四级报警上报间隔 默认3600,单位s
        mStates.fourthAlarmReportInterval.set("3600")

        when (productType) {
            ProductType.LR200 -> { // 一体式雨量计
                // 一级报警语音编号  [1~255] 默认 4
                mStates.firstAlarmVoice.set("84")
                // 二级报警语音编号  [1~255] 默认 3
                mStates.secondAlarmVoice.set("83")
                // 三级报警语音编号  [1~255] 默认 2
                mStates.thirdAlarmVoice.set("82")
                // 四级报警语音编号  [1~255] 默认 1
                mStates.fourthAlarmVoice.set("81")

                // 一级报警阈值
                mStates.firstAlarmThreshold.set("80")
                // 二级报警阈值
                mStates.secondAlarmThreshold.set("60")
                // 三级报警阈值
                mStates.thirdAlarmThreshold.set("40")
                // 四级报警阈值
                mStates.fourthAlarmThreshold.set("20")
            }

            else -> {
                // 一级报警语音编号  [1~255] 默认 4
                mStates.firstAlarmVoice.set("4")
                // 二级报警语音编号  [1~255] 默认 3
                mStates.secondAlarmVoice.set("3")
                // 三级报警语音编号  [1~255] 默认 2
                mStates.thirdAlarmVoice.set("2")
                // 四级报警语音编号  [1~255] 默认 1
                mStates.fourthAlarmVoice.set("1")

                // 一级报警阈值
                mStates.firstAlarmThreshold.set("40")
                // 二级报警阈值
                mStates.secondAlarmThreshold.set("20")
                // 三级报警阈值
                mStates.thirdAlarmThreshold.set("10")
                // 四级报警阈值
                mStates.fourthAlarmThreshold.set("5")
            }
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询数据 - 使用新架构的指令序列API
     */
    private fun queryData() {
        val commands = listOf(
            // 获取报警控制参数
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL),
            // 获取报警阈值参数
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE),
            // 获取报警间隔参数
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL)
        )

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                timeout = AppContants.Communication.DELAY_10000_MILLIS,
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    /**
     * 保存配置 - 使用新架构的简化API
     */
    private fun saveConfiguration() {
        // 数据验证
        if (!validateInputData()) {
            return
        }

        val commands = mutableListOf<String>()

        // 设置报警监测点参数
        val entity = AlarmMonitorPointEntity(
            monitorpoint = mStates.monitorPoint.get(),
            cnt = mStates.broadcastTimes.get(),
            level1 = mStates.firstAlarmVoice.get(),
            level2 = mStates.secondAlarmVoice.get(),
            level3 = mStates.thirdAlarmVoice.get(),
            level4 = mStates.fourthAlarmVoice.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL,
            entity.toCommandString()
        )
        commands.add(command)

        // 设置报警阈值（根据产品类型使用不同字段）
        val triggerValueEntity = if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
            AlarmTriggerValueEntity(
                devlevel1 = mStates.firstAlarmThreshold.get(),
                devlevel2 = mStates.secondAlarmThreshold.get(),
                devlevel3 = mStates.thirdAlarmThreshold.get(),
                devlevel4 = mStates.fourthAlarmThreshold.get()
            )
        } else {
            AlarmTriggerValueEntity(
                level1 = mStates.firstAlarmThreshold.get(),
                level2 = mStates.secondAlarmThreshold.get(),
                level3 = mStates.thirdAlarmThreshold.get(),
                level4 = mStates.fourthAlarmThreshold.get()
            )
        }
        val triggerValueCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_BROADCAST_TRIGGER_VALUE,
            triggerValueEntity.toCommandString()
        )
        commands.add(triggerValueCommand)

        // 设置报警间隔
        val reportIntervalEntity = AlarmReportIntervalEntity(
            level1 = mStates.firstAlarmReportInterval.get(),
            level2 = mStates.secondAlarmReportInterval.get(),
            level3 = mStates.thirdAlarmReportInterval.get(),
            level4 = mStates.fourthAlarmReportInterval.get()
        )
        val reportIntervalCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_BROADCAST_REPORT_INTERVAL,
            reportIntervalEntity.toCommandString()
        )
        commands.add(reportIntervalCommand)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                timeout = AppContants.Communication.DELAY_10000_MILLIS,
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 数据验证逻辑 - 提取为独立方法提高可读性
     */
    private fun validateInputData(): Boolean {
        // 验证播报次数
        if (mStates.broadcastTimes.get().isEmpty()) {
            showMessageDialog("请输入播报次数!")
            return false
        }
        try {
            val value = mStates.broadcastTimes.get().toInt()
            if (value < 0 || value > 255) {
                showMessageDialog("播报次数范围[0,255]!")
                return false
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的播报次数!")
            return false
        }

        // 验证语音编号
        val voiceFields = listOf(
            "一级报警语音编号" to mStates.firstAlarmVoice.get(),
            "二级报警语音编号" to mStates.secondAlarmVoice.get(),
            "三级报警语音编号" to mStates.thirdAlarmVoice.get(),
            "四级报警语音编号" to mStates.fourthAlarmVoice.get()
        )

        for ((name, value) in voiceFields) {
            if (value.isEmpty()) {
                showMessageDialog("请输入$name!")
                return false
            }
            try {
                val intValue = value.toInt()
                if (intValue < 1 || intValue > 255) {
                    showMessageDialog("${name}范围[1,255]!")
                    return false
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的$name!")
                return false
            }
        }

        // 验证报警阈值
        val thresholdFields = listOf(
            "一级报警阈值" to mStates.firstAlarmThreshold.get(),
            "二级报警阈值" to mStates.secondAlarmThreshold.get(),
            "三级报警阈值" to mStates.thirdAlarmThreshold.get(),
            "四级报警阈值" to mStates.fourthAlarmThreshold.get()
        )

        for ((name, value) in thresholdFields) {
            if (value.isEmpty()) {
                showMessageDialog("请输入$name!")
                return false
            }
            try {
                value.toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的$name!")
                return false
            }
        }

        // 验证报警间隔
        val intervalFields = listOf(
            "一级报警上报间隔" to mStates.firstAlarmReportInterval.get(),
            "二级报警上报间隔" to mStates.secondAlarmReportInterval.get(),
            "三级报警上报间隔" to mStates.thirdAlarmReportInterval.get(),
            "四级报警上报间隔" to mStates.fourthAlarmReportInterval.get()
        )

        for ((name, value) in intervalFields) {
            if (value.isEmpty()) {
                showMessageDialog("请输入$name!")
                return false
            }
            try {
                value.toInt()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的$name!")
                return false
            }
        }

        return true
    }

    /**
     * 处理指令响应 - 这是唯一需要实现的方法
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL -> {
                val result = iotParseManager.parse<AlarmMonitorPointInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询语音参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initAlarmMonitorPointData(result.data)
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
                        val errMsg = "查询报警阈值出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initAlarmTriggerValueData(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL -> {
                val result = iotParseManager.parse<AlarmReportIntervalInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_REPORT_INTERVAL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询报警间隔出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initAlarmReportIntervalData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置语音参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting())
                            processNavigateUp()
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_TRIGGER_VALUE -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置报警阈值出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting())
                            processNavigateUp()
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_REPORT_INTERVAL -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置报警间隔出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
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

    /**
     * 初始化报警监测点数据
     */
    private fun initAlarmMonitorPointData(info: AlarmMonitorPointInfo) {
        try {
            mStates.monitorPoint.set(info.monitorpoint)
            mStates.broadcastTimes.set(info.cnt)
            mStates.firstAlarmVoice.set(info.level1)
            mStates.secondAlarmVoice.set(info.level2)
            mStates.thirdAlarmVoice.set(info.level3)
            mStates.fourthAlarmVoice.set(info.level4)

            // 添加这行来保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 初始化报警阈值数据（根据产品类型使用不同字段）
     */
    private fun initAlarmTriggerValueData(info: AlarmTriggerValueInfo) {
        try {
            if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
                mStates.firstAlarmThreshold.set(info.devlevel1.formatDoubleValue("", 3))
                mStates.secondAlarmThreshold.set(info.devlevel2.formatDoubleValue("", 3))
                mStates.thirdAlarmThreshold.set(info.devlevel3.formatDoubleValue("", 3))
                mStates.fourthAlarmThreshold.set(info.devlevel4.formatDoubleValue("", 3))
            } else {
                mStates.firstAlarmThreshold.set(info.level1.formatDoubleValue("", 3))
                mStates.secondAlarmThreshold.set(info.level2.formatDoubleValue("", 3))
                mStates.thirdAlarmThreshold.set(info.level3.formatDoubleValue("", 3))
                mStates.fourthAlarmThreshold.set(info.level4.formatDoubleValue("", 3))
            }

            // 添加这行来保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 初始化报警上报间隔数据
     */
    private fun initAlarmReportIntervalData(info: AlarmReportIntervalInfo) {
        try {
            mStates.firstAlarmReportInterval.set(info.level1)
            mStates.secondAlarmReportInterval.set(info.level2)
            mStates.thirdAlarmReportInterval.set(info.level3)
            mStates.fourthAlarmReportInterval.set(info.level4)

            // 添加这行来保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 点击事件处理
     */
    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择报警编号
         */
        fun onMonitorPointChooseClick() {
            val selectedIndex = monitorPointList.indexOf(mStates.monitorPoint.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", monitorPointList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.monitorPoint.set(text)
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
            saveConfiguration()
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