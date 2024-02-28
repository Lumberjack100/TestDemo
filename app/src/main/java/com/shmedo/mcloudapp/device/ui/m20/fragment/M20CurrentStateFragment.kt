package com.shmedo.mcloudapp.device.ui.m20.fragment

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.CommonCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.databinding.FragmentM20CurrentStateBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
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
    private lateinit var binding: FragmentM20CurrentStateBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: M20CurrentStateViewModel
    private val iotParseManager: IOTParserManager by inject()



    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m20_current_state, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM20CurrentStateBinding
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
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
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
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result =
                    iotParseManager.parse<String>(cmdStr, IOTCommandType.QUERY_DEVICE_STATUS)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询设备状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList{
                            binding.refreshLayout.finish()
                        }
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
            val commonCurrentStateInfo = MoshiUtil.fromJson<CommonCurrentStateInfo>(content) ?: return
            mStates.wrapStateInfo.set(commonCurrentStateInfo)

            var sensorAbnormal = false
            commonCurrentStateInfo.sensor_errno?.forEach { errnoBean ->
                sensorAbnormal = errnoBean.errno != 0
            }
            binding.tvSensorStatus.text = if (sensorAbnormal) "未接入" else "正常"
            binding.tvSensorStatus.setTextColor(
                if (sensorAbnormal) ColorUtils.getColor(R.color.device_offline_platform)
                else ColorUtils.getColor(R.color.device_online_platform)
            )

            binding.tvInclination.text = String.format(
                "%s°,%s°,%s°",
                commonCurrentStateInfo.x_Angle,
                commonCurrentStateInfo.y_Angle,
                commonCurrentStateInfo.z_Angle
            )
            binding.tvInternalVoltage.text =
                String.format("%sV", commonCurrentStateInfo.inner_power_volt)
            binding.tvExternalVoltage.text =
                String.format("%sV", commonCurrentStateInfo.ext_power_volt)
            binding.tvSolarPanelVoltage.text = String.format("%sV", commonCurrentStateInfo.solar_volt)
            binding.tvAmbientTemperature.text = String.format("%s℃", commonCurrentStateInfo.temp)
            binding.tvAmbientHumidity.text = String.format("%s%%", commonCurrentStateInfo.humidity)
            binding.tvSupplementaryPower.text =
                String.format("%sV", commonCurrentStateInfo.supply_power)
            binding.tvPowerConsumption.text =
                String.format("%sV", commonCurrentStateInfo.consume_power)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}