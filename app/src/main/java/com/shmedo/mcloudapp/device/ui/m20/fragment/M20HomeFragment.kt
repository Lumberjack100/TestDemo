package com.shmedo.mcloudapp.device.ui.m20.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParseManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentM20HomeBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.state.M20HomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

class M20HomeFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentM20HomeBinding by lazy { getBinding() as FragmentM20HomeBinding }
    private val mHeadStates: M20HomeViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParseManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m20_home, BR.vm, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
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
        binding.recyclerview.setup { rv ->
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
        mHeadStates.deviceToken.set(String.format("设备编号：%s", deviceInfo.deviceToken))
        mHeadStates.productName.set(String.format("产品型号：%s", deviceInfo.productName))
        mHeadStates.firmwareVersion.set(String.format("固件版本：%s", deviceInfo.firmwareVersion))

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

    override fun createObserver() {
        super.createObserver()
        if (communicateWay is BleConnect) {
            bleViewModel.launch(bleDevice!!)
        }
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        if(isConnected){
            mHeadStates.isDeviceStateTagHighLight.set(true)
            mHeadStates.deviceStateTagText.set("已连接")
            mHeadStates.connectOperateText.set("断开连接")
        }else{
            mHeadStates.isDeviceStateTagHighLight.set(false)
            mHeadStates.deviceStateTagText.set("未连接")
            mHeadStates.connectOperateText.set("蓝牙连接")
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onConnectOperateClick() {
            if (bleViewModel.isConnected()) {
                showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                }, "取消")
            } else {
                bleViewModel.launch(bleDevice!!)
            }
        }
    }

    private fun processItemClick(moduleName: String) {
        when (moduleName) {
            "设置向导" -> {

            }

            "状态" -> {
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

            "设置" -> {

            }
        }
    }

    override fun onBleDeviceReady() {

    }

    override fun doDispatchFailed(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.M20_MD_LEVEL_INITIAL -> {
                Toaster.show("下发指令失败")
            }

            else -> {}
        }
    }

    override fun doDispatchSuccess(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.M20_MD_LEVEL_INITIAL -> {
                netIotCommandViewModel.processCmdResult()
            }

            else -> {}
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.M20_MD_LEVEL_INITIAL -> {

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
                    R.drawable.ic_setup_wizard,
                    "设置向导",
                    "一键配置"
                )
            )
            add(
                ConfigModule(
                    R.drawable.ic_device_current_state,
                    "状态",
                    "获取当前设备状态"
                )
            )
            add(
                ConfigModule(
                    R.drawable.ic_device_data_center,
                    "数据中心",
                    "基础参数配置"
                )
            )
            add(
                ConfigModule(
                    R.drawable.ic_device_setting,
                    "设置",
                    "高级设置"
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