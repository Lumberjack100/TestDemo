package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.u_product.URCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentURProductSensorInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.URProductSensorInfoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc:  雨量计传感器状态
 *
 */
class URProductSensorInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentURProductSensorInfoBinding
    private lateinit var mStates: URProductSensorInfoViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val decimalFormat = DecimalFormat("#.###", DecimalFormatSymbols(Locale.getDefault()))

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_u_r_product_sensor_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentURProductSensorInfoBinding
        refreshLayout = binding.refreshLayout
        initRefresh()
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryInfo()
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询信息出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
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
        launchWithViewLifecycle {
            try {
                val urCurrentStateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<URCurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle
                mStates.batPowerVoltage.set(urCurrentStateInfo.ext_power_volt.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY)

                val uRSensorInfoList = urCurrentStateInfo.attach_data
                if (uRSensorInfoList.isNullOrEmpty()) {
                    return@launchWithViewLifecycle
                }
                uRSensorInfoList.forEach { info ->
                    when (info.key) {
                        "dayRain" -> {//24小时雨量值
                            mStates.rain24h.set(info.value.toDoubleOrNull()
                                ?.let {
                                    decimalFormat.format(it)
                                } ?: IOTConstants.NULL_KEY)
                        }

                        "worktime" -> {
                            mStates.runTime.set(info.value.toIntOrNull()?.let {
                                val day = it / (24 * 60 * 60)
                                val hour = (it % (24 * 60 * 60)) / (60 * 60)
                                val minute = (it % (60 * 60)) / 60
                                "${day}天${hour}时${minute}分"
                            } ?: IOTConstants.NULL_KEY)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    companion object {
        fun newInstance() = URProductSensorInfoFragment()
    }
}