package com.shmedo.mcloudapp.device.ui.mr702.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.setup
import com.google.android.flexbox.FlexboxLayoutManager
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.IOTCommandManager
import com.shmedo.lib.device.base.iot_cmd.entity.WorkModeEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.WorkModeBean
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParseManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.ext.showWaitDialog
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMR702HomeBinding
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.model.PlatformLable
import com.shmedo.mcloudapp.device.ui.m20.fragment.M20CurrentStateFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702HomeViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702HomeFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMR702HomeBinding by lazy { getBinding() as FragmentMR702HomeBinding }
    override val mHeadStates: MR702HomeViewModel by viewModels()
    private val iotParseManager: IOTParseManager by inject()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m_r702_home, BR.vm, mHeadStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "设备配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (mHeadStates.isBleConnected.get()) {
                showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                    mActivity.finish()
                }, "取消")
            } else
                mActivity.finish()
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (mHeadStates.isBleConnected.get()) {
                showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                    mActivity.finish()
                }, "取消")
            } else
                mActivity.finish()
        }
        initPlatformAdapter()
        initModuleAdapter()
        initClickListener()
    }

    private fun initPlatformAdapter() {
        binding.llDeviceInfo.rvPlatform.setup { rv ->
            rv.layoutManager = FlexboxLayoutManager(context)
            addType<PlatformLable>(R.layout.item_platform_label)
        }.models = testData()
    }

    private fun testData(): List<PlatformLable> {
        return listOf(
            PlatformLable("淘宝", true),
            PlatformLable("微信"),
            PlatformLable("QQ"),
            PlatformLable("UC浏览器"),
            PlatformLable("京东"),
        )
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
                processItemClick(module.name)
            }
        }.models = getModuleList()
    }

    override fun initData() {
        super.initData()
        mHeadStates.deviceName.set(deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken })
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)
        mHeadStates.productName.set(deviceInfo.productName)
        mHeadStates.firmwareVersion.set(deviceInfo.firmwareVersion)

        when (communicateWay) {
            NetPlatformConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(deviceInfo.onlineStatus)
                mHeadStates.deviceStateTagText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
                mHeadStates.isConnectOperateVisible.set(false)
                mHeadStates.isExtendedField3Visible.set(false)
                mHeadStates.isPlatformConnectionStateVisible.set(false)
            }

            BleConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(false)
                mHeadStates.deviceStateTagText.set("未连接")
                mHeadStates.connectOperateText.set("蓝牙连接")
                mHeadStates.isConnectOperateVisible.set(true)
                mHeadStates.isExtendedField3Visible.set(false)
                mHeadStates.isPlatformConnectionStateVisible.set(true)
            }

            else -> {}
        }
    }

    private fun initClickListener() {
        binding.llDeviceInfo.tvDeviceConnectOperate.setOnClickListener {
            onConnectOperateClick()
        }
    }

    private fun onConnectOperateClick() {
        if (mHeadStates.isBleConnected.get()) {
            showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                bleViewModel.disconnect()
            }, "取消")
        } else {
            showWaitDialog(StringUtils.getString(R.string.ble_state_connecting))
            bleViewModel.launch(bleDevice!!)
        }
    }

    inner class ClickProxy {
        fun onNormalClick() {
            if (mHeadStates.isWorkModeNormal.get())
                return
            if (communicateWay is BleConnect && !mHeadStates.isBleConnected.get()) {
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
            if (communicateWay is BleConnect && !mHeadStates.isBleConnected.get()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定切换到低功耗模式吗？", "温馨提示", "确定", {
                mHeadStates.isWorkModeNormal.set(false)
                setWorkMode("2")
            }, "取消")
        }
    }

    private fun processItemClick(moduleName: String) {
        when (moduleName) {
            "关于设备" -> {
                val bundle =
                    M20CurrentStateFragment.newBundleArguments(
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                nav().navigate(R.id.action_m20HomeFragment_to_m20CurrentStateFragment, bundle)
            }

            "数据中心" -> {
                queryWorkMode()
            }

            "接口配置" -> {

            }

            "终端参数" -> {

            }

            "设备操作" -> {

            }

            "网络与通信" -> {

            }
        }
    }

    override fun lazyLoadData() {
        //4G 模式下，直接查询设备工作模式
        if (communicateWay is NetPlatformConnect) {
            queryWorkMode()
        }
    }

    override fun onDeviceConnected() {
        //蓝牙模式下，等蓝牙建立连接后查询设备工作模式
        queryWorkMode()
    }

    /**
     * 查询设备工作模式
     */
    private fun queryWorkMode() {
        val command: String =
            IOTCommandManager.getInstance().getCommand(IOTCommandType.GET_WORK_MODE)
        if (communicateWay is NetPlatformConnect) {
            netIotCommandViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
        } else {
            bleViewModel.sendCommand(command, true, deviceInfo.apiKey)
        }
    }

    /**
     * 设置设备工作模式
     */
    private fun setWorkMode(mode: String) {
        val entity = WorkModeEntity(mode)
        val command: String =
            IOTCommandManager.getInstance().getCommand(IOTCommandType.SET_WORK_MODE, entity)
        if (communicateWay is NetPlatformConnect) {
            netIotCommandViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
        } else {
            bleViewModel.sendCommand(command, true, deviceInfo.apiKey)
        }
    }

    override fun doDispatchFailed(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.GET_WORK_MODE -> {

            }

            else -> {}
        }
    }

    override fun doDispatchSuccess(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.GET_WORK_MODE -> {

            }

            else -> {}
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.GET_WORK_MODE -> {
                val result =
                    iotParseManager.parse<WorkModeBean>(cmdStr, IOTCommandType.GET_WORK_MODE)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备工作模式出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        val modeBean: WorkModeBean = result.data
                        mHeadStates.isWorkModeNormal.set(modeBean.mode == "1")
                    }
                }
            }

            IOTCommandType.SET_WORK_MODE -> {
                val result =
                    iotParseManager.parse<CommonSettingCmdResult>(
                        cmdStr,
                        IOTCommandType.COMMON_SETTING_COMMAND
                    )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置设备工作模式出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        Toaster.show("设置成功")
                    }
                }
            }

            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    private fun getModuleList() =
        arrayListOf<ConfigModule>().apply {
            add(
                ConfigModule(
                    R.drawable.ic_device_running_info,
                    "关于设备",
                    "设备基本信息、运行数据"
                )
            )
            add(
                ConfigModule(
                    R.drawable.ic_device_terminal_param,
                    "数据中心",
                    "连接平台设置"
                )
            )
            add(
                ConfigModule(
                    R.drawable.ic_device_sensor_config,
                    "接口配置",
                    "串口、ADC、DI、DO配置"
                )
            )
            add(
                ConfigModule(
                    R.drawable.ic_device_data_center,
                    "终端参数",
                    "本机触摸屏和上报规则设置"
                )
            )

            add(
                ConfigModule(
                    R.drawable.ic_device_setting,
                    "设备操作",
                    "时间校准、人工置数、召测等"
                )
            )

            add(
                ConfigModule(
                    R.drawable.ic_device_net_communicate,
                    "网络与通信",
                    "无线、有线配置"
                )
            )
        }

    companion object {
        fun newBundleArguments(
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}