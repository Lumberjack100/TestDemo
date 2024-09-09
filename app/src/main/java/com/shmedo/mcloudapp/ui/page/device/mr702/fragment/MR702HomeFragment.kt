package com.shmedo.mcloudapp.ui.page.device.mr702.fragment

import android.os.Bundle
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.flexbox.FlexboxLayoutManager
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.WorkModeEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.WorkModeBean
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDataCenterStatus
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702HomeBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceOperationModule
import com.shmedo.mcloudapp.model.MR702PortConfigModule
import com.shmedo.mcloudapp.model.MR702TerminalParameterModule
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.model.NetworkCommunicationModule
import com.shmedo.mcloudapp.model.PlatformLabel
import com.shmedo.mcloudapp.model.RebootModule
import com.shmedo.mcloudapp.model.RunningStatusModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.QueryDeviceDataFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702HomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import kotlinx.coroutines.flow.debounce
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：   配置主页
 */
class MR702HomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702HomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mHeadStates: MR702HomeViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mHeadStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_home, BR.stateVM, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702HomeBinding
        binding.llToolbar.toolbar.title = "设备配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
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
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
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

    private fun initModuleAdapter() {
        binding.rvModule.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(15f),
                    false
                )
            )
            addType<ConfigModule>(R.layout.item_device_config_module)
            R.id.item.onClick {
                val module = getModel<ConfigModule>()
                processItemClick(module)
            }
        }
    }

    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionVisible.set(true)
        mHeadStates.productLightResId.set(R.drawable.ic_mr702)
        mHeadStates.productGrayResId.set(R.drawable.ic_mr702)
        mHeadStates.productLogoResId.set(mHeadStates.productLightResId.get())
        mHeadStates.productName.set(deviceInfo.productName)
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)
        mHeadStates.deviceName.set(if (deviceInfo.deviceName == deviceInfo.deviceToken) deviceInfo.productToken else deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken })
        mHeadStates.firmwareVersion.set(deviceInfo.firmwareVersion)

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
        updateConfigModuleData()
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

        fun onNormalClick() {
            if (mHeadStates.isWorkModeNormal.get())
                return
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定切换到正常模式吗？", "温馨提示", "确定", {
                mHeadStates.isWorkModeNormal.set(true)
                setWorkMode("1")
            }, "取消")
        }

        fun onLowPowerClick() {
            if (!mHeadStates.isWorkModeNormal.get())
                return
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定切换到低功耗模式吗？", "温馨提示", "确定", {
                mHeadStates.isWorkModeNormal.set(false)
                setWorkMode("2")
            }, "取消")
        }
    }

    private fun processItemClick(module: ConfigModule) {
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (module.functionModule) {
            is RebootModule -> {
                showMessage("确定重启设备吗？", "温馨提示", "确定", {
                    reboot()
                }, "取消")
            }

            else -> {
                if (module.functionModule.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().navigate(
                        module.functionModule.navId,
                        bundle
                    )
                }
            }
        }
    }

    override fun lazyLoadData() {
        //4G 模式下，直接查询设备工作模式
        if (communicateWay is NetPlatformConnect) {
            queryData()
        } else {
            bleViewModel.launch(bleDevice!!)
        }
    }

    override fun onBleDeviceReady() {
        super.onBleDeviceReady()
        launchWithViewLifecycle {
//            delay(2000) //延迟 timeMillis 秒后，提示超时
            //蓝牙模式下，等蓝牙建立连接后查询设备工作模式
//            queryData()
        }
    }

    /**
     *
     */
    private fun queryData() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.GET_WORK_MODE)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DATA_CENTER_STATUS)
        commandItems.add(command)
