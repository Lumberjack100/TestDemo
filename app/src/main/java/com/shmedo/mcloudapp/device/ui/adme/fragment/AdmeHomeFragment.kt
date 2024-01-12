package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeEquipModelEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeBaseInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMotionState
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentAdmeHomeBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.device.model.BasicConfigModule
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommonModule
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DeviceOperationModule
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.model.RebootModule
import com.shmedo.mcloudapp.device.model.RunningStatusModule
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.QueryDeviceDataFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2023/12/13
 * @desc: ADME 主页面
 *
 */
class AdmeHomeFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentAdmeHomeBinding by lazy { getBinding() as FragmentAdmeHomeBinding }
    private val mHeadStates: AdmeHomeViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val modeList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_device_mode) }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_adme_home, BR.stateVM, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        toolbarViewModel.toolbarIvActionVisible.set(true)
        binding.llToolbar.toolbar.title = "设备配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (bleViewModel.isConnected()) {
                showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                    mActivity.finish()
                }, "取消")
            } else
                mActivity.finish()
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (bleViewModel.isConnected()) {
                showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                    mActivity.finish()
                }, "取消")
            } else
                mActivity.finish()
        }
        initModuleAdapter()
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
//        mHeadStates.productResId.set(R.drawable.ic_adme)
        mHeadStates.deviceName.set(deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken })
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)
        mHeadStates.productName.set(deviceInfo.productName)
        mHeadStates.firmwareVersion.set(deviceInfo.firmwareVersion)

        when (communicateWay) {
            NetPlatformConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(deviceInfo.onlineStatus)
                mHeadStates.deviceStateTagText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
                mHeadStates.isConnectOperateVisible.set(false)
                mHeadStates.isPlatformConnectionStateVisible.set(false)
            }

            BleConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(false)
                mHeadStates.deviceStateTagText.set("未连接")
                mHeadStates.isConnectOperateVisible.set(true)
                mHeadStates.connectOperateText.set("蓝牙连接")
                mHeadStates.isPlatformConnectionStateVisible.set(true)
                mHeadStates.platformConnectionStateText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
            }

            else -> {}
        }
        updateConfigModuleData()
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        if (isConnected) {
            mHeadStates.isDeviceStateTagHighLight.set(true)
            mHeadStates.deviceStateTagText.set("已连接")
            mHeadStates.connectOperateText.set("断开连接")
        } else {
            mHeadStates.isDeviceStateTagHighLight.set(false)
            mHeadStates.deviceStateTagText.set("未连接")
            mHeadStates.connectOperateText.set("蓝牙连接")
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            val bundle = QueryDeviceDataFragment.newBundleArguments(
                deviceInfo.deviceToken
            )
            nav(binding.llToolbar.ivAction).navigate(
                R.id.action_admeHomeFragment_to_querydevicedata_graph, bundle
            )
        }

        override fun onConnectOperateClick() {
            if (bleViewModel.isConnected()) {
                showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                }, "取消")
            } else {
                bleViewModel.launch(bleDevice!!)
            }
        }

        fun onSwitchConfigModel() {
            val selectedIndex = modeList.indexOf(mHeadStates.mode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", modeList,
                    null, selectedIndex,
                    { position, text ->
                        //不可以主动切换到异常保护模式
                        if (text == modeList[2]) {
                            return@asBottomList
                        }
                        mHeadStates.mode.set(
                            when (position) {
                                0 -> {
                                    mMessenger.admeDeviceMode.set("0")
                                    modeList[0]
                                }

                                1 -> {
                                    mMessenger.admeDeviceMode.set("1")
                                    modeList[1]
                                }

                                else -> modeList[2]
                            }
                        )
                        updateConfigModuleData()
                        setEquipModel()
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }
    }

    private fun processItemClick(module: ConfigModule) {
        if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (module.configModule) {
            is RebootModule -> {
                showMessage("确定重启设备吗？", "温馨提示", "确定", {
                    reboot()
                }, "取消")
            }

            else -> {
                if (module.configModule.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().navigate(
                        module.configModule.navId,
                        bundle
                    )
                }
            }
        }
    }

    override fun lazyLoadData() {
        //4G 模式下，直接查询设备工作模式
        if (communicateWay is NetPlatformConnect) {
            queryEquipmentBaseInfo()
        } else {
            bleViewModel.launch(bleDevice!!)
        }
    }

    override fun onBleDeviceReady() {
        //蓝牙模式下，等蓝牙建立连接后查询设备工作模式
        queryEquipmentBaseInfo()
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryEquipmentBaseInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 获取电机的运行状态
     */
    private fun getMotorMotionData(timeMillis: Long = 0L) {
        launchWithViewLifecycle {
            delay(timeMillis)

            commandItems.clear()
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.ADME_MD_GET_MOTION_STATE
            )
            commandItems.add(command)
            sendCommandFromCmdList(isStartTimeoutJob = true)
        }
    }

    private fun setEquipModel() {
        val entity = AdmeEquipModelEntity(
            when (mHeadStates.mode.get()) {
                modeList[0] -> "0"
                modeList[1] -> "1"
                else -> "2"
            }
        )
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_EQUIPMENT_MODEL,
            entity.toCommandString()
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS -> {//获取设备的基本信息
                val result =
                    iotParseManager.parse<AdmeBaseInfo>(
                        cmdStr,
                        IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS
                    )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取设备的基本信息出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            //获取设备的运行状态
                            getMotorMotionData()
                        }
                        val baseInfo: AdmeBaseInfo = result.data
                        mHeadStates.mode.set(
                            when (baseInfo.equimodel) {
                                "0" -> modeList[0]
                                "1" -> modeList[1]
                                else -> modeList[2]
                            }
                        )
                        mMessenger.admeDeviceMode.set(baseInfo.equimodel)
                        updateConfigModuleData()
                    }
                }
            }

            IOTCommandType.ADME_MD_GET_MOTION_STATE -> {//获取ADME的运行状态
                val result = iotParseManager.parse<AdmeMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取设备的运行状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            //获取设备的运行状态
                            getMotorMotionData(20000)
                        }
                        updateMotionState(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_EQUIPMENT_MODEL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置设备模式出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.REBOOT -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = StringUtils.getString(R.string.reboot_failed) + result.message
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show(StringUtils.getString(R.string.device_reboot_tip))
                        }
                    }
                }
            }

            IOTCommandType.MD_SAVE_CONFIG_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "保存出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }

            else -> {}
        }
    }

    /**
     * 刷新电机运动状态
     */
    private fun updateMotionState(admeMotionState: AdmeMotionState) {
        when (admeMotionState.motionstate) {
            "0" -> mHeadStates.runningStateText.set("管口停止")
            "1" -> mHeadStates.runningStateText.set("管底停止")
            "2" -> mHeadStates.runningStateText.set("管口测量")
            "3" -> mHeadStates.runningStateText.set("管口测试")
            "4" -> mHeadStates.runningStateText.set("上拉测量")
            "5" -> mHeadStates.runningStateText.set("上拉测试")
            "6" -> mHeadStates.runningStateText.set("下放测量")
            "7" -> mHeadStates.runningStateText.set("下放测试")
            else -> {}
        }
    }

    private fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(RunningStatusModule(navId = R.id.action_admeHomeFragment_to_admeCurrentStateFragment))
        )
        when (mHeadStates.mode.get()) {
            modeList[0] -> {//设备配置模式
                moduleList.add(
                    ConfigModule(BasicConfigModule(navId = R.id.action_admeHomeFragment_to_admeBasicParamConfigFragment))
                )
                if (communicateWay is BleConnect) {
                    moduleList.add(
                        ConfigModule(
                            CommonModule(
                                name = "孔深测量",
                                desc = "测量测斜管深度",
                                resID = R.drawable.ic_measuring_hole_depth,
                                navId = R.id.action_global_to_admeMeasuringHoleDepthFragment
                            )
                        )
                    )
                    moduleList.add(
                        ConfigModule(
                            CommonModule(
                                name = "指令下发",
                                desc = "自定义指令下发",
                                resID = R.drawable.ic_device_instruction_send,
                                navId = 0
                            )
                        )
                    )
                }
                moduleList.add(
                    ConfigModule(
                        DeviceOperationModule(
                            desc = "计米轮、测斜议、执行机构等",
                            navId = R.id.action_global_to_admeAdvancedConfigurationFragment
                        )
                    )
                )
                moduleList.add(
                    ConfigModule(RebootModule())
                )
                moduleList.add(
                    ConfigModule(AdvancedSettingsModule(navId = R.id.action_global_to_admeAdvancedSettingFragment))
                )
            }

            modeList[1] -> {//自动监测模式
                moduleList.add(
                    ConfigModule(BasicConfigModule(navId = R.id.action_admeHomeFragment_to_admeBasicParamConfigFragment))
                )
                moduleList.add(
                    ConfigModule(
                        DeviceOperationModule(
                            desc = "计米轮、测斜议、执行机构等",
                            navId = R.id.action_global_to_admeAdvancedConfigurationFragment
                        )
                    )
                )
                moduleList.add(
                    ConfigModule(RebootModule())
                )
                moduleList.add(
                    ConfigModule(AdvancedSettingsModule(navId = R.id.action_global_to_admeAdvancedSettingFragment))
                )
            }
        }
        binding.rvModule.models = moduleList
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}