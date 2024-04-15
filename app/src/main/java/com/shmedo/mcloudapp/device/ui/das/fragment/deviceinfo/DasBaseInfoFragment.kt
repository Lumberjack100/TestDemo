package com.shmedo.mcloudapp.device.ui.das.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.das.DasBaseInfo
import com.shmedo.lib.device.base.iot_cmd.model.das.DasSolarStatusInfo
import com.shmedo.lib.device.base.iot_cmd.model.das.DasTemperatureAndHumidityStatusinfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentDasBaseInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasBaseInfoViewModel
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class DasBaseInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasBaseInfoBinding
    private lateinit var mStates: DasBaseInfoViewModel
    private val iotParseManager: IOTParserManager by inject()

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_das_base_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasBaseInfoBinding
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

        var command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_DEVICE_BASE)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_SOLAR_STATUS)
        commandItems.add(command)

        command =
            IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_TEMPERATURE_AND_HUMIDITY_STATUS)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_DEVICE_BASE -> {
                val result = iotParseManager.parse<DasBaseInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_DEVICE_BASE
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
                        initBaseInfo(result.data)
                    }
                }
            }

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
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
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

    private fun initBaseInfo(baseInfo: DasBaseInfo) {
        val decimalFormat = DecimalFormat("#.#")
        try {
            mStates.wrapBaseInfo.get().apply {
                sn = deviceInfo.deviceToken
                iccid = baseInfo.iccid
                imei = baseInfo.imei
                code = baseInfo.code
                ver = baseInfo.ver
                local = baseInfo.local
                involt = baseInfo.involt
                outvolt = baseInfo.outvolt.toDoubleOrNull()?.let {
                    decimalFormat.format(it)
                } ?: ""
            }
            mStates.wrapBaseInfo.notifyChange()

//            mStates.signal.set(String.format("%sdBm", baseInfo.csq))
            mStates.signal.set(baseInfo.csq.toIntOrNull()?.let {
                (it * 2 - 113).toString() + "dBm"
            } ?: "--dBm"
            )
            mStates.signalValue.set(baseInfo.csq.toIntOrNull()?.let {
                it * 2 - 113
            } ?: -113)
        } catch (e: Exception) {
            e.printStackTrace()
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
            e.printStackTrace()
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
            e.printStackTrace()
        }
    }

    companion object {
        fun newInstance() = DasBaseInfoFragment()
    }
}