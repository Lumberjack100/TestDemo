package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.baseclickproxy.DoubleClickListener
import com.shmedo.mcloudapp.databinding.FragmentUdHomeBinding
import com.shmedo.mcloudapp.databinding.ItemSubConfigModuleBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showDialogFragment
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.model.AlarmConfigModule
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusEnum
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.LoraConfigModule
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.model.SensorConfigModule
import com.shmedo.mcloudapp.model.TimeCalibrationModule
import com.shmedo.mcloudapp.model.WorkModeModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.ui.page.device.common.CommonSensorDataHistoryFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.u_product.dialog.FindDeviceBeepDialog
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDHomeViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.utils.UDDeviceStatusProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/8/21
 * @desc: 一体化雷达水位/泥位计首页
 *
 */
class UDHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdHomeBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mHeadStates: UDHomeViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()
    private val iotParseManager: IOTParserManager by inject()

    private var deviceStatusCheckJob: Job? = null
    private var abnormalInfoJob: Job? = null
    private var queryMeasureResultTimeoutJob: Job? = null
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
        toolbarViewModel.toolbarTitleText.set("返回")
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            if (bleViewModel.isConnected()) {
                bleViewModel.disconnect()
            }
            mActivity.finish()
        }
        registerOnBackPressedDispatcher {
            if (bleViewModel.isConnected()) {
                bleViewModel.disconnect()
            }
            mActivity.finish()
        }
        initDeviceLogoDoubleClickListener()
        initModuleAdapter()
    }

    private fun initDeviceLogoDoubleClickListener() {
        binding.llDeviceInfo.ivDeviceLogo.setOnClickListener(object : DoubleClickListener() {
            override fun onDoubleClick(v: View) {
                if (isBleDisconnected() || isNetDisconnected()) {
                    return
                }
                searchDevice()
            }
        })
    }

    override fun initData() {
        super.initData()
        when (productType) {
            ProductType.U_D_1 -> {
                mHeadStates.productLogoResId.set(R.drawable.device_logo_niweiji)
            }

            ProductType.U_D_2 -> {
                mHeadStates.productLogoResId.set(R.drawable.device_logo_niweiji)
            }

            else -> {}
        }
        mHeadStates.productName.set(productType.productName)
        mHeadStates.productToken.set(productType.productToken)
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)

        when (communicateWay) {
            NetPlatformConnect -> {
                toolbarViewModel.toolbarIvActionVisible.set(false)
            }

            BleConnect -> {
                toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_ble_connect)
                toolbarViewModel.toolbarIvActionVisible.set(true)
            }

            else -> {}
        }
        initModuleData()
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        mHeadStates.isConnected.set(isConnected)
        if (isConnected) {
            toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_ble_disconnect)
            mHeadStates.productLogoResId.set(R.drawable.device_logo_niweiji)
            mHeadStates.iotPlatformStateText.set("蓝牙已连接")
        } else {
            toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_ble_connect)
            mHeadStates.productLogoResId.set(R.drawable.device_logo_niweiji_offline)
            mHeadStates.iotPlatformStateText.set("蓝牙已断开")

            mHeadStates.deviceStatusCode.set(DeviceStatusEnum.UNKNOWN.code)
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
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group2)
            addType<ConfigModuleTree>(R.layout.item_sub_config_module)
            addType<GapItem>(R.layout.item_device_status_info_gap)
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
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    ConfigModule(
                        CommonModule(
                            name = "基本信息",
                            resID = R.drawable.ic_module_basic_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_udBaseInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "网络信息",
                            resID = R.drawable.ic_module_net_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_udNetInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "状态信息",
                            resID = R.drawable.ic_module_state_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_udSensorInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "位置信息",
                            resID = R.drawable.ic_module_location_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_commonLocationInfoFragment
                        )
                    )
                )
            )
        )

        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree()
        configModuleTree.configModules.add(
            ConfigModule(
                WorkModeModule(
                    name = "工作模式",
                    resID = R.drawable.ic_module_work_mode_new,
                    navId = R.id.action_global_to_udWorkModelParamFragment
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                CommonModule(
                    name = "网络配置",
                    resID = R.drawable.ic_module_network_setting,
                    navId = R.id.action_global_to_udMobileNetworkParamFragment
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                DataCenterModule(
                    name = "链路配置",
                    resID = R.drawable.ic_module_datacenter_new,
                    navId = R.id.action_global_to_udProductDataCenterHomeFragment
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                CommonModule(
                    name = "海拔配置",
                    resID = R.drawable.ic_module_cors,
                    navId = R.id.action_global_to_udCORSParamFragment
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                SensorConfigModule(
                    name = "传感配置",
                    resID = R.drawable.ic_module_sensor_setting_new,
                    navId = R.id.action_global_to_udProductSensorParamFragment
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                SensorConfigModule(
                    name = "端口配置",
                    resID = R.drawable.ic_module_serial_port,
                    navId = R.id.action_global_to_udSerialPortParamFragment
                )
            )
        )
//        if (productType == ProductType.U_D_2) {
        configModuleTree.configModules.add(
            ConfigModule(
                LoraConfigModule(
                    name = "电台配置",
                    resID = R.drawable.ic_module_lora_new,
                    navId = R.id.action_global_to_udRadioParamFragment
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                AlarmConfigModule(
                    resID = R.drawable.ic_module_lora_new,
                    navId = R.id.action_global_to_alarmSettingFragment
                )
            )
        )
//        }
        configModuleTree.configModules.add(
            ConfigModule(
                TimeCalibrationModule(
                    name = "时间校准",
                    resID = R.drawable.ic_module_time_calibration_new,
                    navId = R.id.action_global_to_time_calibration
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                AdvancedSettingsModule(
                    name = "系统配置",
                    resID = R.drawable.ic_module_system_setting,
                    navId = R.id.action_global_to_advancedSettingFragment
                )
            )
        )
        if (communicateWay is BleConnect) {
            configModuleTree.configModules.add(
                ConfigModule(
                    CommandDebugConfigModule(
                        resID = R.drawable.ic_module_cmd_debug_new,
                    )
                )
            )
        }
        groupList.add(configModuleTree)

        binding.rvModule.models = groupList
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
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
            if (mHeadStates.isMeasuring.get())
                return

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
            nav().safeNavigate(
                R.id.action_global_to_commonSensorDataHistoryFragment,
                CommonSensorDataHistoryFragment.newBundleArguments(productType, deviceInfo)
            )
        }
    }

    private fun processSubModuleItemClick(module: DeviceFunctionModule) {
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (module) {
            is DataCenterModule -> {
                nav().safeNavigate(
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

            is CommandDebugConfigModule -> {//指令下发
                val bundle = BleCustomCommandLogPrintFragment.newBundleArguments(
                    true,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().safeNavigate(module.navId, bundle)
            }

            else -> {
                if (module.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().safeNavigate(
                        module.navId,
                        bundle
                    )
                } else {
                    Toaster.show("正在开发中")
                }
            }
        }
    }

    private fun queryDeviceStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "method=0")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 查询测量数据
     */
    private fun queryMeasureData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=0")
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 测量数据
     */
    private fun measureData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=1")
        commandItems.add(command)

        mHeadStates.measureDataLoadingDialogId =
            showLoadingWithUUID(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 拍照
     */
    private fun takePhoto() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=2")
        commandItems.add(command)

        mHeadStates.measureDataLoadingDialogId =
            showLoadingWithUUID(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        //4G 模式下，直接查询设备工作模式
        if (communicateWay is NetPlatformConnect) {
            onNetPlatformReady()
        } else {
            bleViewModel.launch(bleDevice!!)
        }
        initLastHistorySensorData()
    }

    private fun onNetPlatformReady() {
        if (deviceInfo.onlineStatus) {
            mHeadStates.productLogoResId.set(R.drawable.device_logo_niweiji)
            mHeadStates.iotPlatformStateText.set("米度平台在线")
            queryDeviceStatusInfo()
        } else {
            mHeadStates.productLogoResId.set(R.drawable.device_logo_niweiji_offline)
            mHeadStates.iotPlatformStateText.set("米度平台离线")
            mHeadStates.deviceStatusCode.set(DeviceStatusEnum.UNKNOWN.code)
        }
        //刷新模块状态
        binding.rvModule.models?.forEach {
            if (it is ConfigModuleTree) {
                it.configModules.forEach { configModule ->
                    configModule.functionModule.refreshStatus(deviceInfo.onlineStatus)
                }
            }
        }
    }

    override fun onBleDeviceReady() {
        super.onBleDeviceReady()
        queryDeviceStatusInfo()
    }

    private fun initLastHistorySensorData() {
        if (NetworkUtils.isConnected()) {
            launchWithViewLifecycle {
                val resultMap: Map<String, String> =
                    deviceRequestViewModel.queryLatestSensorData(
                        deviceInfo.deviceToken,
                        iotSensorTypeList = arrayListOf("904", "206")
                    )
                if (resultMap.isEmpty())
                    return@launchWithViewLifecycle

                val waterSurfaceElevation = resultMap["liquid_surface_alt"]?.let { "$it m" }
                    ?: AppContants.PLACE_HOLDER_VALUE
                val airDistance =
                    resultMap["ullage"]?.let { "$it m" } ?: AppContants.PLACE_HOLDER_VALUE
                val installationAngle =
                    resultMap["z"]?.let { "$it °" } ?: AppContants.PLACE_HOLDER_VALUE
                val todayRainfall =
                    resultMap["today_rain"]?.let { "$it mm" } ?: AppContants.PLACE_HOLDER_VALUE
                val measurementTime = resultMap["time"]?.replace(".000", "")?.replace("-", ".")
                    ?: AppContants.PLACE_HOLDER_VALUE

                mHeadStates.waterSurfaceElevation.set(waterSurfaceElevation)
                mHeadStates.airDistance.set(airDistance)
                mHeadStates.installationAngle.set(installationAngle)
                mHeadStates.todayRainfall.set(todayRainfall)
                mHeadStates.measurementTime.set(measurementTime)
            }
        }
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
                dismissLoadingDialog()
            }

            IOTCommandType.SAMPLE -> {
                stopMeasurementAnimation()
                if (cmdStr.contains("method=1")) {
                    super.doCmdResponseResultError(
                        cmdStr = cmdStr,
                        errMsg = "测量数据指令下发出错: $errMsg",
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                } else if (cmdStr.contains("method=2")) {
                    super.doCmdResponseResultError(
                        cmdStr = cmdStr,
                        errMsg = "拍照指令下发出错: $errMsg",
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                }
            }

            IOTCommandType.MD_SEARCH_DEVICE -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = "设备查找出错: $errMsg",
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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                dismissLoadingDialog()
            }

            IOTCommandType.SAMPLE -> {
                stopMeasurementAnimation()
                if (!cmdStr.contains("method=0")) {
                    super.doCmdResponseResultTimeOut(
                        cmdStr = cmdStr,
                        errMsg = "设备未响应",
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                }
            }

            IOTCommandType.MD_SEARCH_DEVICE -> {
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = "设备未响应",
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            else -> {
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowErrMsg,
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
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = false,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }

            IOTCommandType.SAMPLE -> {
                stopMeasurementAnimation()
                if (cmdStr.contains("method=0")) {
                    super.showNearbyCommunicationTimeoutAlert(
                        cmdStr = cmdStr,
                        isDismissLoadingDialog = isDismissLoadingDialog,
                        isShowErrMsg = false,
                        isMessageDialog = isMessageDialog,
                        errMsg = errMsg
                    )
                } else {
                    super.showNearbyCommunicationTimeoutAlert(
                        cmdStr = cmdStr,
                        isDismissLoadingDialog = isDismissLoadingDialog,
                        isShowErrMsg = true,
                        isMessageDialog = true,
                        errMsg = "设备未响应"
                    )
                }
            }

            IOTCommandType.MD_SEARCH_DEVICE -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = true,
                    isMessageDialog = true,
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
                        initStatusInfo(cmdStr, result.data)
                    }
                }
            }

            IOTCommandType.SAMPLE -> {//召测
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "召测出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                        stopMeasurementAnimation()
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        dismissLoadingDialog(mHeadStates.measureDataLoadingDialogId)
                        processSampleResponse(cmdStr, result.data)
                    }
                }
            }

            IOTCommandType.MD_SEARCH_DEVICE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设备查找出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            showDialogFragment(FindDeviceBeepDialog.TAG) {
                                FindDeviceBeepDialog.newInstance(ProductType.U_D_2)
                            }
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun initStatusInfo(cmdStr: String, content: String) {
        if (!cmdStr.contains("method=0"))
            return

        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle

                val status = when (stateInfo.deviceStatus) {
                    "-2" -> "告警"
                    "-3" -> "故障"
                    else -> "正常"
                }
                mHeadStates.deviceStatusCode.set(stateInfo.deviceStatus)
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
                if (status == "正常") {
                    mHeadStates.warnErrorText.set("正常")
                    return@launchWithViewLifecycle
                }
                processAbnormalInfo(stateInfo.deviceError, stateInfo.deviceWarn)
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun processAbnormalInfo(
        deviceError: Map<String, String>? = null,
        deviceWarn: Map<String, String>? = null
    ) {
        try {
            val errorInfoList = UDDeviceStatusProcessor.processAbnormalInfo(deviceError, deviceWarn)
            handleAbnormalInfo(errorInfoList)
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 处理设备异常信息轮播展示
     * 每隔3秒切换一次，取出异常信息列表中的每一条异常信息，轮播显示
     */
    private fun handleAbnormalInfo(errorInfoList: List<String>) {
        //取消之前的job（如果存在）
        abnormalInfoJob?.cancel()

        //如果列表为空，直接返回
        if (errorInfoList.isEmpty()) {
            return
        }
        if (errorInfoList.size == 1) {
            mHeadStates.warnErrorText.set(errorInfoList[0])
            return
        }
        abnormalInfoJob = launchWithViewLifecycle {
            flow {
                while (true) {
                    errorInfoList.forEach { errorInfo ->
                        emit(errorInfo)
                        delay(1500) // 延迟3秒
                    }
                }
            }.collect { errorInfo ->
                mHeadStates.warnErrorText.set(errorInfo)
            }
        }
    }

    /**
     * 处理召测响应
     */
    private fun processSampleResponse(cmdStr: String, content: String) {
        try {
            //{"time":"2024-09-04 11:07:34"}
            //{"filename":"20240904111431"}
            //{"obj_alt":28.320,"ld_value":3.514,"z_angle":86.4,"time":"2024-09-04 14:32:32"}
            if (cmdStr.contains("method=1")) {
                startMeasurementAnimation()
                clearQueryMeasureResultTimeoutJob()
                startQueryMeasureResultJob()
            } else if (cmdStr.contains("method=2")) {
                showMessageDialog("拍照指令已下发，请稍后在历史数据中查看拍照图片")
            } else {
                val resultMap: Map<String, String> =
                    if (content.isEmpty()) mapOf() else MoshiUtil.fromJson<Map<String, String>>(
                        content
                    ) ?: mapOf()

                //已经有数据
                if (resultMap.containsKey("obj_alt")
                    || resultMap.containsKey("ld_value")
                    || resultMap.containsKey("z_angle")
                    || resultMap.containsKey("today_rain")
                    || resultMap.containsKey("time")
                ) {
                    stopMeasurementAnimation()

                    val waterSurfaceElevation =
                        resultMap["obj_alt"]?.let { "$it m" } ?: AppContants.PLACE_HOLDER_VALUE
                    val airDistance =
                        resultMap["ld_value"]?.let { "$it m" } ?: AppContants.PLACE_HOLDER_VALUE
                    val installationAngle =
                        resultMap["z_angle"]?.let { "$it °" } ?: AppContants.PLACE_HOLDER_VALUE
                    val todayRainfall =
                        resultMap["today_rain"]?.let { "$it mm" } ?: AppContants.PLACE_HOLDER_VALUE
                    val measurementTime =
                        resultMap["time"]?.replace("-", ".") ?: AppContants.PLACE_HOLDER_VALUE

                    mHeadStates.waterSurfaceElevation.set(waterSurfaceElevation)
                    mHeadStates.airDistance.set(airDistance)
                    mHeadStates.installationAngle.set(installationAngle)
                    mHeadStates.todayRainfall.set(todayRainfall)
                    mHeadStates.measurementTime.set(measurementTime)
                    return
                }

                startQueryMeasureResultJob()
            }
        } catch (e: Exception) {
            stopMeasurementAnimation()
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun startMeasurementAnimation() {
        // 显示进度条并开始动画
        mHeadStates.isMeasuring.set(true)
        binding.llRadarWaterGaugeMeasureData.btnMeasureData.startProgressAnimation()
    }

    private fun stopMeasurementAnimation() {
        dismissLoadingDialog(mHeadStates.measureDataLoadingDialogId)
        // 隐藏进度条并停止动画
        mHeadStates.isMeasuring.set(false)
        binding.llRadarWaterGaugeMeasureData.btnMeasureData.stopProgressAnimation()
    }

    private fun startQueryMeasureResultJob() {
        //启动一个新的协程作为超时Job
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = launchWithViewLifecycle {
            if (repeatPollNum >= REPEAT_POLL_NUM) {
                stopMeasurementAnimation()
                return@launchWithViewLifecycle
            }
            delay(AppContants.Communication.DELAY_5000_MILLIS) //延迟 timeMillis 秒
            repeatPollNum++
            Timber.d("查询测量数据轮询次数：$repeatPollNum")
            queryMeasureData()
        }
    }

    private fun clearQueryMeasureResultTimeoutJob() {
        repeatPollNum = 0
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = null
    }

    override fun createObserver() {
        super.createObserver()
        setupHeartbeat()
        if (communicateWay is NetPlatformConnect) {
            checkDeviceOnlineStatus()
        }
    }

    /**
     * 设置心���检查
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

    private fun checkDeviceOnlineStatus() {
        // 取消现有的job
        deviceStatusCheckJob?.cancel()

        // 创建新的job，每30秒执行一次
        deviceStatusCheckJob = launchWithViewLifecycle {
            while (isActive) {
                try {
                    deviceRequestViewModel.getDeviceDetailInfo(deviceInfo.deviceToken) { error: Throwable ->
                        addDeviceLogItem(Log.ERROR, error.errorMsg)
                    }?.let { deviceDetailInfo ->
                        deviceInfo = deviceDetailInfo.deviceInfo
                        // 如果设备在线状态发生变化，更新UI
                        if (deviceInfo.onlineStatus != mHeadStates.isConnected.get()) {
                            onNetPlatformReady()
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
                delay(30000) // 延迟30秒
            }
        }
    }

    // 在 onDestroy 中取消 job
    override fun onDestroy() {
        super.onDestroy()
        deviceStatusCheckJob?.cancel()
        deviceStatusCheckJob = null
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        const val REPEAT_POLL_NUM = 10
    }
}