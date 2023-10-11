package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinfo

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRCommunicationData
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRunningData
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMr702RunningStatusInfoBinding
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.MRRunningDataItem
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.CommunicationDataTableAdapter
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.model.CommunicationDataCellModel
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DeviceInfoViewModel
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class MR702RunningStatusInfoFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702RunningStatusInfoBinding by lazy { getBinding() as FragmentMr702RunningStatusInfoBinding }
    private val mStates: MR702DeviceInfoViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val tableAdapter: CommunicationDataTableAdapter by lazy { CommunicationDataTableAdapter() }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_running_status_info, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRefresh()
        initCommunicateDataTableView()
        initRunningDataAdapter()
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            queryInfo()
        }
    }

    private fun initCommunicateDataTableView() {
        binding.tableview.setAdapter(tableAdapter)
    }

    private fun initRunningDataAdapter() {
        binding.rvRunningData.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(10f),
                    false
                )
            )
            addType<MRRunningDataItem>(R.layout.item_mr702_device_info_running_data)
        }
    }

    override fun lazyLoadData() {
//        binding.refreshLayout.autoRefresh()

        testTableData()
        testRunningData()
    }

    private fun queryInfo() {
        commandItems.clear()

        var entity = MRDeviceInfoEntity(pages = 2, label = 1)
        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)

        entity = MRDeviceInfoEntity(pages = 2, label = 2)
        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO, entity)
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
            IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO -> {
                val result = iotParseManager.parse<MRDeviceInfo>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询运行状态出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList(isShowLoadingDialog = false)
                        if (result.data.label == "1") {
                            initCommunicationData(result.data.communicationData)
                        } else {
                            binding.refreshLayout.finish()
                            initRunningData(result.data.runningData)
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun initCommunicationData(communicationData: MRCommunicationData) {

    }

    private fun initRunningData(runningData: MRRunningData) {
        try {
            val list = mutableListOf<MRRunningDataItem>()
            list.add(
                MRRunningDataItem(
                    "运行时长(小时)",
                    runningData.ttime
                )
            )
            list.add(
                MRRunningDataItem(
                    "单次运行时长(小时)",
                    runningData.otime
                )
            )
            list.add(
                MRRunningDataItem(
                    "重启次数",
                    runningData.rebootn
                )
            )
            if (runningData.ustorage.isEmpty() || runningData.tstorage.isEmpty()) {
                list.add(
                    MRRunningDataItem(
                        "存储状态",
                        "--%",
                        "已用--GB/--GB"
                    )
                )
            } else {
                val decimalFormat = DecimalFormat("#.#")
                val ustorage = decimalFormat.format(runningData.ustorage.toDouble())
                val tstorage = decimalFormat.format(runningData.tstorage.toDouble())
                val percent = decimalFormat.format(ustorage.toDouble() / tstorage.toDouble() * 100)
                list.add(
                    MRRunningDataItem(
                        "存储状态",
                        "${percent}%",
                        "已用${ustorage}GB/${tstorage}GB"
                    )
                )
            }
            binding.rvRunningData.models = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun newInstance() = MR702RunningStatusInfoFragment()
    }

    private fun testTableData() {
        val columnHeaderList = arrayListOf<CommunicationDataCellModel>()
        val rowHeaderList = arrayListOf(
            CommunicationDataCellModel("数据状态"),
            CommunicationDataCellModel("网络协议"),
            CommunicationDataCellModel("发送数据"),
            CommunicationDataCellModel("未发数据"),
            CommunicationDataCellModel("人工置数"),
            CommunicationDataCellModel("在线率"),
        )
        val cellDataList: MutableList<MutableList<CommunicationDataCellModel>> = arrayListOf()

        for (i in 1..7) {
            val columnHeader = CommunicationDataCellModel("平台$i")
            columnHeaderList.add(columnHeader)
        }

        for (i in 1..6) {
            val list = arrayListOf<CommunicationDataCellModel>()
            when (i) {

                1 -> {
                    for (j in 1..7) {
                        list.add(CommunicationDataCellModel(if (j % 2 == 0) "在线" else "离线"))
                    }
                }

                2 -> {
                    for (j in 1..7) {
                        list.add(CommunicationDataCellModel(if (j % 2 == 0) "IPV4" else "IPV6"))
                    }
                }

                3 -> {
                    for (j in 1..7) {
                        list.add(CommunicationDataCellModel(if (j % 2 == 0) "100000" else "6000"))
                    }
                }

                4 -> {
                    for (j in 1..7) {
                        list.add(CommunicationDataCellModel(if (j % 2 == 0) "10" else "6"))
                    }
                }

                5 -> {
                    for (j in 1..7) {
                        list.add(CommunicationDataCellModel(if (j % 2 == 0) "10" else "45"))
                    }
                }

                6 -> {
                    for (j in 1..7) {
                        list.add(CommunicationDataCellModel(if (j % 2 == 0) "100%" else "0%"))
                    }
                }
            }
            cellDataList.add(list)
        }

        tableAdapter.setAllItems(columnHeaderList, rowHeaderList, cellDataList)
    }

    private fun testRunningData() {
        val list = mutableListOf<MRRunningDataItem>()
        list.add(
            MRRunningDataItem(
                "运行时长(小时)",
                "100"
            )
        )
        list.add(
            MRRunningDataItem(
                "单次运行时长(小时)",
                "72"
            )
        )
        list.add(
            MRRunningDataItem(
                "重启次数",
                "2"
            )
        )
        list.add(
            MRRunningDataItem(
                "存储状态",
                "32.5%",
                "已用5.2GB/16GB"
            )
        )
        binding.rvRunningData.models = list
    }
}