package com.shmedo.mcloudapp.device.ui.lb20s.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.formatDoubleValue
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.lb20s.LB20SCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentLb20sBaseInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.LB20SBaseInfoViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2024/5/12
 * @desc: 预警广播
 *
 */
class LB20SBaseInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLb20sBaseInfoBinding
    private lateinit var mStates: LB20SBaseInfoViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.getDefault()))


    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_lb20s_base_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLb20sBaseInfoBinding
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
            queryBaseInfo()
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryBaseInfo() {
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
                        val errMsg = "查询基本信息出错: ${result.message}"
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
        try {
            val dataMap = MoshiUtil.fromJson<Map<String, LB20SCurrentStateInfo>>(content) ?: return
            val commonCurrentStateInfo = dataMap["000_1"] ?: return

            val volumeLevelStr = commonCurrentStateInfo.attach_data?.get("volumelevel") ?: "0"
            mStates.wrapStateInfo.set(commonCurrentStateInfo)
            mStates.wrapStateInfo.get().apply {
                ext_power_volt = formatDoubleValue(
                    ext_power_volt.toDoubleOrNull(),
                    decimalFormat,
                    IOTConstants.NULL_KEY
                )
                solar_volt =
                    formatDoubleValue(solar_volt.toDoubleOrNull(), decimalFormat, "0")
                battery_volt =
                    formatDoubleValue(battery_volt.toDoubleOrNull(), decimalFormat, "0")
                temp =
                    formatDoubleValue(temp.toDoubleOrNull(), decimalFormat, IOTConstants.NULL_KEY)
                humidity = formatDoubleValue(
                    humidity.toDoubleOrNull(),
                    decimalFormat,
                    IOTConstants.NULL_KEY
                )
                temp_out = formatDoubleValue(
                    temp_out.toDoubleOrNull(),
                    decimalFormat,
                    IOTConstants.NULL_KEY
                )
                humidity_out = formatDoubleValue(
                    humidity_out.toDoubleOrNull(),
                    decimalFormat,
                    IOTConstants.NULL_KEY
                )
                volumelevel =
                    if (volumeLevelStr == "0") "无" else if (volumeLevelStr == "1") "低" else if (volumeLevelStr == "2") "中" else if (volumeLevelStr == "3") "高" else "无"
            }
            mStates.wrapStateInfo.notifyChange()

            mStates.signalValue.set(commonCurrentStateInfo._4g_signal.let {
                if (it <= 0)
                    it
                else
                    it * 2 - 113
            })
            mStates.bdSinalValue.set(commonCurrentStateInfo.bd_signal.let {
                if (it <= 0)
                    it
                else
                    it * 2 - 113
            })


        } catch (e: Exception) {
            Timber.e(e)
        }
    }


    companion object {
        fun newInstance() = LB20SBaseInfoFragment()
    }
}