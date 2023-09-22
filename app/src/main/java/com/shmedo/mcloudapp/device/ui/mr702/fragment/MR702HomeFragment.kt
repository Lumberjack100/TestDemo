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
import com.shmedo.lib.device.base.iot_cmd.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.entity.adme.AdmeWorkModeEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParseManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTStringUtil
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
import timber.log.Timber

class MR702HomeFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMR702HomeBinding by lazy { getBinding() as FragmentMR702HomeBinding }
    override val mStates: MR702HomeViewModel by viewModels()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m_r702_home, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "设备配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (mStates.isBleConnected.get()) {
                showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                    mActivity.finish()
                }, "取消")
            } else
                mActivity.finish()
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (mStates.isBleConnected.get()) {
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
        mStates.deviceName.set(deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken })
        mStates.deviceToken.set(deviceInfo.deviceToken)
        mStates.productName.set(deviceInfo.productName)
        mStates.firmwareVersion.set(deviceInfo.firmwareVersion)

        when (communicateWay) {
            NetPlatformConnect -> {
                mStates.isDeviceStateTagHighLight.set(deviceInfo.onlineStatus)
                mStates.deviceStateTagText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
                mStates.isConnectOperateVisible.set(false)
                mStates.isExtendedField3Visible.set(false)
                mStates.isPlatformConnectionStateVisible.set(false)
            }

            BleConnect -> {
                mStates.isDeviceStateTagHighLight.set(false)
                mStates.deviceStateTagText.set("未连接")
                mStates.connectOperateText.set("蓝牙连接")
                mStates.isConnectOperateVisible.set(true)
                mStates.isExtendedField3Visible.set(false)
                mStates.isPlatformConnectionStateVisible.set(true)
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
        if (mStates.isBleConnected.get()) {
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
            if (mStates.isWorkModeNormal.get())
                return

            if (communicateWay is BleConnect && !mStates.isBleConnected.get()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定切换到正常模式吗？", "温馨提示", "确定", {
                mStates.isWorkModeNormal.set(true)

            }, "取消")
        }

        fun onLowPowerClick() {
            if (!mStates.isWorkModeNormal.get())
                return
            if (communicateWay is BleConnect && !mStates.isBleConnected.get()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定切换到低功耗模式吗？", "温馨提示", "确定", {
                mStates.isWorkModeNormal.set(false)

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

    /**
     * 设置设备工作模式
     */
    private fun setWorkMode() {
        val entity = AdmeWorkModeEntity()
        entity.workmode
        val command: String =
            IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_WORK_MODE, entity)
        sendCommand(command)
    }

    override fun doDispatchFailed(cmdStr: String, errorMsg: String) {
        when (IOTStringUtil.extractCommandType(cmdStr)) {
            IOTCommandType.M20_MD_LEVEL_INITIAL -> {

            }

            else -> {}
        }
    }

    override fun doDispatchSuccess(cmdStr: String) {
        when (IOTStringUtil.extractCommandType(cmdStr)) {
            IOTCommandType.M20_MD_LEVEL_INITIAL -> {

            }

            else -> {}
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTStringUtil.extractCommandType(cmdStr)) {
            IOTCommandType.M20_MD_LEVEL_INITIAL -> {
                val commandResult: IOTCommandResult<String> =
                    IOTParseManager.instance.parse<String>(cmdStr)
                if (!commandResult.isSuccess) {
                    val errMsg = String.format("%s %s", "查询设备状态出错!", commandResult.message)
                    Timber.e(errMsg)
                    Toaster.show(errMsg)
                    return
                }
                val content: String = commandResult.result!!
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