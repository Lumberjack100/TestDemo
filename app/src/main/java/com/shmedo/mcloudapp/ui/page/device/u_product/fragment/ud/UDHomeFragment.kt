package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUDHomeBinding
import com.shmedo.mcloudapp.databinding.ItemSubConfigModuleBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.model.AlarmConfigModule
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.LoraConfigModule
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.model.RebootModule
import com.shmedo.mcloudapp.model.RunningStatusModule
import com.shmedo.mcloudapp.model.SensorConfigModule
import com.shmedo.mcloudapp.model.TimeCalibrationModule
import com.shmedo.mcloudapp.model.WorkModeModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoParentFragment
import com.shmedo.mcloudapp.ui.page.device.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.ui.page.device.common.QueryDeviceDataFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.mr702.dialog.TimeCalibrationPopupView
import com.shmedo.mcloudapp.ui.viewmodel.state.CommandResponseViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDHomeViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import kotlinx.coroutines.flow.debounce
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/8/21
 * @desc: 一体化雷达水位计首页
 *
 */
class UDHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUDHomeBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mHeadStates: UDHomeViewModel by viewModels()
    private val mCommandResponseStates: CommandResponseViewModel by viewModels()

    private val iotParseManager: IOTParserManager by inject()

    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_u_d_home, BR.stateVM, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUDHomeBinding
        binding.llToolbar.toolbar.title = "设备配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            if (bleViewModel.isConnected()) {
                showMessage(
                    StringUtils.getString(R.string.disconnect_device_warn),
                    "温馨提示",
                    "确定",
                    {
                        bleViewModel.disconnect()
                        mActivity.finish()
                    },
                    "取消"
                )
            } else
                mActivity.finish()
        }
        registerOnBackPressedDispatcher {
            if (bleViewModel.isConnected()) {
                showMessage(
                    StringUtils.getString(R.string.disconnect_device_warn),
                    "温馨提示",
                    "确定",
                    {
                        bleViewModel.disconnect()
                        mActivity.finish()
                    },
                    "取消"
                )
            } else
                mActivity.finish()
        }
        initModuleAdapter()
    }

    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionVisible.set(true)
        mHeadStates.productLightResId.set(R.drawable.device_logo_niweiji)
        mHeadStates.productGrayResId.set(R.drawable.device_logo_niweiji_gray)
        mHeadStates.productLogoResId.set(mHeadStates.productLightResId.get())

        mHeadStates.productName.set(deviceInfo.productName)
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)
        mHeadStates.deviceName.set(if (deviceInfo.deviceName == deviceInfo.deviceToken) deviceInfo.productToken else deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken })
        mHeadStates.firmwareVersion.set(deviceInfo.firmwareVersion.ifEmpty { "--" })
        mHeadStates.isRunningStateVisible.set(false)
        mHeadStates.isPlatformsVisible.set(false)

        when (communicateWay) {
            NetPlatformConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(deviceInfo.onlineStatus)
                mHeadStates.deviceStateTagText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
                mHeadStates.isConnectOperateVisible.set(false)
                mHeadStates.isIOTPlatformStateVisible.set(false)
            }

            BleConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(false)
                mHeadStates.deviceStateTagText.set("未连接")
                mHeadStates.isConnectOperateVisible.set(true)
                mHeadStates.connectOperateText.set("蓝牙连接")
                mHeadStates.isIOTPlatformStateVisible.set(true)
                mHeadStates.iotPlatformStateText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
            }

            else -> {}
        }
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        mHeadStates.isConnected.set(isConnected)
        mHeadStates.isDeviceStateTagHighLight.set(isConnected)
        if (isConnected) {
            mHeadStates.deviceStateTagText.set("已连接")
            mHeadStates.connectOperateText.set("断开连接")
            mHeadStates.productLogoResId.set(mHeadStates.productLightResId.get())
        } else {
            mHeadStates.deviceStateTagText.set("未连接")
            mHeadStates.connectOperateText.set("蓝牙连接")
            mHeadStates.productLogoResId.set(mHeadStates.productGrayResId.get())
        }
        //刷新模块状态
        binding.rvModule.models?.forEach {
            if (it is ConfigModule) {
                it.functionModule.refreshStatus(isConnected)
            }
        }
    }

    private fun initModuleAdapter() {
        binding.rvModule.linear().setup { rv ->
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group)
            addType<ConfigModuleTree>(R.layout.item_sub_config_module)
            onCreate {
                when (itemViewType) {
                    R.layout.item_sub_config_module -> {
                        val itemBinding = getBinding<ItemSubConfigModuleBinding>()
                        itemBinding.rvSubModule.setup { subRv ->
                            subRv.addItemDecoration(
                                MyGridSpacingItemDecoration(
                                    2,
                                    ConvertUtils.dp2px(10f), false
                                )
                            )
                            addType<ConfigModule>(R.layout.item_device_config_module)
                            R.id.item.onClick {
                                val configModule = getModel<ConfigModule>()
                                processSubModuleItemClick(configModule.functionModule)
                            }
                        }
                    }

                    else -> {}
                }
            }
            onBind {
                when (itemViewType) {
                    R.layout.item_sub_config_module -> {
                        val configModuleTree = getModel<ConfigModuleTree>()
                        val itemBinding = getBinding<ItemSubConfigModuleBinding>()
                        itemBinding.rvSubModule.models = configModuleTree.configModules
                    }

                    else -> {

                    }
                }
            }

        }.models = getModuleList()
    }

    private fun getModuleList(): MutableList<Any> {
        val groupList = mutableListOf<Any>()
        groupList.add(
            DeviceStatusInfoGroupItem(
                "设备信息",
                iconResId = R.drawable.ic_mr702_device_info_running_data,
                hover = false
            )
        )
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    ConfigModule(
                        RunningStatusModule(
                            name = "基本信息",
                            desc = "查看设备基本信息",
                            resID = R.drawable.ic_module_current_state,
                            navId = R.id.action_global_to_commonRunningDeviceInfoFragment
                        )
                    ),
                    ConfigModule(
                        RunningStatusModule(
                            name = "网络信息",
                            desc = "查看设备网络信息",
                            resID = R.drawable.ic_module_network_info,
                            navId = R.id.action_global_to_commonRunningDeviceInfoFragment
                        )
                    ),
                    ConfigModule(
                        RunningStatusModule(
                            name = "状态信息",
                            desc = "查看设备运行状态信息",
                            resID = R.drawable.ic_basic_config,
                            navId = R.id.action_global_to_commonRunningDeviceInfoFragment
                        )
                    ),
                    ConfigModule(
                        RunningStatusModule(
                            name = "位置信息",
                            desc = "查看设备位置信息",
                            resID = R.drawable.ic_module_location_info,
                            navId = R.id.action_global_to_commonRunningDeviceInfoFragment
                        )
                    )
                )
            )
        )
        groupList.add(
            DeviceStatusInfoGroupItem(
                "设备配置",
                iconResId = R.drawable.ic_mr702_interface_info_sensor_config
            )
        )
        val configModuleTree = ConfigModuleTree(
            configModules = arrayListOf(
                ConfigModule(
                    WorkModeModule(
                        name = "工作模式",
                        desc = "报警上报参数配置",
                        navId = R.id.action_global_to_udWorkModelParamFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "网络配置",
                        desc = "移动网络参数配置",
                        resID = R.drawable.ic_module_network_setting,
                        navId = R.id.action_global_to_udMobileNetworkParamFragment
                    )
                ),
                ConfigModule(
                    DataCenterModule(
                        navId = R.id.action_global_to_udProductDataCenterHomeFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "CORS测高",
                        desc = "CORS参数配置",
                        resID = R.drawable.ic_module_satellite_communications,
                        navId = R.id.action_global_to_udCORSParamFragment
                    )
                ),
                ConfigModule(
                    SensorConfigModule(
                        name = "传感配置",
                        navId = R.id.action_global_to_udProductSensorParamFragment
                    )
                ),
                ConfigModule(
                    LoraConfigModule(
                        name = "电台配置",
                        navId = R.id.action_global_to_udRadioParamFragment
                    )
                ),
                ConfigModule(
                    AlarmConfigModule(
                        navId = R.id.action_global_to_alarmSettingFragment
                    )
                ),
                ConfigModule(
                    TimeCalibrationModule(
                        name = "时间校准",
                    )
                ),
                ConfigModule(RebootModule()),
                ConfigModule(
                    AdvancedSettingsModule(
                        navId = R.id.action_global_to_advancedSettingFragment
                    )
                )
            )
        )
        if (communicateWay is BleConnect) {
            configModuleTree.configModules.add(ConfigModule(CommandDebugConfigModule(desc = "")))
        }
        groupList.add(configModuleTree)


        return groupList
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            val bundle = QueryDeviceDataFragment.newBundleArguments(
                deviceInfo.deviceToken
            )
            nav(binding.llToolbar.ivAction).navigate(
                R.id.action_global_to_queryDeviceDataFragment, bundle
            )
        }

        override fun onConnectOperateClick() {
            if (bleViewModel.isConnected()) {
                showMessage(
                    StringUtils.getString(R.string.disconnect_device_warn),
                    "温馨提示",
                    "确定",
                    {
                        bleViewModel.disconnect()
                    },
                    "取消"
                )
            } else {
                bleViewModel.launch(bleDevice!!)
            }
        }
    }

    private fun processSubModuleItemClick(module: DeviceFunctionModule) {
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (module) {
            is RunningStatusModule -> {
                val index = when (module.name) {
                    "基本信息" -> 0
                    "网络信息" -> 1
                    "状态信息" -> 2
                    "位置信息" -> 3
                    else -> 0
                }
                nav().navigate(
                    module.navId,
                    BaseDeviceStatusInfoParentFragment.newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice,
                        tabIndex = index
                    )
                )
            }

            is TimeCalibrationModule -> {//时间校准
                queryTerminalTime()
            }

            is DataCenterModule -> {
                nav().navigate(
                    module.navId,
                    UniversalDataCenterHomeFragment.newBundleArguments(
                        centerNum = 4,
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                )
            }

            is CommandDebugConfigModule -> {//指令调试
                val bundle = BleCustomCommandLogPrintFragment.newBundleArguments(
                    true,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(module.navId, bundle)
            }

            else -> {
                if (module.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().navigate(
                        module.navId,
                        bundle
                    )
                }
            }
        }
    }

    /**
     * 查询终端时间
     */
    private fun queryTerminalTime() {
        commandItems.clear()
        val command =
            IOTCommandUtil.getCommand(IOTCommandType.QUERY_TERMINAL_TIME)
        commandItems.add(command)

        if (communicateWay is BleConnect) {
            mCommandResponseStates.isResponseLoading.set(true)
            showTimeCalibrationPopup()
        }
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 显示时间校准弹窗
     */
    private fun showTimeCalibrationPopup() {
        val popupView = TimeCalibrationPopupView(requireContext())
        popupView.setTitle("时间校准", mCommandResponseStates)
            .setClickListener(object : TimeCalibrationPopupView.OnClickListener {
                override fun onSettingClick() {
                    commandItems.clear()
                    val command = IOTCommandUtil.getCommand(
                        IOTCommandType.SET_TERMINAL_TIME,
                        "time=${TimeUtils.getNowString()}"
                    )
                    commandItems.add(command)
                    showLoadingDialog(StringUtils.getString(R.string.processing))
                    sendCommandFromCmdList(isStartTimeoutJob = true)
                }
            })
        XPopup.Builder(context)
            .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
            .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(popupView)
            .show()
    }

    override fun lazyLoadData() {
        //4G 模式下，直接查询设备工作模式
        if (communicateWay is NetPlatformConnect) {
            onNetPlatformReady()
        } else {
            bleViewModel.launch(bleDevice!!)
        }
    }

    private fun onNetPlatformReady() {

    }

    override fun onBleDeviceReady() {
        super.onBleDeviceReady()
        launchWithViewLifecycle {
//            delay(2000) //延迟 timeMillis 秒后，提示超时
            //蓝牙模式下，等蓝牙建立连接后查询设备工作模式
//            queryData()
        }
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        super.doNetDispatchSuccess(cmdStr)
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME -> {
                mCommandResponseStates.isResponseLoading.set(true)
                mCommandResponseStates.isResponseSuccess.set(false)
                showTimeCalibrationPopup()
            }

            IOTCommandType.SET_TERMINAL_TIME -> {
                mCommandResponseStates.isResponseLoading.set(true)
                mCommandResponseStates.isResponseSuccess.set(false)
            }

            IOTCommandType.QUERY_SAMPLE -> {
                mCommandResponseStates.isResponseLoading.set(true)
                mCommandResponseStates.isResponseSuccess.set(false)
//                showTelemetryDataPopup()
            }

            else -> {}
        }
    }

    override fun doCmdResponseResultError(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME,
            IOTCommandType.SET_TERMINAL_TIME,
            IOTCommandType.QUERY_SAMPLE -> {
                mCommandResponseStates.isResponseLoading.set(false)
                mCommandResponseStates.isResponseSuccess.set(false)
                mCommandResponseStates.responseContent.set(errorMsg)
            }

            else -> {
                super.doCmdResponseResultError(cmdStr, errorMsg)
            }
        }
    }

    override fun doCmdResponseResultTimeOut(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME,
            IOTCommandType.SET_TERMINAL_TIME,
            IOTCommandType.QUERY_SAMPLE -> {
                mCommandResponseStates.isResponseLoading.set(false)
                mCommandResponseStates.isResponseSuccess.set(false)
                mCommandResponseStates.responseContent.set("指令响应超时")
            }

            else -> {
                super.doCmdResponseResultTimeOut(cmdStr, errorMsg)
            }
        }
    }

    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowMsg: Boolean,
        msg: String
    ) {
        super.showNearbyCommunicationTimeoutAlert(cmdStr, isDismissLoadingDialog, false, msg)
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME,
            IOTCommandType.SET_TERMINAL_TIME,
            IOTCommandType.QUERY_SAMPLE -> {
                mCommandResponseStates.isResponseLoading.set(false)
                mCommandResponseStates.isResponseSuccess.set(false)
                mCommandResponseStates.responseContent.set("指令响应超时")
            }

            else -> {

            }
        }
    }

    override fun setResultData(cmdStr: String) {
        updateLastCommunicationTime()
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME -> {//查询终端时间
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_TERMINAL_TIME
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        handleFailureResult(result.message, false)
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set(result.message)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(true)
                        mCommandResponseStates.isCalibratingSuccess.set(false)
                        mCommandResponseStates.deviceTime.set(result.data)
                        mCommandResponseStates.systemTime.set(TimeUtils.getNowString())
                    }
                }
            }

            IOTCommandType.SET_TERMINAL_TIME -> {//设置终端时间
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        handleFailureResult(result.message, false)
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set(result.message)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(true)
                        mCommandResponseStates.isCalibratingSuccess.set(true)
                        mCommandResponseStates.deviceTime.set(mCommandResponseStates.systemTime.get())
                    }
                }
            }

            IOTCommandType.QUERY_SAMPLE -> {//召测
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        handleFailureResult(result.message, false)
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(false)
                        mCommandResponseStates.responseContent.set(result.message)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        mCommandResponseStates.isResponseLoading.set(false)
                        mCommandResponseStates.isResponseSuccess.set(true)
                        try {
                            mCommandResponseStates.responseContent.set(result.data)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }

            IOTCommandType.REBOOT -> {//重启设备
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = StringUtils.getString(R.string.reboot_failed) + result.message
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show(StringUtils.getString(R.string.device_reboot_tip))
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    override fun createObserver() {
        super.createObserver()
        setupHeartbeat()
    }

    /**
     * 设置心跳检查
     */
    private fun setupHeartbeat() {
        launchWithViewLifecycle {
            lastCommunicationTime
                .debounce(AppContants.Communication.DELAY_10000_MILLIS)  // 30秒无更新触发
                .collect { lastUpdateTime ->
                    val updateTime = TimeUtils.millis2String(lastUpdateTime, "yyyy-MM-dd HH:mm:ss")
                    //仅当设备连接并且需要发送心跳时，才发送心跳包
                    if (mHeadStates.isConnected.get()) {
                        Timber.d("发送心跳包指令 startTime: ${TimeUtils.getNowString()}，lastUpdateTime：$updateTime")
                        val command = IOTCommandUtil.getCommand(IOTCommandType.HEART_BEAT)
                        Timber.d("发送心跳包指令: $command")
                        sendBleCommand(command)
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}