//        showLoadingDialog(StringUtils.getString(R.string.loading))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 设置设备工作模式
     */
    private fun setWorkMode(mode: String) {
        commandItems.clear()
        val entity = WorkModeEntity(mode)
        val command = IOTCommandUtil.getCommand(IOTCommandType.SET_WORK_MODE, entity)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 重启设备
     */
    private fun reboot() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.REBOOT)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        updateLastCommunicationTime()
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.GET_WORK_MODE -> {
                val result =
                    iotParseManager.parse<WorkModeBean>(cmdStr, IOTCommandType.GET_WORK_MODE)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备工作模式出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        val modeBean: WorkModeBean = result.data
                        mHeadStates.isWorkModeNormal.set(modeBean.mode == "1")
                    }
                }
            }

            IOTCommandType.MD_MR_GET_DATA_CENTER_STATUS -> {
                val result = iotParseManager.parse<MRDataCenterStatus>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DATA_CENTER_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询数据中心状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initDataCenterStatus(result.data)
                    }
                }
            }

            IOTCommandType.SET_WORK_MODE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置工作模式出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("设置成功")
                        }
                    }
                }
            }

            IOTCommandType.REBOOT -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "重启失败: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("设备即将重启")
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun initDataCenterStatus(dataCenterStatus: MRDataCenterStatus) {
        val platformLabels = mutableListOf<PlatformLabel>()
        if (dataCenterStatus.status1 != "0")
            platformLabels.add(
                PlatformLabel(
                    content = "中心1",
                    textColorRes = dataCenterStatus.status1.compareAndReturn(
                        "1",
                        ColorUtils.getColor(R.color.colorPrimary),
                        ColorUtils.getColor(R.color.sub_title_text_color)
                    ),
                    bgResId = dataCenterStatus.status1.compareAndReturn(
                        "1",
                        R.drawable.bg_label_blue_corner_15dp,
                        R.drawable.bg_label_gray_corner_15dp
                    )
                )
            )
        if (dataCenterStatus.status2 != "0")
            platformLabels.add(
                PlatformLabel(
                    "中心2", textColorRes = dataCenterStatus.status2.compareAndReturn(
                        "1",
                        ColorUtils.getColor(R.color.colorPrimary),
                        ColorUtils.getColor(R.color.sub_title_text_color)
                    ),
                    bgResId = dataCenterStatus.status2.compareAndReturn(
                        "1",
                        R.drawable.bg_label_blue_corner_15dp,
                        R.drawable.bg_label_gray_corner_15dp
                    )
                )
            )
        if (dataCenterStatus.status3 != "0")
            platformLabels.add(
                PlatformLabel(
                    "中心3", textColorRes = dataCenterStatus.status3.compareAndReturn(
                        "1",
                        ColorUtils.getColor(R.color.colorPrimary),
                        ColorUtils.getColor(R.color.sub_title_text_color)
                    ),
                    bgResId = dataCenterStatus.status3.compareAndReturn(
                        "1",
                        R.drawable.bg_label_blue_corner_15dp,
                        R.drawable.bg_label_gray_corner_15dp
                    )
                )
            )
        if (dataCenterStatus.status4 != "0")
            platformLabels.add(
                PlatformLabel(
                    "中心4", textColorRes = dataCenterStatus.status4.compareAndReturn(
                        "1",
                        ColorUtils.getColor(R.color.colorPrimary),
                        ColorUtils.getColor(R.color.sub_title_text_color)
                    ),
                    bgResId = dataCenterStatus.status4.compareAndReturn(
                        "1",
                        R.drawable.bg_label_blue_corner_15dp,
                        R.drawable.bg_label_gray_corner_15dp
                    )
                )
            )
        if (dataCenterStatus.status5 != "0")
            platformLabels.add(
                PlatformLabel(
                    "中心5", textColorRes = dataCenterStatus.status5.compareAndReturn(
                        "1",
                        ColorUtils.getColor(R.color.colorPrimary),
                        ColorUtils.getColor(R.color.sub_title_text_color)
                    ),
                    bgResId = dataCenterStatus.status5.compareAndReturn(
                        "1",
                        R.drawable.bg_label_blue_corner_15dp,
                        R.drawable.bg_label_gray_corner_15dp
                    )
                )
            )

        binding.llDeviceInfo.rvPlatform.models = platformLabels
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    private fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    "关于设备",
                    "设备基本信息、运行数据",
                    R.drawable.ic_device_running_info,
                    navId = R.id.action_mR702HomeFragment_to_mR702DeviceInfoFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(DataCenterModule(navId = R.id.action_mR702HomeFragment_to_mR702DataCenterHomeFragment))
        )
        moduleList.add(
            ConfigModule(MR702PortConfigModule(navId = R.id.action_mR702HomeFragment_to_mR702PortHomeFragment))
        )
        moduleList.add(
            ConfigModule(MR702TerminalParameterModule(navId = R.id.action_mR702HomeFragment_to_mR702TerminalParameterFragment))
        )
        moduleList.add(
            ConfigModule(DeviceOperationModule(navId = R.id.action_global_to_mR702EquipmentOperationFragment))
        )
        moduleList.add(
            ConfigModule(NetworkCommunicationModule(navId = R.id.action_mR702HomeFragment_to_mR702NetworkCommunicationFragment))
        )
        moduleList.add(
            ConfigModule(RebootModule())
        )
        moduleList.add(
            ConfigModule(AdvancedSettingsModule(navId = R.id.action_global_to_advancedSettingFragment))
        )
        if (communicateWay is BleConnect) {
            moduleList.add(
                ConfigModule(
                    CommandDebugConfigModule()
                )
            )
        }
        binding.rvModule.models = moduleList
    }

    override fun createObserver() {
        super.createObserver()
        setupHeartbeat()
    }

    // 设置心跳检查
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
}