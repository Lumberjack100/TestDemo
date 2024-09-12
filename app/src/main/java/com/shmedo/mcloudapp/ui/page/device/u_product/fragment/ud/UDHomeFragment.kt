package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.google.android.flexbox.FlexboxLayoutManager
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.UDCommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUdHomeBinding
import com.shmedo.mcloudapp.databinding.ItemSubConfigModuleBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
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
import com.shmedo.mcloudapp.model.PlatformLabel
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/8/21
 * @desc: 一体化雷达水位计首页
 *
 */
class UDHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdHomeBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mHeadStates: UDHomeViewModel by viewModels()
    private val mCommandResponseStates: CommandResponseViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var queryMeasureDataTimeoutJob: Job? = null
    private var repeatPollNum = 0 //重复轮询次数

    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ud_home, BR.stateVM, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdHomeBinding
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
        initPlatformAdapter()
        initModuleAdapter()
    }

    private fun initPlatformAdapter() {
        binding.llDeviceInfo.rvPlatform.setup { rv ->
            rv.layoutManager = FlexboxLayoutManager(context)
            addType<PlatformLabel>(R.layout.item_platform_label)
        }
    }

    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionVisible.set(true)
        mHeadStates.productLogoResId.set(R.drawable.device_logo_niweiji)

        mHeadStates.productName.set(deviceInfo.productName)
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)
        mHeadStates.deviceName.set(if (deviceInfo.deviceName == deviceInfo.deviceToken) deviceInfo.productToken else deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken })

        when (communicateWay) {
            NetPlatformConnect -> {
                mHeadStates.isConnectOperateVisible.set(false)
            }

            BleConnect -> {
                mHeadStates.isConnectOperateVisible.set(true)
                mHeadStates.connectOperateText.set("蓝牙连接")
            }

            else -> {}
        }

        initModuleData()
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        mHeadStates.isConnected.set(isConnected)
        if (isConnected) {
            mHeadStates.connectOperateText.set("断开连接")
            mHeadStates.productLogoResId.set(R.drawable.device_logo_niweiji)
            initPlatformStatus("蓝牙已连接", "1")
        } else {
            mHeadStates.connectOperateText.set("蓝牙连接")
            mHeadStates.productLogoResId.set(R.drawable.device_logo_niweiji_offline)
            initPlatformStatus("蓝牙已断开", "0")
        }

        //刷新模块状态
        binding.rvModule.models?.forEach {
            if (it is ConfigModuleTree) {
                it.configModules.forEach { configModule ->
                    configModule.functionModule.refreshStatus(isConnected)
                }
            }
        }
    }

    private fun initModuleAdapter() {
        binding.rvModule.linear().setup { rv ->
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group_ud)
            addType<ConfigModuleTree>(R.layout.item_sub_config_module)
            onCreate {
                when (itemViewType) {
                    R.layout.item_sub_config_module -> {
                        val itemBinding = getBinding<ItemSubConfigModuleBinding>()
                        itemBinding.rvSubModule.setup { subRv ->
                            subRv.addItemDecoration(
                                MyGridSpacingItemDecoration(
                                    4,
                                    ConvertUtils.dp2px(10f), false
                                )
                            )
                            addType<ConfigModule>(R.layout.item_device_config_module_ud)
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

        }
    }

    private fun initModuleData() {
        val groupList = mutableListOf<Any>()
        groupList.add(
            DeviceStatusInfoGroupItem(
                "设备信息",
                iconResId = R.drawable.ic_ud_device_info_group,
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
                            navId = R.id.action_global_to_commonRunningDeviceInfoStyle2Fragment
                        )
                    ),
                    ConfigModule(
                        RunningStatusModule(
                            name = "网络信息",
                            desc = "查看设备网络信息",
                            resID = R.drawable.ic_module_network_info,
                            navId = R.id.action_global_to_commonRunningDeviceInfoStyle2Fragment
                        )
                    ),
                    ConfigModule(
                        RunningStatusModule(
                            name = "状态信息",
                            desc = "查看设备运行状态信息",
                            resID = R.drawable.ic_basic_config,
                            navId = R.id.action_global_to_commonRunningDeviceInfoStyle2Fragment
                        )
                    ),
                    ConfigModule(
                        RunningStatusModule(
                            name = "位置信息",
                            desc = "查看设备位置信息",
                            resID = R.drawable.ic_module_location_info,
                            navId = R.id.action_global_to_commonRunningDeviceInfoStyle2Fragment
                        )
                    )
                )
            )
        )
        groupList.add(
            DeviceStatusInfoGroupItem(
                "设备配置",
                iconResId = R.drawable.ic_basic_config
            )
        )
        val configModuleTree = ConfigModuleTree(
            configModules = arrayListOf(
                ConfigModule(
                    WorkModeModule(
                        name = "工作模式",
                        desc = "报警上报参数配置",
                        resID = R.drawable.ic_module_report_mode,
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
                ConfigModule(
                    AdvancedSettingsModule(
                        name = "系统配置",
                        navId = R.id.action_global_to_advancedSettingFragment
                    )
                ),
            )
        )
        if (communicateWay is BleConnect) {
            configModuleTree.configModules.add(ConfigModule(CommandDebugConfigModule()))
        }
        groupList.add(configModuleTree)

        binding.rvModule.models = groupList
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

        fun onMeasureDataClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            measureData()
        }

        fun onTakePhotoClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            takePhoto()
        }

        fun onGoToSensorDataHistoryClick() {
            nav().navigate(
                R.id.action_global_to_udMonitorDataHistoryFragment,
                UDSensorDataHistoryFragment.newBundleArguments(deviceInfo)
            )
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

    private fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "value=0")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 查询测量数据
     */
    private fun queryMeasureData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_SAMPLE, "value=0")
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 测量数据
     */
    private fun measureData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_SAMPLE, "value=1")
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 拍照
     */
    private fun takePhoto() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_SAMPLE, "value=2")
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
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
        if (deviceInfo.onlineStatus) {
            initPlatformStatus("米度平台在线", "1")
            queryStatusInfo()
        } else {
            mHeadStates.productLogoResId.set(R.drawable.device_logo_niweiji_offline)
            initPlatformStatus("米度平台离线", "0")
        }
    }

    override fun onBleDeviceReady() {
        super.onBleDeviceReady()
        queryStatusInfo()
    }

    /**
     * 4G 透传指令成功
     */
    override fun doNetDispatchSuccess(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME -> {
                super.doNetDispatchSuccess(cmdStr)
                mCommandResponseStates.isResponseLoading.set(true)
                mCommandResponseStates.isResponseSuccess.set(false)
                showTimeCalibrationPopup()
            }

            IOTCommandType.SET_TERMINAL_TIME -> {
                super.doNetDispatchSuccess(cmdStr)
                mCommandResponseStates.isResponseLoading.set(true)
                mCommandResponseStates.isResponseSuccess.set(false)
            }

            else -> {
                super.doNetDispatchSuccess(cmdStr)
            }
        }
    }

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                dismissLoadingDialog()
            }

            IOTCommandType.QUERY_TERMINAL_TIME,
            IOTCommandType.SET_TERMINAL_TIME,
            -> {
                mCommandResponseStates.isResponseLoading.set(false)
                mCommandResponseStates.isResponseSuccess.set(false)
                mCommandResponseStates.responseContent.set(errorMsg)
            }

            IOTCommandType.QUERY_SAMPLE -> {
                mHeadStates.isMeasuring.set(false)
                if (cmdStr.contains("value=1")) {
                    Toaster.show("测量数据指令下发出错: $errorMsg")
                    dismissLoadingDialog()
                } else if (cmdStr.contains("value=2")) {
                    Toaster.show("拍照指令下发出错: $errorMsg")
                    dismissLoadingDialog()
                } else if (cmdStr.contains("value=0")) {
                    clearQueryMeasureDataTimeoutJob()
                }
            }

            else -> {
                super.doCmdResponseResultError(cmdStr, errorMsg)
            }
        }
    }

    /**
     * 4G 下发指令响应超时
     */
    override fun doCmdResponseResultTimeOut(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                dismissLoadingDialog()
            }

            IOTCommandType.QUERY_TERMINAL_TIME,
            IOTCommandType.SET_TERMINAL_TIME,
            -> {
                mCommandResponseStates.isResponseLoading.set(false)
                mCommandResponseStates.isResponseSuccess.set(false)
                mCommandResponseStates.responseContent.set("指令响应超时")
            }

            IOTCommandType.QUERY_SAMPLE -> {
                mHeadStates.isMeasuring.set(false)
                if (cmdStr.contains("value=1")) {
                    Toaster.show("测量数据指令响应超时")
                    dismissLoadingDialog()
                } else if (cmdStr.contains("value=2")) {
                    Toaster.show("拍照指令响应超时")
                    dismissLoadingDialog()
                } else if (cmdStr.contains("value=0")) {
                    clearQueryMeasureDataTimeoutJob()
                }
            }

            else -> {
                super.doCmdResponseResultTimeOut(cmdStr, errorMsg)
            }
        }
    }

    /**
     * 蓝牙下发指令响应超时
     */
    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowMsg: Boolean,
        msg: String
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr,
                    isDismissLoadingDialog,
                    isShowMsg = false,
                    msg
                )
            }

            IOTCommandType.QUERY_TERMINAL_TIME,
            IOTCommandType.SET_TERMINAL_TIME,
            -> {
                mCommandResponseStates.isResponseLoading.set(false)
                mCommandResponseStates.isResponseSuccess.set(false)
                mCommandResponseStates.responseContent.set("指令响应超时")
            }

            IOTCommandType.QUERY_SAMPLE -> {
                mHeadStates.isMeasuring.set(false)
                if (cmdStr.contains("value=1")) {
                    super.showNearbyCommunicationTimeoutAlert(
                        cmdStr,
                        isDismissLoadingDialog,
                        isShowMsg,
                        msg = "测量数据指令响应超时"
                    )
                } else if (cmdStr.contains("value=2")) {
                    super.showNearbyCommunicationTimeoutAlert(
                        cmdStr,
                        isDismissLoadingDialog,
                        isShowMsg,
                        msg = "拍照指令响应超时"
                    )
                } else if (cmdStr.contains("value=0")) {
                    clearQueryMeasureDataTimeoutJob()
                    super.showNearbyCommunicationTimeoutAlert(
                        cmdStr,
                        isDismissLoadingDialog,
                        isShowMsg = false,
                        msg
                    )
                }
            }

            else -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr,
                    isDismissLoadingDialog,
                    isShowMsg,
                    msg
                )
            }
        }
    }

    override fun setResultData(cmdStr: String) {
        updateLastCommunicationTime()

        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result =
                    iotParseManager.parse<String>(cmdStr, IOTCommandType.MD_GET_DEVICE_STATUS)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initStatusInfo(result.data)
                    }
                }
            }

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
                        val errMsg = "召测出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                        mHeadStates.isMeasuring.set(false)
                        clearQueryMeasureDataTimeoutJob()
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        processSampleResponse(result.data)
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun initPlatformStatus(content: String, status: String) {
        val platformLabels = mutableListOf<PlatformLabel>()
        platformLabels.add(
            PlatformLabel(
                content,
                textColorRes = status.compareAndReturn(
                    "1",
                    ColorUtils.getColor(R.color.colorPrimary),
                    ColorUtils.getColor(R.color.sub_title_text_color)
                ),
                bgResId = status.compareAndReturn(
                    "1",
                    R.drawable.bg_label_blue_corner_15dp,
                    R.drawable.bg_label_gray_corner_15dp
                )
            )
        )
        binding.llDeviceInfo.rvPlatform.models = platformLabels
    }

    private fun initStatusInfo(content: String) {
        if (binding.llDeviceInfo.rvPlatform.mutable.size >= 2) {
            return
        }
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCommonCurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle

                val status = when (stateInfo.deviceStatus) {
                    "-2" -> "告警"
                    "-3" -> "故障"
                    else -> "正常"
                }
                mHeadStates.productLogoResId.set(
                    status.compareAndReturn(
                        "故障",
                        R.drawable.device_logo_niweiji_error,
                        status.compareAndReturn(
                            "告警",
                            R.drawable.device_logo_niweiji_alarm,
                            R.drawable.device_logo_niweiji
                        )
                    )
                )

                val platformLabel = PlatformLabel(
                    status, textColorRes = status.compareAndReturn(
                        "故障",
                        ColorUtils.getColor(R.color.red_F13838),
                        status.compareAndReturn(
                            "告警",
                            ColorUtils.getColor(R.color.yellow_FDA251),
                            ColorUtils.getColor(R.color.colorPrimary)
                        )
                    ),
                    bgResId = status.compareAndReturn(
                        "故障",
                        R.drawable.bg_device_offline_state_flag_corner_10dp,
                        status.compareAndReturn(
                            "告警",
                            R.drawable.bg_label_yellow_corner_15dp,
                            R.drawable.bg_label_blue_corner_15dp
                        )
                    )
                )
                binding.llDeviceInfo.rvPlatform.bindingAdapter.apply {
                    mutable.add(platformLabel)
                    notifyItemInserted(itemCount)
                }
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 处理召测响应
     */
    private fun processSampleResponse(content: String) {
        try {
            //{"value":1,"time":"2024-09-04 11:07:34"}
            //{"value":2,"filename":"20240904111431"}
            //{"value":0,"obj_alt":28.320,"ld_value":3.514,"z_angle":86.4,"time":"2024-09-04 14:32:32"}
            val resultMap = MoshiUtil.fromJson<Map<String, String>>(content) ?: return
            resultMap["value"]?.let { code ->
                when (code) {
                    "1" -> {
                        Toaster.show("测量数据指令下发成功,开始测量数据")
                        mHeadStates.isMeasuring.set(true)
                        clearQueryMeasureDataTimeoutJob()
                        processQueryMeasureData()
                    }

                    "2" -> {
                        Toaster.show("拍照指令下发成功，请稍后在历史中查看图片")
                    }

                    "0" -> {
                        //已经有数据
                        if (resultMap.containsKey("obj_alt")
                            && resultMap.containsKey("ld_value")
                            && resultMap.containsKey("z_angle")
                            && resultMap.containsKey("time")
                        ) {
                            mHeadStates.isMeasuring.set(false)
                            clearQueryMeasureDataTimeoutJob()

                            val waterSurfaceElevation = resultMap["obj_alt"] ?: ""
                            val airDistance = resultMap["ld_value"] ?: ""
                            val installationAngle = resultMap["z_angle"] ?: ""
                            val measurementTime = resultMap["time"]?.replace("-", ".") ?: ""

                            mHeadStates.waterSurfaceElevation.set("$waterSurfaceElevation m")
                            mHeadStates.airDistance.set("$airDistance m")
                            mHeadStates.installationAngle.set("$installationAngle°")
                            mHeadStates.measurementTime.set(measurementTime)
                            return
                        }

                        processQueryMeasureData()
                    }

                    else -> {}
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun processQueryMeasureData() {
        //启动一个新的协程作为超时Job
        queryMeasureDataTimeoutJob?.cancel()
        queryMeasureDataTimeoutJob = launchWithViewLifecycle {
            if (repeatPollNum >= 6) {
                clearQueryMeasureDataTimeoutJob()
                return@launchWithViewLifecycle
            }
            delay(AppContants.Communication.DELAY_10000_MILLIS) //延迟 timeMillis 秒
            repeatPollNum++
            Timber.d("查询测量数据轮询次数：$repeatPollNum")
            queryMeasureData()
        }
    }

    private fun clearQueryMeasureDataTimeoutJob() {
        queryMeasureDataTimeoutJob?.cancel()
        queryMeasureDataTimeoutJob = null
        repeatPollNum = 0
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
                .debounce(AppContants.Communication.DELAY_20000_MILLIS)  //20秒无更新触发
                .collect { lastUpdateTime ->
                    val updateTime =
                        TimeUtils.millis2String(lastUpdateTime, "yyyy-MM-dd HH:mm:ss")
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