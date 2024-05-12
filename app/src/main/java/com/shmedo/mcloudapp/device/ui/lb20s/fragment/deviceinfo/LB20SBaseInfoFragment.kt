package com.shmedo.mcloudapp.device.ui.lb20s.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
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
            val fullJsonMap = MoshiUtil.fromJson<Map<String, Any>>(content) ?: return
            val targetData = fullJsonMap["000_1"] ?: return
            val commonCurrentStateInfo =
                MoshiUtil.fromJson<LB20SCurrentStateInfo>(targetData.toString()) ?: return

            mStates.wrapStateInfo.set(commonCurrentStateInfo)
            mStates.wrapStateInfo.get().apply {
                inner_power_volt = inner_power_volt.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
                ext_power_volt = ext_power_volt.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
                solar_current = solar_current.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "0"
                battery_current = battery_current.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "0"
                temp = temp.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
                humidity = humidity.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
                temp_out = temp_out.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
                humidity_out = humidity_out.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
                volumelevel =
                    if (volumelevel == "0") "无" else if (volumelevel == "1") "低" else if (volumelevel == "2") "中" else "高"
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