package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.gt600

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600.DualAntennaConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600.ElevationMaskConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600.GNSSCtlConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600.NMEATimeConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.DualAntennaData
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.ElevationMaskData
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.GNSSCtlData
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.NMEATimeData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentGt600GnssConfigBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.GT600GNSSConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2026/1/8
 * @desc: GT600 GNSS 配置页面
 *
 * 页面包含四个配置分组：
 * 1. 卫星信息 - 截至高度角（暂不支持，UI保留但禁用）
 * 2. 双天线参数 - 功能开关、上报频率、天线间距
 * 3. RTCM参数 - 观测值(OBS)频率、星历值(EHP)频率
 * 4. NMEA参数 - GPGGA/GPRMC/GPVGT/GPGSV/GPGSA 五项频率配置
 *
 * 涉及指令：
 * - 双天线参数: md_cfgnmeavtgout (method=0查询, method=1设置)
 * - RTCM参数: md_getgnssctl / md_setgnssctl
 * - NMEA参数: md_getnmeatime / md_setnmeatime
 */
class GT600GNSSConfigFragment : OptimizedBaseIOTDeviceFragment() {

    private lateinit var binding: FragmentGt600GnssConfigBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: GT600GNSSConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    // ========== 选项列表定义 ==========

    /** 双天线上报频率选项（显示文本） */
    private val dualAntennaReportFreqList = arrayListOf("1s", "5s", "10s", "15s", "30s", "60s")

    /** 双天线上报频率选项（实际值） */
    private val dualAntennaReportFreqValueList = arrayListOf("1", "5", "10", "15", "30", "60")

    /** RTCM 观测值频率选项（显示文本） */
    private val rtcmObsTimeList = arrayListOf("1s", "5s", "10s", "15s", "30s")

    /** RTCM 观测值频率选项（实际值） */
    private val rtcmObsTimeValueList = arrayListOf("1", "5", "10", "15", "30")

    /** RTCM 星历值频率选项（显示文本） */
    private val rtcmEphTimeList = arrayListOf("1s", "5s", "10s", "15s", "30s", "60s")

    /** RTCM 星历值频率选项（实际值） */
    private val rtcmEphTimeValueList = arrayListOf("1", "5", "10", "15", "30", "60")

    /** NMEA 频率选项（显示文本） */
    private val nmeaFreqList =
        arrayListOf("20Hz", "10Hz", "5Hz", "1Hz", "5s", "10s", "15s", "30s", "60s", "关闭")

    /** NMEA 频率选项（实际值） */
    private val nmeaFreqValueList =
        arrayListOf("0.05", "0.1", "0.2", "1", "5", "10", "15", "30", "60", "0")

    /** 截至高度角选项（显示文本） */
    private val elevationAngleList = arrayListOf(
        "5°", "10°", "15°", "20°", "25°", "30°", "35°", "40°", "45°",
        "50°", "55°", "60°", "65°", "70°", "75°", "80°", "85°", "90°"
    )

    /** 截至高度角选项（实际值） */
    private val elevationAngleValueList = arrayListOf(
        "5", "10", "15", "20", "25", "30", "35", "40", "45",
        "50", "55", "60", "65", "70", "75", "80", "85", "90"
    )

