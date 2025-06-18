package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m20s

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
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
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.baseclickproxy.DoubleClickListener
import com.shmedo.mcloudapp.databinding.FragmentM20sHomeBinding
import com.shmedo.mcloudapp.databinding.ItemSubConfigModuleBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showDialogFragment
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
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.ui.page.device.common.CommonSensorDataHistoryFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.u_product.dialog.FindDeviceBeepDialog
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.M20SHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：  M20S 配置主页
 */
class M20SHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM20sHomeBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mHeadStates: M20SHomeViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()
    private val iotParseManager: IOTParserManager by inject()

    private var lastOnlineStatus: Boolean = false//在线状态
    private var deviceStatusCheckJob: Job? = null


    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m20s_home, BR.stateVM, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM20sHomeBinding
        binding.llToolbar.toolbar.title = "返回"
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
        mHeadStates.productName.set(productType.productName)
        mHeadStates.productToken.set(productType.productToken)
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)

        toolbarViewModel.toolbarIvActionVisible.set(communicateWay is BleConnect)

        initModuleData()
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        mHeadStates.isConnected.set(isConnected)
        if (isConnected) {
            mHeadStates.productLogoResId.set(R.drawable.device_logo_m20)
            mHeadStates.iotPlatformStateText.set("蓝牙已连接")
            toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_ble_disconnect)
        } else {
            mHeadStates.productLogoResId.set(R.drawable.device_logo_m20_offline)
            mHeadStates.iotPlatformStateText.set("蓝牙已断开")
            toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_ble_connect)

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
                            navId = R.id.action_global_to_m20SBaseInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "网络信息",
                            resID = R.drawable.ic_module_net_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_m20SNetInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "状态信息",
                            resID = R.drawable.ic_module_state_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_m20SStatusInfoFragment
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
        if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
            configModuleTree.configModules.add(
                ConfigModule(
                    CommonModule(
                        name = "工作模式",
                        resID = R.drawable.ic_module_work_mode_new,
                        navId = R.id.action_global_to_m20SWorkModelFragment
                    )
                )
            )
        }
        configModuleTree.configModules.add(
            ConfigModule(
                DataCenterModule(
                    name = "链路配置",
                    resID = R.drawable.ic_module_datacenter_new,
                    navId = R.id.action_global_to_universalDataCenterHomeFragment
                )
            )
        )
        if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
            configModuleTree.configModules.add(
                ConfigModule(
                    CommonModule(
                        name = "电台配置",
                        resID = R.drawable.ic_module_lora_new,
                        navId = R.id.action_global_to_m20SRadioSettingFragment
                    )
                )
            )
        }
        if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
            configModuleTree.configModules.add(
                ConfigModule(
                    CommonModule(
                        name = "报警配置",
                        resID = R.drawable.ic_module_alarm_new,
                        navId = R.id.action_global_to_alarmSettingFragment
                    )
                )
            )
        }
        if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
            configModuleTree.configModules.add(
                ConfigModule(
                    CommonModule(
                        name = "卫星通信",
                        resID = R.drawable.ic_module_cors,
                        navId = 0,
                        isSupport = false
                    )
                )
            )
        }
        configModuleTree.configModules.add(
            ConfigModule(
                CommonModule(
                    name = "时间校准",
                    resID = R.drawable.ic_module_time_calibration_new,
                    navId = R.id.action_global_to_time_calibration
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                CommonModule(
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
                bleViewModel.disconnect()
            } else {
                bleViewModel.launch(bleDevice!!)
            }
        }

        fun onGoToSensorDataHistoryClick() {
            nav().safeNavigate(
                R.id.action_global_to_commonSensorDataHistoryFragment,
                CommonSensorDataHistoryFragment.Companion.newBundleArguments(
                    productType,
                    deviceInfo
                )
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
                    UniversalDataCenterHomeFragment.Companion.newBundleArguments(
                        centerNum = 4,
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                )
            }

            is CommandDebugConfigModule -> {//指令下发
                val bundle = BleCustomCommandLogPrintFragment.Companion.newBundleArguments(
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
                    val bundle = newBundleArguments(
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

    private fun queryStatusInfo() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
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
        lastOnlineStatus = deviceInfo.onlineStatus
        if (deviceInfo.onlineStatus) {
            mHeadStates.iotPlatformStateText.set("米度平台在线")
            queryStatusInfo()
        } else {
            mHeadStates.productLogoResId.set(R.drawable.device_logo_m20_offline)
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
        queryStatusInfo()
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
                dismissLoadingDialog()
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
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                dismissLoadingDialog()
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
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = false,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
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
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result =
                    iotParseManager.parse<String>(cmdStr, IOTCommandType.QUERY_DEVICE_STATUS)
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

            IOTCommandType.MD_SEARCH_DEVICE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设备查找出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            showDialogFragment(FindDeviceBeepDialog.Companion.TAG) {
                                FindDeviceBeepDialog.Companion.newInstance(ProductType.GNSS_M_5)
                            }
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle


                val deviceAbnormalList =
                    if (stateInfo.self_check.isEmpty()) arrayListOf<String>() else DeviceStatusHelper.checkDeviceAbnormal(
                        stateInfo.self_check
                    )
                val status = if (deviceAbnormalList.isEmpty()) "正常" else "故障"
                mHeadStates.productLogoResId.set(
                    status.compareAndReturn(
                        "故障",
                        R.drawable.device_logo_m20_error,
                        status.compareAndReturn(
                            "告警",
                            R.drawable.device_logo_m20_alarm,
                            R.drawable.device_logo_m20
                        )
                    )
                )
                mHeadStates.deviceStatusCode.set(if (deviceAbnormalList.isEmpty()) "0" else "-3")
                mHeadStates.warnErrorText.set(status)

                mHeadStates.xAngle.set(
                    DeviceStatusInfoProcessor.formatDoubleValue(
                        stateInfo.x_Angle,
                        "--",
                        2
                    ) + "°"
                )

                mHeadStates.yAngle.set(
                    DeviceStatusInfoProcessor.formatDoubleValue(
                        stateInfo.y_Angle,
                        "--",
                        2
                    ) + "°"
                )

                mHeadStates.zAngle.set(
                    DeviceStatusInfoProcessor.formatDoubleValue(
                        stateInfo.z_Angle,
                        "--",
                        2
                    ) + "°"
                )
            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    override fun createObserver() {
        super.createObserver()
        setupHeartbeat()
        if (communicateWay is NetPlatformConnect) {
            checkDeviceOnlineStatus()
        }
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
                        Timber.Forest.d("发送心跳包指令 startTime: ${TimeUtils.getNowString()}，lastUpdateTime：$updateTime")
                        val command = IOTCommandUtil.getCommand(IOTCommandType.HEART_BEAT)
                        Timber.Forest.d("发送心跳包指令: $command")
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
                        // 如果设备在线状态发生变化，更新UI
                        if (deviceDetailInfo.deviceInfo.onlineStatus != lastOnlineStatus) {
                            onNetPlatformReady()
                        }
                    }
                } catch (e: Exception) {
                    Timber.Forest.e(e)
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

}