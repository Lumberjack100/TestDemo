package com.shmedo.mcloudapp.device.ui.das.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.das.DasSolarStatusInfo
import com.shmedo.lib.device.base.iot_cmd.model.das.DasTemperatureAndHumidityStatusinfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentDasOtherInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasOtherInfoViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat

/**
 * @author：gonghe
 * @time: 2024/2/29
 * @desc:
 *
 */
@Deprecated("")
class DasOtherInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasOtherInfoBinding
    private lateinit var mStates: DasOtherInfoViewModel
    private val iotParseManager: IOTParserManager by inject()

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_das_other_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasOtherInfoBinding
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

        var command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_SOLAR_STATUS)
        commandItems.add(command)

        command =
            IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_SOLAR_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_SOLAR_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询太阳能控制器状态出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        val content: String = result.data
                        initSolarStatus(content)
                    }
                }
            }

            IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询温湿度状态出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        val content: String = result.data
                        initTemperatureAndHumidityStatus(content)
                    }
                }
            }

            else -> {}
        }
    }

    /**
     * 太阳能控制器
     */
    private fun initSolarStatus(content: String) {
        val decimalFormat = DecimalFormat("#.#")
        try {
            val info = MoshiUtil.fromJson<DasSolarStatusInfo>(content) ?: return
            mStates.errNo.set(info.solar.errno.toString())
            mStates.solarvolt.set(decimalFormat.format(info.solar.solarvolt))
            mStates.batvolt.set(decimalFormat.format(info.solar.batvolt))
            mStates.solarpwr.set(decimalFormat.format(info.solar.solarpwr))
            mStates.loadpwr.set(decimalFormat.format(info.solar.loadpwr))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * 温湿度状态
     */
    private fun initTemperatureAndHumidityStatus(content: String) {
        val decimalFormat = DecimalFormat("#.#")
        try {
            val info = MoshiUtil.fromJson<DasTemperatureAndHumidityStatusinfo>(content) ?: return
            mStates.inthErrNo.set(info.inth.errno.toString())
            mStates.inthTemp.set(decimalFormat.format(info.inth.temp))
            mStates.inthHumi.set(decimalFormat.format(info.inth.humi))

            mStates.outthErrNo.set(info.outth.errno.toString())
            mStates.outthTemp.set(decimalFormat.format(info.outth.temp))
            mStates.outthHumi.set(decimalFormat.format(info.outth.humi))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        fun newInstance() = DasOtherInfoFragment()
    }
}