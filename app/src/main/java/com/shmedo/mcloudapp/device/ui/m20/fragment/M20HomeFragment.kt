package com.shmedo.mcloudapp.device.ui.m20.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.communicate.service.base.ConnectedResult
import com.shmedo.lib.ble.communicate.service.base.ConnectingResult
import com.shmedo.lib.ble.communicate.service.base.DisconnectedResult
import com.shmedo.lib.ble.communicate.service.base.IdleResult
import com.shmedo.lib.ble.communicate.service.base.LinkLossResult
import com.shmedo.lib.ble.communicate.service.base.MissingServiceResult
import com.shmedo.lib.ble.communicate.service.base.SuccessResult
import com.shmedo.lib.ble.communicate.service.base.UnknownErrorResult
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.device.base.iot_cmd.IOTCommandManager
import com.shmedo.lib.device.base.iot_cmd.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParseManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTStringUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.dismissWaitDialog
import com.shmedo.mcloudapp.common.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.ext.showWaitDialog
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentM20HomeBinding
import com.shmedo.mcloudapp.device.CmdDispatch
import com.shmedo.mcloudapp.device.NoDeviceState
import com.shmedo.mcloudapp.device.WorkingState
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.model.QueryCmdResult
import com.shmedo.mcloudapp.device.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.device.viewmodel.request.IOTCommandViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.M20HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class M20HomeFragment : BaseFragment() {
    private val binding: FragmentM20HomeBinding by lazy { getBinding() as FragmentM20HomeBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: M20HomeViewModel by viewModels()
    private val iotCommandViewModel: IOTCommandViewModel by viewModels()
    private val bleViewModel: BleViewModel by activityViewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }
    private var statusBarColor = 0

    private var communicateWay: CommunicateWay = NetPlatformConnect
    private lateinit var deviceInfo: DeviceInfo
    private var bleDevice: DiscoveredBluetoothDevice? = null

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m20_home, BR.vm, mStates)
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
            }
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            if (mStates.isBleConnected.get()) {
                showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                    bleViewModel.disconnect()
                    mActivity.finish()
                }, "取消")
            }
        }
        initModuleAdapter()
        binding.llDeviceInfo.tvDeviceConnectOperate.setOnClickListener {
            onConnectOperateClick()
        }
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
        arguments?.let {
            communicateWay = it.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
        mStates.deviceName.set(deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken })
        mStates.deviceToken.set(String.format("设备SN号：%s", deviceInfo.deviceToken))
        mStates.productName.set(String.format("所属产品：%s", deviceInfo.productName))
        mStates.firmwareVersion.set(String.format("固件版本：%s", deviceInfo.firmwareVersion))

        when (communicateWay) {
            NetPlatformConnect -> {
                mStates.isDeviceStateHighLight.set(deviceInfo.onlineStatus)
                mStates.deviceState.set(if (deviceInfo.onlineStatus) "在线" else "离线")
                mStates.isConnectOperateVisible.set(false)
                mStates.isExtendedField3Visible.set(false)
                mStates.isPlatformConnectionStateVisible.set(false)
            }

            BleConnect -> {
                mStates.isDeviceStateHighLight.set(false)
                mStates.deviceState.set("未连接")
                mStates.connectOperate.set("蓝牙连接")
                mStates.isConnectOperateVisible.set(true)
                mStates.isExtendedField3Visible.set(false)
                mStates.isPlatformConnectionStateVisible.set(true)
            }

            else -> {}
        }
    }

    override fun createObserver() {
        if (communicateWay is NetPlatformConnect) {
            processNetPlatform()
        } else {
            processBle()
        }
    }

    private fun processNetPlatform() {
        launchAndRepeatWithViewLifecycle {
            iotCommandViewModel.cmdDispatchFlow.collectLatest {
                when (it) {
                    is CmdDispatch.DispatchFailed -> {
                        dismissWaitDialog()
                        doDispatchFailed(it.cmdStr, it.errorMsg)
                    }

                    is CmdDispatch.DispatchSuccess -> {
                        doDispatchSuccess(it.cmdStr)
                    }

                    is CmdDispatch.CmdResponseResultError -> {
                        dismissWaitDialog()
                        Toaster.show("指令响应错误: ${it.errorMsg}")
                    }

                    is CmdDispatch.CmdResponseResultTimeOut -> {
                        dismissWaitDialog()
                        Toaster.show("指令响应超时")
                    }

                    is CmdDispatch.CmdResponseResultSuccess -> {
                        dismissWaitDialog()
                        setResultData(it.cmdResult)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun processBle() {
        launchAndRepeatWithViewLifecycle {
            bleViewModel.state.collectLatest { state ->
                Timber.d("Medo BluetoothGatt: $state")
                when (state) {
                    NoDeviceState -> {}
                    is WorkingState -> when (state.result) {
                        is IdleResult,
                        is ConnectingResult -> {
                            showWaitDialog(StringUtils.getString(R.string.ble_state_connecting))
                        }

                        is ConnectedResult -> {
                            dismissWaitDialog()
                        }

                        is SuccessResult -> {
                            dismissWaitDialog()
                            mStates.isDeviceStateHighLight.set(true)
                            mStates.deviceState.set("已连接")
                            mStates.connectOperate.set("断开连接")
                            mStates.isBleConnected.set(true)
                        }

                        is DisconnectedResult -> {
                            dismissWaitDialog()
                            mStates.isBleConnected.set(false)
                        }

                        is LinkLossResult -> {
                            dismissWaitDialog()
                            mStates.isBleConnected.set(false)
                        }

                        is MissingServiceResult -> {
                            dismissWaitDialog()
                            mStates.isBleConnected.set(false)
                        }

                        is UnknownErrorResult -> {
                            dismissWaitDialog()
                        }

                    }
                }
            }
        }
        bleViewModel.launch(bleDevice!!)
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryBaseInfo() {
        val command: String =
            IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        showWaitDialog("处理中...")
        iotCommandViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
    }

    private fun processItemClick(moduleName: String) {
        when (moduleName) {
            "设置向导" -> {
                queryBaseInfo()
            }

            "状态" -> {

            }

            "数据中心" -> {

            }

            "设置" -> {

            }
        }
    }

    private fun onConnectOperateClick() {
        if (mStates.isBleConnected.get()) {
            showMessage(StringUtils.getString(R.string.disconnect_device), "温馨提示", "确定", {
                bleViewModel.disconnect()
            }, "取消")
        } else {
            bleViewModel.launch(bleDevice!!)
        }
    }

    private fun doDispatchFailed(cmdStr: String, errorMsg: String) {
        when (IOTStringUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> Toaster.show("下发指令失败: $errorMsg")
            IOTCommandType.M20_MD_LEVEL_INITIAL -> {

            }

            else -> {}
        }
    }

    private fun doDispatchSuccess(cmdStr: String) {
        when (IOTStringUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                iotCommandViewModel.processCmdResult()
            }

            IOTCommandType.M20_MD_LEVEL_INITIAL -> {

            }

            else -> {}
        }
    }

    private fun setResultData(queryCmdResult: QueryCmdResult) {
        val cmdStr: String = queryCmdResult.responseContent
        when (IOTStringUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val commandResult: IOTCommandResult<String> =
                    IOTParseManager.instance.parse<String>(cmdStr)
                if (!commandResult.isSuccess) {
                    val errMsg =
                        java.lang.String.format("%s %s", "查询设备状态出错!", commandResult.message)
                    Timber.e(errMsg)
                    Toaster.show(errMsg)
                    return
                }
                val content: String = commandResult.result!!
                Toaster.show(content)
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