    // ========== 生命周期方法 ==========

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_gt600_gnss_config, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentGt600GnssConfigBinding
        binding.llToolbar.toolbar.title = "GNSS配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher { handleBackByCheckDataModified() }
        initRefresh()
    }

    /**
     * 初始化下拉刷新
     */
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

    /**
     * 重置为默认参数
     */
    private fun resetDefaultParams() {
        // 双天线参数默认值
        mStates.dualAntennaSwitch.set(false)
        mStates.dualAntennaReportFreq.set(dualAntennaReportFreqList[5]) // 默认60s
        mStates.antennaDistance.set("50")

        // RTCM 参数默认值
        mStates.rtcmObsTime.set(rtcmObsTimeList[3]) // 默认15s
        mStates.rtcmEphTime.set(rtcmEphTimeList[5]) // 默认60s

        // NMEA 参数默认值
        mStates.gpggaFreq.set(nmeaFreqList[3]) // 默认1Hz
        mStates.gprmcFreq.set(nmeaFreqList[3]) // 默认1Hz
        mStates.gpvgtFreq.set(nmeaFreqList[3]) // 默认1Hz
        mStates.gpgsvFreq.set(nmeaFreqList[3]) // 默认1Hz
        mStates.gpgsaFreq.set(nmeaFreqList[3]) // 默认1Hz
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    // ========== 数据查询 ==========

    /**
     * 查询所有配置数据
     * 发送三个查询指令：双天线参数、RTCM参数、NMEA参数
     */
    private fun queryData() {
        val commands = listOf(
            // 1. 查询截至高度角参数
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_ELEVATION_MASK, "method=0"
            ),
            // 2. 查询双天线参数
            IOTCommandUtil.getCommand(IOTCommandType.MD_CFG_NMEA_VTG_OUT, "method=0"),
            // 3. 查询 RTCM 参数
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_GNSS_CTL),
            // 4. 查询 NMEA 参数
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_NMEA_TIME)
        )

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载对话框
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            )
        )
    }

    // ========== 数据保存 ==========

    /**
     * 保存配置
     * 按顺序发送三个设置指令
     */
    private fun saveConfiguration() {
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
     * 构建保存指令列表
     */
    private fun buildSaveCommands(): List<String> {
        val commands = mutableListOf<String>()

        // 1. 截至高度角设置指令
        val angleValue = getValueFromDisplayList(
            mStates.elevationAngle.get(),
            elevationAngleList,
            elevationAngleValueList,
            "15"
        )
        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_ELEVATION_MASK,
                ElevationMaskConfigEntity.createSetEntity(angleValue).toCommandString()
            )
        )

        // 1. 双天线参数设置指令
        val dualAntennaEntity = DualAntennaConfigEntity(
            method = "1", // 设置模式
            switch = if (mStates.dualAntennaSwitch.get()) "1" else "0",
            antdist = mStates.antennaDistance.get(),
            report_freq = getValueFromDisplayList(
                mStates.dualAntennaReportFreq.get(),
                dualAntennaReportFreqList,
                dualAntennaReportFreqValueList,
                "60"
            )
        )
        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_CFG_NMEA_VTG_OUT,
                dualAntennaEntity.toCommandString()
            )
        )

        // 2. RTCM 参数设置指令
        val gnssCtlEntity = GNSSCtlConfigEntity(
            encrypttype = "0",
            rtcmobstime = getValueFromDisplayList(
                mStates.rtcmObsTime.get(),
                rtcmObsTimeList,
                rtcmObsTimeValueList,
                "5"
            ),
            rtcmephtime = getValueFromDisplayList(
                mStates.rtcmEphTime.get(),
                rtcmEphTimeList,
                rtcmEphTimeValueList,
                "60"
            ),
            onlybd = "0",
        )
        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_GNSS_CTL,
                gnssCtlEntity.toCommandString()
            )
        )

        // 3. NMEA 参数设置指令
        val nmeaEntity = NMEATimeConfigEntity(
            gga = getValueFromDisplayList(
                mStates.gpggaFreq.get(),
                nmeaFreqList,
                nmeaFreqValueList,
                "1"
            ),
            rmc = getValueFromDisplayList(
                mStates.gprmcFreq.get(),
                nmeaFreqList,
                nmeaFreqValueList,
                "1"
            ),
            vtg = getValueFromDisplayList(
                mStates.gpvgtFreq.get(),
                nmeaFreqList,
                nmeaFreqValueList,
                "1"
            ),
            gsv = getValueFromDisplayList(
                mStates.gpgsvFreq.get(),
                nmeaFreqList,
                nmeaFreqValueList,
                "1"
            ),
            gsa = getValueFromDisplayList(
                mStates.gpgsaFreq.get(),
                nmeaFreqList,
                nmeaFreqValueList,
                "1"
            )
        )
        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_NMEA_TIME,
                nmeaEntity.toCommandString()
            )
        )

        return commands
    }

    /**
     * 从显示列表中获取对应的实际值
     */
    private fun getValueFromDisplayList(
        displayValue: String,
        displayList: List<String>,
        valueList: List<String>,
        defaultValue: String
    ): String {
        val index = displayList.indexOf(displayValue)
        return if (index in valueList.indices) valueList[index] else defaultValue
    }

    // ========== 响应处理 ==========

    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_ELEVATION_MASK -> {
                handleElevationMaskResponse(cmdStr)
            }

            IOTCommandType.MD_CFG_NMEA_VTG_OUT -> {
                handleDualAntennaResponse(cmdStr)
            }

            IOTCommandType.MD_GET_GNSS_CTL -> {
                handleGNSSCtlQueryResponse(cmdStr)
            }

            IOTCommandType.MD_SET_GNSS_CTL -> {
                handleGNSSCtlSaveResponse(cmdStr)
            }

            IOTCommandType.MD_GET_NMEA_TIME -> {
                handleNMEATimeQueryResponse(cmdStr)
            }

            IOTCommandType.MD_SET_NMEA_TIME -> {
                handleNMEATimeSaveResponse(cmdStr)
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    /**
     * 处理双天线参数响应（查询和设置共用同一个指令）
     */
    private fun handleDualAntennaResponse(cmdStr: String) {
        val result = if (cmdStr.contains("method=0"))
            iotParseManager.parse<DualAntennaData>(cmdStr, IOTCommandType.MD_CFG_NMEA_VTG_OUT)
        else
            iotParseManager.parse<CommonSettingCmdResult>(cmdStr)

        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = if (cmdStr.contains("method=0"))
                    "查询双天线参数出错: ${result.message}"
                else
                    "双天线参数保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                if (cmdStr.contains("method=0")) {
                    initDualAntennaData(result.data as DualAntennaData)
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
     * 处理 GNSS 控制参数查询响应
     */
    private fun handleGNSSCtlQueryResponse(cmdStr: String) {
        val result = iotParseManager.parse<GNSSCtlData>(cmdStr, IOTCommandType.MD_GET_GNSS_CTL)
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "查询RTCM参数出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                initGNSSCtlData(result.data)
            }
        }
    }

    /**
     * 处理 GNSS 控制参数保存响应
     */
    private fun handleGNSSCtlSaveResponse(cmdStr: String) {
        when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
            is IOTCommandResult.Failure -> {
                val errMsg = "RTCM参数保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                if (!isCommunicationExecuting()) processNavigateUp()
            }
        }
    }

    /**
     * 处理 NMEA 参数查询响应
     */
    private fun handleNMEATimeQueryResponse(cmdStr: String) {
        val result = iotParseManager.parse<NMEATimeData>(cmdStr, IOTCommandType.MD_GET_NMEA_TIME)
        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = "查询NMEA参数出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                initNMEATimeData(result.data)
            }
        }
    }

    /**
     * 处理 NMEA 参数保存响应
     */
    private fun handleNMEATimeSaveResponse(cmdStr: String) {
        when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
            is IOTCommandResult.Failure -> {
                val errMsg = "NMEA参数保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            else -> {
                if (!isCommunicationExecuting()) processNavigateUp()
            }
        }
    }

    /**
     * 处理截至高度角参数响应（查询和设置共用同一个指令）
     */
    private fun handleElevationMaskResponse(cmdStr: String) {
        val result = if (cmdStr.contains("method=0"))
            iotParseManager.parse<ElevationMaskData>(cmdStr, IOTCommandType.MD_ELEVATION_MASK)
        else
            iotParseManager.parse<CommonSettingCmdResult>(cmdStr)

        when (result) {
            is IOTCommandResult.Failure -> {
                val errMsg = if (cmdStr.contains("method=0"))
                    "查询截至高度角参数出错: ${result.message}"
                else
                    "截至高度角参数保存出错: ${result.message}"
                handleFailureResult(errMsg, isMessageDialog = true)
            }

            is IOTCommandResult.Success -> {
                if (cmdStr.contains("method=0")) {
                    initElevationMaskData(result.data as ElevationMaskData)
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
     * 初始化截至高度角数据
     */
    private fun initElevationMaskData(data: ElevationMaskData) {
        try {
            val angleIndex = elevationAngleValueList.indexOf(data.angle)
            if (angleIndex in elevationAngleList.indices) {
                mStates.elevationAngle.set(elevationAngleList[angleIndex])
            }
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e, "初始化截至高度角数据出错")
        }
    }

    // ========== 数据初始化 ==========

    /**
     * 初始化双天线参数数据
     */
    private fun initDualAntennaData(data: DualAntennaData) {
        try {
            // 功能开关
            mStates.dualAntennaSwitch.set(data.switch == "1")

            // 上报频率
            val reportFreqIndex = dualAntennaReportFreqValueList.indexOf(data.report_freq)
            if (reportFreqIndex in dualAntennaReportFreqList.indices) {
                mStates.dualAntennaReportFreq.set(dualAntennaReportFreqList[reportFreqIndex])
            }

            // 天线间距
            if (data.antdist.isNotEmpty()) {
                mStates.antennaDistance.set(data.antdist)
            }

            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e, "初始化双天线参数数据出错")
        }
    }

    /**
     * 初始化 GNSS 控制参数数据（RTCM参数）
     */
    private fun initGNSSCtlData(data: GNSSCtlData) {
        try {
            // 观测值频率
            val obsTimeIndex = rtcmObsTimeValueList.indexOf(data.rtcmobstime)
            if (obsTimeIndex in rtcmObsTimeList.indices) {
                mStates.rtcmObsTime.set(rtcmObsTimeList[obsTimeIndex])
            }

            // 星历值频率
            val ephTimeIndex = rtcmEphTimeValueList.indexOf(data.rtcmephtime)
            if (ephTimeIndex in rtcmEphTimeList.indices) {
                mStates.rtcmEphTime.set(rtcmEphTimeList[ephTimeIndex])
            }

            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e, "初始化RTCM参数数据出错")
        }
    }

    /**
     * 初始化 NMEA 参数数据
     */
    private fun initNMEATimeData(data: NMEATimeData) {
        try {
            // GPGGA
            setNMEAFreqFromValue(data.gga) { mStates.gpggaFreq.set(it) }
            // GPRMC
            setNMEAFreqFromValue(data.rmc) { mStates.gprmcFreq.set(it) }
            // GPVGT
            setNMEAFreqFromValue(data.vtg) { mStates.gpvgtFreq.set(it) }
            // GPGSV
            setNMEAFreqFromValue(data.gsv) { mStates.gpgsvFreq.set(it) }
            // GPGSA
            setNMEAFreqFromValue(data.gsa) { mStates.gpgsaFreq.set(it) }

            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e, "初始化NMEA参数数据出错")
        }
    }

    /**
     * 根据 NMEA 频率值设置显示文本
     */
    private fun setNMEAFreqFromValue(value: String, setter: (String) -> Unit) {
        val index = nmeaFreqValueList.indexOf(value)
        if (index in nmeaFreqList.indices) {
            setter(nmeaFreqList[index])
        }
    }

    // ========== 点击事件处理 ==========

    inner class ClickProxy : BaseClickProxy() {

        /** 选择截至高度角 */
        fun onElevationAngleSelected() {
            showBottomListPopup(
                elevationAngleList,
                mStates.elevationAngle.get()
            ) { text ->
                mStates.elevationAngle.set(text)
            }
        }

        /** 选择双天线上报频率 */
        fun onDualAntennaReportFreqSelected() {
            showBottomListPopup(
                dualAntennaReportFreqList,
                mStates.dualAntennaReportFreq.get()
            ) { text ->
                mStates.dualAntennaReportFreq.set(text)
            }
        }

        /** 选择 RTCM 观测值频率 */
        fun onRtcmObsTimeSelected() {
            showBottomListPopup(
                rtcmObsTimeList,
                mStates.rtcmObsTime.get()
            ) { text ->
                mStates.rtcmObsTime.set(text)
            }
        }

        /** 选择 RTCM 星历值频率 */
        fun onRtcmEphTimeSelected() {
            showBottomListPopup(
                rtcmEphTimeList,
                mStates.rtcmEphTime.get()
            ) { text ->
                mStates.rtcmEphTime.set(text)
            }
        }

        /** 选择 GPGGA 频率 */
        fun onGpggaFreqSelected() {
            showBottomListPopup(nmeaFreqList, mStates.gpggaFreq.get()) { text ->
                mStates.gpggaFreq.set(text)
            }
        }

        /** 选择 GPRMC 频率 */
        fun onGprmcFreqSelected() {
            showBottomListPopup(nmeaFreqList, mStates.gprmcFreq.get()) { text ->
                mStates.gprmcFreq.set(text)
            }
        }

        /** 选择 GPVGT 频率 */
        fun onGpvgtFreqSelected() {
            showBottomListPopup(nmeaFreqList, mStates.gpvgtFreq.get()) { text ->
                mStates.gpvgtFreq.set(text)
            }
        }

        /** 选择 GPGSV 频率 */
        fun onGpgsvFreqSelected() {
            showBottomListPopup(nmeaFreqList, mStates.gpgsvFreq.get()) { text ->
                mStates.gpgsvFreq.set(text)
            }
        }

        /** 选择 GPGSA 频率 */
        fun onGpgsaFreqSelected() {
            showBottomListPopup(nmeaFreqList, mStates.gpgsaFreq.get()) { text ->
                mStates.gpgsaFreq.set(text)
            }
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
            // 验证天线间距
            val distance = mStates.antennaDistance.get().toFloatOrNull()
            if (distance == null || distance <= 0) {
                Toaster.show("天线间距必须大于0")
                return
            }
            saveConfiguration()
        }
    }

    /**
     * 显示底部列表弹窗
     */
    private fun showBottomListPopup(
        list: ArrayList<String>,
        currentValue: String,
        onSelected: (String) -> Unit
    ) {
        val selectedIndex = list.indexOf(currentValue)
        XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
        XPopup.Builder(context)
            .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
            .isDestroyOnDismiss(true)
            .enableDrag(false)
            .asBottomList(
                "", list.toTypedArray(),
                null, selectedIndex,
                { _, text -> onSelected(text) },
                0, R.layout.custom_xpopup_adapter_text_center
            )
            .show()
    }

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }
}
