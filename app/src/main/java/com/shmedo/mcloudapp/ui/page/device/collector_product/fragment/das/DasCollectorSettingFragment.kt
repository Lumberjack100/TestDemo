package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasCollectorEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasCollectorInfo
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
import com.shmedo.mcloudapp.databinding.FragmentDasCollectorSettingBinding
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasCollectorSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/1/8
 * @desc: 物联网采集器(DAS)参数配置页面 - 支持4G和蓝牙两种通讯方式
 *
 */
class DasCollectorSettingFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasCollectorSettingBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: DasCollectorSettingViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val mdParseManager: MDParserManager by inject()

    private var collectorModel = "-1"


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_collector_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasCollectorSettingBinding
        binding.llToolbar.toolbar.title = "采集器配置"
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
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        // 蓝牙模式下获取采集器模型参数
        if (communicateWay == BleConnect) {
            arguments?.let {
                collectorModel = it.getString(COLLECTOR_MODEL, "-1")
            }
        }

        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.isShowSensitivity.set(false)
        mStates.collectorAddress.set("")
        mStates.solvingInterval.set("")
        mStates.standbyTime.set("")
        mStates.collectionInterval.set("")
        mStates.sensitivity.set("")
    }

    inner class ClickProxy : BaseClickProxy() {
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
        if (mStates.collectorAddress.get().isEmpty()) {
            showMessageDialog("请输入采集器地址!")
            return false
        }
        try {
            val value = mStates.collectorAddress.get().toDouble()
            if (value < 0 || value > 255) {
                showMessageDialog("采集器地址数值范围[0,255]!")
                return false
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的采集器地址!")
            return false
        }
        if (mStates.solvingInterval.get().isEmpty()) {
            showMessageDialog("请输入解算间隔!")
            return false
        }
        if (mStates.standbyTime.get().isEmpty()) {
            showMessageDialog("请输入待机时长!")
            return false
        }
        if (mStates.collectionInterval.get().isEmpty()) {
            showMessageDialog("请输入采集间隔!")
            return false
        }
        if (mStates.isShowSensitivity.get()) {
            if (mStates.sensitivity.get().isEmpty()) {
                showMessageDialog("请输入灵敏度!")
                return false
            }
            try {
                val value = mStates.sensitivity.get().toDouble()
                if (value < 30 || value > 150) {
                    showMessageDialog("灵敏度数值范围[30,150]!")
                    return false
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的灵敏度!")
                return false
            }
        }
        return true
    }

    /**
     * 4G通讯模式保存指令
     */
    private fun init4GSaveCommand() {
        val entity = DasCollectorEntity(
            type = mStates.type.get(),
            addr = mStates.collectorAddress.get(),
            collgap = mStates.collectionInterval.get(),
            calcgap = mStates.solvingInterval.get(),
            standbygap = mStates.standbyTime.get(),
            sensitivity = if (mStates.isShowSensitivity.get()) mStates.sensitivity.get() else IOTConstants.NULL_KEY
        )

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL,
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

    /**
     * 蓝牙通讯模式保存指令
     */
    private fun initBleSaveCommand() {
        val commands = mutableListOf<String>()

        var command = MDCommandUtil.getCommand(
            MDCommandType.SET_COLLECTOR_ADDRESS,
            mStates.collectorAddress.get()
        )
        commands.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SOLUTION_FREQUENCY,
            "$collectorModel${MDCommandUtil.formatStringFour(mStates.solvingInterval.get())}"
        )
        commands.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_STANDBY_TIME,
            "$collectorModel${MDCommandUtil.formatStringFour(mStates.standbyTime.get())}"
        )
        commands.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_FREQUENCY,
            "$collectorModel${MDCommandUtil.formatStringFive(mStates.collectionInterval.get())}"
        )
        commands.add(command)

        if (mStates.isShowSensitivity.get()) {
            command = MDCommandUtil.getCommand(
                MDCommandType.SET_COLLECTOR_SENSITIVITY,
                mStates.sensitivity.get()
            )
            commands.add(command)
        }

        command = MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
        )
        commands.add(command)

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
            queryBleCollectorInfo()
        } else {
            query4GCollectorInfo()
        }
    }

    /**
     * 蓝牙通讯模式查询采集器信息
     */
    private fun queryBleCollectorInfo() {
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_CONFIG, collectorModel
        )

        Timber.d("查询采集器配置信息===%s", command)
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    /**
     * 4G通讯模式查询采集器信息
     */
    private fun query4GCollectorInfo() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL
        )

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
            MDCommandType.COLLECTOR_CONFIG -> {
                val result = mdParseManager.parse<DasCollectorInfo>(
                    cmdStr,
                    MDCommandType.COLLECTOR_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询采集器参数出错：${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is MDCommandResult.Success -> {
                        initBleCollectorInfo(result.data)
                    }
                }
            }

            MDCommandType.SET_COLLECTOR_ADDRESS -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "采集器地址配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.COLLECTOR_SOLUTION_FREQUENCY -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "采集器解算间隔配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.COLLECTOR_STANDBY_TIME -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "采集器待机时长配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.COLLECTOR_FREQUENCY -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "采集器采集间隔配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.SET_COLLECTOR_SENSITIVITY -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "采集器灵敏度配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
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

    /**
     * 初始化蓝牙通讯模式下的参数数据
     */
    private fun initBleCollectorInfo(collectorInfo: DasCollectorInfo) {
        try {
            mStates.collectorAddress.set(collectorInfo.addr)
            mStates.solvingInterval.set(collectorInfo.calcgap)
            mStates.standbyTime.set(collectorInfo.standbygap)
            mStates.collectionInterval.set(collectorInfo.collgap)

            mStates.isShowSensitivity.set(collectorInfo.sensitivity != IOTConstants.NULL_KEY)
            collectorInfo.sensitivity.notNullKey {
                mStates.sensitivity.set(it.formatDoubleValue("", 1))
            }
            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 处理4G通讯指令结果
     */
    private fun handle4GCommandResult(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL -> {
                val result = iotParseManager.parse<DasCollectorInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        init4GParamData(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "配置参数出错: ${result.message}"
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

    /**
     * 初始化4G通讯模式下的参数数据
     */
    private fun init4GParamData(collectorInfo: DasCollectorInfo) {
        try {
            mStates.type.set(collectorInfo.type)
            mStates.collectorAddress.set(collectorInfo.addr)
            mStates.solvingInterval.set(collectorInfo.calcgap)
            mStates.standbyTime.set(collectorInfo.standbygap)
            mStates.collectionInterval.set(collectorInfo.collgap)

            mStates.isShowSensitivity.set(collectorInfo.sensitivity != IOTConstants.NULL_KEY)
            collectorInfo.sensitivity.notNullKey {
                mStates.sensitivity.set(it.formatDoubleValue("", 1))
            }
            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
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

    companion object {
        private const val COLLECTOR_MODEL = "collector_model"
        fun newBundleArguments(
            collectorModel: String,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putString(COLLECTOR_MODEL, collectorModel)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}