package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

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
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.baseclickproxy.DoubleClickListener
import com.shmedo.mcloudapp.databinding.FragmentMr702HomeBinding
import com.shmedo.mcloudapp.databinding.ItemSubConfigModuleBinding
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
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702HomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：  MR702 遥测终端机配置主页
 */
class MR702HomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702HomeBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mHeadStates: MR702HomeViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()
    private val iotParseManager: IOTParserManager by inject()

    private var lastOnlineStatus: Boolean = false//在线状态
    private var deviceStatusCheckJob: Job? = null


    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_home, BR.stateVM, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702HomeBinding
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
            mHeadStates.productLogoResId.set(R.drawable.device_logo_mr702)
            mHeadStates.iotPlatformStateText.set("蓝牙已连接")
            toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_ble_disconnect)

        } else {
            mHeadStates.productLogoResId.set(R.drawable.device_logo_mr702_offline)
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
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    ConfigModule(
                        CommonModule(
                            name = "基本信息",
                            resID = R.drawable.ic_module_basic_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_mR702BaseInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "网络信息",
                            resID = R.drawable.ic_module_net_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_mR702NetInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "状态信息",
                            resID = R.drawable.ic_module_state_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_mR702StatusInfoFragment
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
        val configModuleTree = ConfigModuleTree(
            configModules = arrayListOf(
                ConfigModule(
                    CommonModule(
                        name = "网络配置",
                        resID = R.drawable.ic_module_network_setting,
                        navId = R.id.action_global_to_mR702NetworkConfigFragment
                    )
                ),
                ConfigModule(
                    DataCenterModule(
                        name = "链路配置",
                        resID = R.drawable.ic_module_datacenter_new,
                        navId = R.id.action_global_to_mR702DataCenterHomeFragment
                    )
                ),

                ConfigModule(
                    CommonModule(
                        name = "上报配置",
                        resID = R.drawable.ic_module_work_mode_new,
                        navId = R.id.action_global_to_mR702ReportConfigFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "端口配置",
                        resID = R.drawable.ic_module_serial_port,
                        navId = R.id.action_global_to_mR702PortHomeFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "设备操作",
                        resID = R.drawable.ic_module_equip_operation,
                        navId = R.id.action_global_to_mR702EquipmentOperationFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "时间校准",
                        resID = R.drawable.ic_module_time_calibration_new,
                        navId = R.id.action_global_to_time_calibration
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "系统配置",
                        resID = R.drawable.ic_module_system_setting,
                        navId = R.id.action_global_to_advancedSettingFragment
                    )
                ),
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
            mHeadStates.productLogoResId.set(R.drawable.device_logo_mr702)
            mHeadStates.iotPlatformStateText.set("米度平台在线")
            queryStatusInfo()
        } else {
            mHeadStates.productLogoResId.set(R.drawable.device_logo_mr702_offline)
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

    private fun queryStatusInfo() {

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