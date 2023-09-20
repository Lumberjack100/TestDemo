package com.shmedo.mcloudapp.device.ui.m20.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
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
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.IOTCommandManager
import com.shmedo.lib.device.base.iot_cmd.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.m20.M20CurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParseManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTStringUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.dismissWaitDialog
import com.shmedo.mcloudapp.common.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.databinding.FragmentM20CurrentStateBinding
import com.shmedo.mcloudapp.device.CmdResponseResultError
import com.shmedo.mcloudapp.device.CmdResponseResultSuccess
import com.shmedo.mcloudapp.device.CmdResponseResultTimeOut
import com.shmedo.mcloudapp.device.DispatchFailed
import com.shmedo.mcloudapp.device.DispatchSuccess
import com.shmedo.mcloudapp.device.NoDeviceState
import com.shmedo.mcloudapp.device.WorkingState
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.device.viewmodel.request.NetIOTCommandViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.M20CurrentStateViewModel
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class M20CurrentStateFragment : BaseFragment() {
    private val binding: FragmentM20CurrentStateBinding by lazy { getBinding() as FragmentM20CurrentStateBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: M20CurrentStateViewModel by viewModels()
    private val netIotCommandViewModel: NetIOTCommandViewModel by viewModels()
    private val bleViewModel: BleViewModel by activityViewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }
    private var statusBarColor = 0

    private var communicateWay: CommunicateWay = NetPlatformConnect
    private lateinit var deviceInfo: DeviceInfo
    private var bleDevice: DiscoveredBluetoothDevice? = null


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m20_current_state, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "运行状态"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
        initRefresh()
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            queryStatusInfo()
        }
    }

    override fun initData() {
        arguments?.let {
            communicateWay = it.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
    }

    override fun createObserver() {
        if (communicateWay is NetPlatformConnect) {
            processNetPlatform()
        } else {
            processBle()
        }
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryStatusInfo() {
        val command: String =
            IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        if (communicateWay is NetPlatformConnect) {
            netIotCommandViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
        } else {
            bleViewModel.sendCommand(command, true, deviceInfo.apiKey)
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun processNetPlatform() {
        launchAndRepeatWithViewLifecycle {
            netIotCommandViewModel.cmdDispatchFlow.collectLatest {
                when (it) {
                    is DispatchFailed -> {
                        dismissWaitDialog()
                        doDispatchFailed(it.cmdStr, it.errorMsg)
                    }

                    is DispatchSuccess -> {
                        doDispatchSuccess(it.cmdStr)
                    }

                    is CmdResponseResultError -> {
                        dismissWaitDialog()
                        Toaster.show("指令响应错误: ${it.errorMsg}")
                    }

                    is CmdResponseResultTimeOut -> {
                        dismissWaitDialog()
                        Toaster.show("指令响应超时")
                    }

                    is CmdResponseResultSuccess -> {
                        dismissWaitDialog()
                        setResultData(it.cmdResult.responseContent)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun processBle() {
        launchAndRepeatWithViewLifecycle {
            bleViewModel.state.collectLatest { state ->
                when (state) {
                    NoDeviceState -> {}
                    is WorkingState -> when (state.result) {
                        is IdleResult,
                        is ConnectingResult -> {
                            val ss = "33333"
                        }

                        is ConnectedResult -> {
                            val ss = "33333"
                        }

                        is SuccessResult -> {
                            setResultData(state.result.data.response)
                        }

                        is DisconnectedResult -> {
                            val ss = "33333"
                        }

                        is LinkLossResult -> {
                            val ss = "33333"
                        }

                        is MissingServiceResult -> {
                            val ss = "33333"
                        }

                        is UnknownErrorResult -> {
                            val ss = "33333"
                        }

                    }
                }
            }
        }
    }

    private fun doDispatchFailed(cmdStr: String, errorMsg: String) {
        when (IOTStringUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> Toaster.show("下发指令失败: $errorMsg")

            else -> {}
        }
    }

    private fun doDispatchSuccess(cmdStr: String) {
        when (IOTStringUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                netIotCommandViewModel.processCmdResult()
            }

            else -> {}
        }
    }

    private fun setResultData(cmdStr: String) {
        when (IOTStringUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                binding.refreshLayout.finish()
                val commandResult: IOTCommandResult<String> = IOTParseManager.instance.parse<String>(cmdStr)
                if (!commandResult.isSuccess) {
                    val errMsg =
                        java.lang.String.format("%s %s", "查询设备状态出错!", commandResult.message)
                    Timber.e(errMsg)
                    Toaster.show(errMsg)
                    return
                }
                val content: String = commandResult.result!!
                initStatusInfo(content)
            }

            else -> {}
        }
    }

    private fun initStatusInfo(content: String) {
        try {
            val m20CurrentStateInfo = MoshiUtil.fromJson<M20CurrentStateInfo>(content) ?: return
            mStates.wrapStateInfo.set(m20CurrentStateInfo)

            var sensorAbnormal = false
            m20CurrentStateInfo.sensor_errno?.forEach { errnoBean ->
                sensorAbnormal = errnoBean.errno != 0
            }
            binding.tvSensorStatus.text = if (sensorAbnormal) "未接入" else "正常"
            binding.tvSensorStatus.setTextColor(
                if (sensorAbnormal) ColorUtils.getColor(R.color.device_offline_platform)
                else ColorUtils.getColor(R.color.device_online_platform)
            )

            binding.tvInclination.text = String.format(
                "%s°,%s°,%s°",
                m20CurrentStateInfo.x_Angle,
                m20CurrentStateInfo.y_Angle,
                m20CurrentStateInfo.z_Angle
            )
            binding.tvInternalVoltage.text =
                String.format("%sV", m20CurrentStateInfo.inner_power_volt)
            binding.tvExternalVoltage.text =
                String.format("%sV", m20CurrentStateInfo.ext_power_volt)
            binding.tvSolarPanelVoltage.text = String.format("%sV", m20CurrentStateInfo.solar_volt)
            binding.tvAmbientTemperature.text = String.format("%s℃", m20CurrentStateInfo.temp)
            binding.tvAmbientHumidity.text = String.format("%s%%", m20CurrentStateInfo.humidity)
            binding.tvSupplementaryPower.text =
                String.format("%sV", m20CurrentStateInfo.supply_power)
            binding.tvPowerConsumption.text =
                String.format("%sV", m20CurrentStateInfo.consume_power)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
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