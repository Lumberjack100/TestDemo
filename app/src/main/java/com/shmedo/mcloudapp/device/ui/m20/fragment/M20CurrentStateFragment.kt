package com.shmedo.mcloudapp.device.ui.m20.fragment

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.m20.M20CurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.databinding.FragmentM20CurrentStateBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.state.M20CurrentStateViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：   运行状态页面
 */
class M20CurrentStateFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentM20CurrentStateBinding by lazy { getBinding() as FragmentM20CurrentStateBinding }
    private val mStates: M20CurrentStateViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m20_current_state, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "运行状态"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
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

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryStatusInfo() {
        val command: String =
            IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        if (communicateWay is NetPlatformConnect) {
            netIotCommandViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
        } else {
            bleViewModel.sendCommand(command, true, deviceInfo.apiKey)
        }
    }

    override fun doNetDispatchFailed(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> Toaster.show("下发指令失败: $errorMsg")

            else -> {}
        }
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                netIotCommandViewModel.processCmdResult()
            }

            else -> {}
        }
    }

    override fun showTimeoutAlert() {
        // 关闭 loading 框并显示超时警告
        binding.refreshLayout.finish(false)
        Toaster.show("发送指令超时,请稍后尝试")
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                binding.refreshLayout.finish()
                val result =
                    iotParseManager.parse<String>(cmdStr, IOTCommandType.QUERY_DEVICE_STATUS)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        val content: String = result.data
                        initStatusInfo(content)
                    }
                }
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
}