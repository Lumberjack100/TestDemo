package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinterface

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRCollectionParam
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRSensorStatus
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs4851InterfaceBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.MRSensorItem
import com.shmedo.mcloudapp.device.viewmodel.state.MR702InterfaceHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS4851InterfaceViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MR702RS4851InterfaceFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702Rs4851InterfaceBinding by lazy { getBinding() as FragmentMr702Rs4851InterfaceBinding }
    private val mInterfaceHomeViewModel: MR702InterfaceHomeViewModel by activityViewModels()
    private val mStates: MR702RS4851InterfaceViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_rs485_1_interface, BR.vm, mStates)
            .addBindingParam(BR.homeVM, mInterfaceHomeViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRefresh()
        initSensorAdapter()
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            queryInfo()
        }
    }

    private fun initSensorAdapter() {
        binding.rv.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(8f),
                    false
                )
            )
            addType<MRSensorItem>(R.layout.item_mr702_interface_sensor_item)
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onSubmitClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()

    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
        testData()
    }

    private fun queryInfo() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_485_PORT1_COLL)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_485_PORT1_SENSOR, "index=0")
        commandItems.add(command)
        sendCommandFromCmdList(isShowLoadingDialog = false)
    }

    override fun cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog: Boolean) {
        super.cancelNearbyCommunicationTimeoutJob(false)
        binding.refreshLayout.finish(false)
    }

    override fun showNearbyCommunicationTimeoutAlert(
        isDismissLoadingDialog: Boolean,
        isShowMsg: Boolean,
        msg: String
    ) {
        Toaster.show("发送指令超时,请稍后尝试")
        binding.refreshLayout.finish(false)
    }

    override fun doNetDispatchFailed(cmdStr: String, errorMsg: String) {
        Toaster.show("下发指令失败: $errorMsg")
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        netIotCommandViewModel.processCmdResult()
    }

    override fun doCmdResponseResultError(errorMsg: String) {
        Toaster.show("指令响应错误: $errorMsg")
        binding.refreshLayout.finish(false)
    }

    override fun doCmdResponseResultTimeOut(errorMsg: String) {
        Toaster.show("指令响应超时: $errorMsg")
        binding.refreshLayout.finish(false)
    }

    override fun setResultData(cmdStr: String) {
        if (viewLifecycleOwner.lifecycle.currentState < androidx.lifecycle.Lifecycle.State.RESUMED) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_485_PORT1_COLL -> {
                val result = iotParseManager.parse<MRCollectionParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_485_PORT1_COLL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询采集参数出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList(isShowLoadingDialog = false)
                        initCollectionData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_GET_485_PORT1_SENSOR -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_485_PORT1_SENSOR
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询传感器出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList(isShowLoadingDialog = false)
                        binding.refreshLayout.finish()
                        initSensorData(result.data)
                    }
                }
            }

            else -> {}
        }
    }

    private fun initCollectionData(collectionParam: MRCollectionParam) {
        mStates.acquisitionFrequency.set(collectionParam.collfreq)
        mStates.collectionDuration.set(collectionParam.collcycle)
        mStates.collectionInterval.set(collectionParam.collgap)
        mStates.noResponseTimes.set(collectionParam.noresp)
    }

    private fun initSensorData(content: String) {
        lifecycleScope.launch {
            try {
                val sensorStatusList =
                    MoshiUtil.fromJson<List<MRSensorStatus>>(content) ?: return@launch
                val list = sensorStatusList.map { sensorStatus ->
                    var sensorItem = MRSensorItem(
                        sensorStatus.sta == "0",
                        "雷达水位计",
                        "214",
                        "1",
                    )
                }
                binding.rv.models = sensorStatusList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        fun newInstance() = MR702RS4851InterfaceFragment()
    }

    private fun testData() {
        val list = mutableListOf<MRSensorItem>()
        list.add(
            MRSensorItem(
                true,
                "雷达水位计",
                "214",
                "1",
            )
        )
        list.add(
            MRSensorItem(
                true,
                "雷达水位计",
                "214",
                "2",
            )
        )
        list.add(
            MRSensorItem(
                true,
                "雷达水位计",
                "214",
                "3",
            )
        )
        list.add(
            MRSensorItem(
                false,
                "雷达水位计",
                "214",
                "4",
            )
        )
        binding.rv.models = list
    }
}