package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentLr200BaseInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.LR200BaseInfoViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class LR200BaseInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLr200BaseInfoBinding
    private lateinit var mStates: LR200BaseInfoViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.getDefault()))


    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_lr200_base_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLr200BaseInfoBinding
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
            val commonCurrentStateInfo =
                MoshiUtil.fromJson<CommonCurrentStateInfo>(content) ?: return
            mStates.wrapStateInfo.set(commonCurrentStateInfo)
            mStates.wrapStateInfo.get().apply {
                volt_percent = volt_percent.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: IOTConstants.NULL_KEY
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
            }
            mStates.wrapStateInfo.notifyChange()

            mStates.signalValue.set(commonCurrentStateInfo._4g_signal.let {
                if (it <= 0)
                    it
                else
                    it * 2 - 113
            })
            mStates.isLoRaExist.set(
                commonCurrentStateInfo.self_check.uppercase().contains("LORA:1")
            )

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        fun newInstance() = LR200BaseInfoFragment()
    }
}