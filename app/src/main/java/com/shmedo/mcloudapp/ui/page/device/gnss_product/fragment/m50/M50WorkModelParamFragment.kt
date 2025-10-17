package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.RtkParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.RadioCommunicateInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.RtkParamInfo
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
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentM50WorkModelParamBinding
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50WorkModelParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/7/29
 * @desc: M50工作模式参数设置页面（优化版）
 *
 * 优化特点：
 * 1. 使用新的通信架构，代码更简洁
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 保持原有的复杂业务逻辑不变
 * 5. 更清晰的代码结构和职责分离
 */
class M50WorkModelParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50WorkModelParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50WorkModelParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    // 配置选项列表
    private val reportModelList = arrayListOf("常在线", "低功耗", "自适应")
    private val networkModelList = arrayListOf("4G传输", "电台传输", "自动")
    private val siteTypeList = arrayListOf("基站", "测站")  // 站点类型
    private val frontendCalculationList = arrayListOf("关", "开")
    private val diffSourceList = arrayListOf("MQTT", "NTRIP", "LORA")
    private val diffSourceCodeMap = mapOf(
        "MQTT" to "2",
        "NTRIP" to "0",
        "LORA" to "1"
    )
    private val codeToDiffSourceMap = diffSourceCodeMap.entries.associate { it.value to it.key }
    private val coordinateInitializationList = arrayListOf("是", "否")
    private val initializationModeList = arrayListOf("手动", "自动")
    private val initializationTimeList =
        arrayListOf("15min", "30min", "60min", "120min", "360min", "12小时", "24小时")
    private val calculationIntervalTimeList = arrayListOf(
        "5min",
        "10min",
        "15min",
        "30min",
        "60min",
        "120min",
        "360min",
        "12h",
        "24h"
    )

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m50_work_model_param, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM50WorkModelParamBinding
        binding.llToolbar.toolbar.title = "工作模式"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher { handleBackByCheckDataModified() }
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
        mStates.reportModel.set(reportModelList[0]) // 默认常在线
        mStates.networkModel.set(networkModelList[2]) // 默认自动
        mStates.siteType.set(siteTypeList[1]) // 默认测站
        mStates.frontendCalculation.set(frontendCalculationList[0]) // 默认关
        mStates.diffSource.set(diffSourceList[0]) // 默认MQTT
        mStates.coordinateInitialization.set(coordinateInitializationList[1]) // 默认否
        mStates.initializationMode.set(initializationModeList[1]) // 默认自动
        mStates.longitude.set("") // 经度
        mStates.latitude.set("") // 纬度
        mStates.altitude.set("") // 高度
        mStates.initializationTime.set(initializationTimeList[0]) // 默认15min
        mStates.calculationIntervalTime.set(calculationIntervalTimeList[0]) // 默认5min
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询数据
     */
    private fun queryData() {
        val commands = listOf(
            // 查询电台参数
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_RADIO_CTRL),
            // 查询RTK配置
            IOTCommandUtil.getCommand(IOTCommandType.GM_MD_CFG_RTK, "method=0")
        )

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            )
        )
    }

    /**
     * 保存配置
     */
    private fun saveConfiguration() {
        // 验证输入数据
        if (!validateInputData()) {
            return
        }

        // 构建保存指令序列
        val commands = buildSaveCommands()

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 验证输入数据
     */
    private fun validateInputData(): Boolean {
        // 检查电台未启用时网络模式是否为电台传输
        if (!mStates.isRadioEnable.get() && mStates.networkModel.get() == "电台传输") {
            showMessageDialog("电台未启用，网络模式不能选择电台传输!")
            return false
        }

        // 验证坐标初始化相关配置（仅当前端解算为"开"时）
        if (mStates.frontendCalculation.get() == "开" && mStates.coordinateInitialization.get() == "是") {
            if (mStates.siteType.get() == "基站" && mStates.initializationMode.get() == "手动") {
                // 验证手动模式下的坐标输入
                if (mStates.longitude.get().isEmpty()) {
                    showMessageDialog("请输入经度!")
                    return false
                }
                if (mStates.latitude.get().isEmpty()) {
                    showMessageDialog("请输入纬度!")
                    return false
                }
                if (mStates.altitude.get().isEmpty()) {
                    showMessageDialog("请输入高度!")
                    return false
                }

                // 验证输入格式
                try {
                    mStates.longitude.get().toDouble()
                    mStates.latitude.get().toDouble()
                    mStates.altitude.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的坐标数值!")
                    return false
                }
            }
        }

        return true
    }

    /**
     * 构建保存指令列表
     */
    private fun buildSaveCommands(): List<String> {
        val commands = mutableListOf<String>()
        val diffDataCode = getDiffDataCode()

        if (mStates.frontendCalculation.get() == "开" && mStates.coordinateInitialization.get() == "是") {
            // 前端解算开启且坐标初始化为"是"时的完整配置
            if (mStates.siteType.get() == "基站") {
                // 构建基站模式下的保存命令
                val entity = RtkParamEntity(
                    reportMode = reportModelList.indexOf(mStates.reportModel.get()).toString(),
                    networkMode = networkModelList.indexOf(mStates.networkModel.get()).toString(),
                    mode = (siteTypeList.indexOf(mStates.siteType.get()) + 1).toString(),
                    frontCalc = "1",
                    latitude = if (mStates.initializationMode.get() == "手动") mStates.latitude.get() else "0",
                    longitude = if (mStates.initializationMode.get() == "手动") mStates.longitude.get() else "0",
                    height = if (mStates.initializationMode.get() == "手动") mStates.altitude.get() else "0",
                    diffdata = diffDataCode
                )
                val command = IOTCommandUtil.getCommand(
                    IOTCommandType.GM_MD_CFG_RTK,
                    entity.toCommandString()
                )
                commands.add(command)
            }

            if (mStates.siteType.get() == "测站") {
                // 构建测站模式下的保存命令
                val entity = RtkParamEntity(
                    reportMode = reportModelList.indexOf(mStates.reportModel.get()).toString(),
                    networkMode = networkModelList.indexOf(mStates.networkModel.get()).toString(),
                    mode = (siteTypeList.indexOf(mStates.siteType.get()) + 1).toString(),
                    frontCalc = "1",
                    basearc = (initializationTimeList.indexOf(mStates.initializationTime.get()) + 1).toString(),
                    arc = (calculationIntervalTimeList.indexOf(mStates.calculationIntervalTime.get()) + 1).toString(),
                    diffdata = diffDataCode
                )
                var command = IOTCommandUtil.getCommand(
                    IOTCommandType.GM_MD_CFG_RTK,
                    entity.toCommandString()
                )
                commands.add(command)

                // 添加数据存储格式设置命令
                command = IOTCommandUtil.getCommand(IOTCommandType.MD_FORMAT_DATA_STORAGE, "type=4")
                commands.add(command)
            }
        } else {
            // 前端解算为"关"或坐标初始化为"否"时的基础配置
            val entity = RtkParamEntity(
                reportMode = reportModelList.indexOf(mStates.reportModel.get()).toString(),
                networkMode = networkModelList.indexOf(mStates.networkModel.get()).toString(),
                mode = (siteTypeList.indexOf(mStates.siteType.get()) + 1).toString(),
                frontCalc = frontendCalculationList.indexOf(mStates.frontendCalculation.get()).toString(),
                diffdata = diffDataCode
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.GM_MD_CFG_RTK,
                entity.toCommandString()
            )
            commands.add(command)
        }

        return commands
    }

    /** 获取差分源对应的命令参数值 */
    private fun getDiffDataCode(): String {
        if (mStates.frontendCalculation.get() != "开" || mStates.siteType.get() != "测站") {
            return IOTConstants.NULL_KEY
        }
        return diffSourceCodeMap[mStates.diffSource.get()] ?: IOTConstants.NULL_KEY
    }

    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_RADIO_CTRL -> {
                handleRadioControlQuery(cmdStr)
            }

            IOTCommandType.GM_MD_CFG_RTK -> {
                handleRtkConfigResponse(cmdStr)
            }

            IOTCommandType.MD_FORMAT_DATA_STORAGE -> {
                handleDataStorageSave(cmdStr)
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    /**
     * 处理电台控制查询响应
     */
    private fun handleRadioControlQuery(cmdStr: String) {
        val result = iotParseManager.parse<RadioCommunicateInfo>(
            cmdStr,
            IOTCommandType.MD_GET_RADIO_CTRL
        )
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "查询电台参数出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                initRadioData(result.data)
            }
        }
    }

    /**
     * 处理RTK配置响应
     */
    private fun handleRtkConfigResponse(cmdStr: String) {
        val result = if (cmdStr.contains("method=0"))
            iotParseManager.parse<RtkParamInfo>(cmdStr, IOTCommandType.GM_MD_CFG_RTK)
        else
            iotParseManager.parse<CommonSettingCmdResult>(cmdStr)

        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = if (cmdStr.contains("method=0"))
                    "查询信息出错: ${result.message}"
                else
                    "数据保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                if (cmdStr.contains("method=0")) {
                    initParamData(result.data as RtkParamInfo)
                } else {
                    // 保存成功，检查是否还有指令需要执行
                    if (!isCommunicationExecuting()) {
                        processNavigateUp()
                    }
                }
            }
        }
    }

    /**
     * 处理数据存储格式保存响应
     */
    private fun handleDataStorageSave(cmdStr: String) {
        when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
            is IOTCommandResult.Failure -> {
                val errMsg = "数据保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                if (!isCommunicationExecuting()) {
                    processNavigateUp()
                }
            }
        }
    }

    /**
     * 初始化电台数据
     */
    private fun initRadioData(data: RadioCommunicateInfo) {
        mStates.isRadioEnable.set(data.sw == "1")

        // 当电台未启用时，如果当前网络模式为"电台传输"，则自动切换为"4G传输"
        if (data.sw != "1" && mStates.networkModel.get() == "电台传输") {
            mStates.networkModel.set("4G传输")
        }

        // 保存初始状态
        mStates.saveInitialState()
    }

    /**
     * 初始化参数数据
     */
    private fun initParamData(info: RtkParamInfo) {
        try {
            info.reportMode.toIntOrNull()?.let {
                if (it in reportModelList.indices) {
                    mStates.reportModel.set(reportModelList[it])
                }
            }
            info.networkMode.toIntOrNull()?.let {
                if (it in networkModelList.indices) {
                    // 如果电台未启用且网络模式为电台传输，则强制设置为4G传输
                    if (!mStates.isRadioEnable.get() && networkModelList[it] == "电台传输") {
                        mStates.networkModel.set("4G传输")
                    } else {
                        mStates.networkModel.set(networkModelList[it])
                    }
                }
            }
            info.mode.toIntOrNull()?.let {
                if (it in 1..siteTypeList.size) {
                    mStates.siteType.set(siteTypeList[it - 1])
                }
            }
            info.frontCalc.toIntOrNull()?.let {
                if (it in frontendCalculationList.indices) {
                    mStates.frontendCalculation.set(frontendCalculationList[it])
                }
            }
            info.baseStationMode.toIntOrNull()?.let {
                if (it in initializationModeList.indices) {
                    mStates.initializationMode.set(initializationModeList[it])
                }
            }
            mStates.longitude.set(info.longitude)
            mStates.latitude.set(info.latitude)
            mStates.altitude.set(info.height.formatDoubleValue("", 3))

            info.basearc.toIntOrNull()?.let {
                if (it in 1..initializationTimeList.size) {
                    mStates.initializationTime.set(initializationTimeList[it - 1])
                }
            }
            info.arc.toIntOrNull()?.let {
                if (it in 1..calculationIntervalTimeList.size) {
                    mStates.calculationIntervalTime.set(calculationIntervalTimeList[it - 1])
                }
            }
            info.diffdata.takeIf { it.isNotEmpty() }?.let { code ->
                codeToDiffSourceMap[code]?.let { diff ->
                    mStates.diffSource.set(diff)
                }
            }

            // 保存初始状态
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

        /** 选择上报模式 */
        fun onReportModelChooseClick() {
            val selectedIndex = reportModelList.indexOf(mStates.reportModel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", reportModelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.reportModel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择网络模式 */
        fun onNetworkModelChooseClick() {
            val availableNetworkModelList = arrayListOf<String>()
            availableNetworkModelList.addAll(networkModelList)

            // 当电台未启用时，不能选择电台传输
            if (!mStates.isRadioEnable.get()) {
                // 如果当前已经选择了电台传输，自动切换为4G传输
                if (mStates.networkModel.get() == "电台传输") {
                    mStates.networkModel.set("4G传输")
                    showMessageDialog("电台未启用，网络模式已自动切换为4G传输")
                    return
                }
                // 显示仅包含4G传输和自动的网络模式选项
                availableNetworkModelList.remove("电台传输")
            }

            val selectedIndex = availableNetworkModelList.indexOf(mStates.networkModel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", availableNetworkModelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.networkModel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择工作模式 */
        fun onWorkModelChooseClick() {
            val selectedIndex = siteTypeList.indexOf(mStates.siteType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", siteTypeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.siteType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择前端解算 */
        fun onFrontendCalculationChooseClick() {
            val selectedIndex = frontendCalculationList.indexOf(mStates.frontendCalculation.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", frontendCalculationList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.frontendCalculation.set(text)
                        // 当前端解算选择"关"时，自动将坐标初始化设为"否"
                        if (text == "关") {
                            mStates.coordinateInitialization.set("否")
                        }
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择差分源 */
        fun onDiffSourceChooseClick() {
            val selectedIndex = diffSourceList.indexOf(mStates.diffSource.get()).takeIf { it >= 0 } ?: 0
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", diffSourceList.toTypedArray(),
                    null, selectedIndex,
                    { _, text ->
                        mStates.diffSource.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择坐标初始化 */
        fun onCoordinateInitializationChooseClick() {
            val selectedIndex =
                coordinateInitializationList.indexOf(mStates.coordinateInitialization.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", coordinateInitializationList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.coordinateInitialization.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择初始化模式 */
        fun onInitializationModeChooseClick() {
            val selectedIndex = initializationModeList.indexOf(mStates.initializationMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", initializationModeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.initializationMode.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择初始化时间 */
        fun onInitializationTimeChooseClick() {
            val selectedIndex = initializationTimeList.indexOf(mStates.initializationTime.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", initializationTimeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.initializationTime.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择解算间隔时间 */
        fun onCalculationIntervalTimeChooseClick() {
            val selectedIndex =
                calculationIntervalTimeList.indexOf(mStates.calculationIntervalTime.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", calculationIntervalTimeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.calculationIntervalTime.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 恢复默认配置 */
        fun onResetClick() {
            resetDefaultParams()
        }

        /** 提交保存 */
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
