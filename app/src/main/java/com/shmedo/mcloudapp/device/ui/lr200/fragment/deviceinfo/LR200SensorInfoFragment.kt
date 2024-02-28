package com.shmedo.mcloudapp.device.ui.lr200.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.MonitoringType
import com.shmedo.lib.device.base.iot_cmd.model.CommonCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentLr200SensorInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.LR200SensorInfoViewModel
import org.koin.android.ext.android.inject

class LR200SensorInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLr200SensorInfoBinding
    private lateinit var mStates: LR200SensorInfoViewModel
    private val iotParseManager: IOTParserManager by inject()

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_lr200_sensor_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLr200SensorInfoBinding
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
                        val errMsg = "查询传感器状态出错: ${result.message}"
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
            if (commonCurrentStateInfo.sensor_errno.isNullOrEmpty()) {
                mStates.isMEMSSensorVisible.set(false)
                return
            }
            mStates.isMEMSSensorVisible.set(true)
            commonCurrentStateInfo.sensor_errno?.let { errnoList ->
                mStates.memsErrNo.set(errnoList[0].errno)
                mStates.name.set(MonitoringType.valueByCode(errnoList[0].sensor_id).description)
                mStates.memsAxisY.set(commonCurrentStateInfo.x_Angle.toString())
                mStates.memsAxisX.set(commonCurrentStateInfo.y_Angle.toString())
                mStates.memsAxisZ.set(commonCurrentStateInfo.z_Angle.toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun newInstance() = LR200SensorInfoFragment()
    }

}