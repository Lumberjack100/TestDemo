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
        binding.tableview.isIgnoreSelectionColors = true
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
        try {
            tableAdapter.setAllItems(
                getColumnHeaderList(),
                getRowHeaderList(),
                getCellDataList(communicationData)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getColumnHeaderList(): ArrayList<CommunicationDataCellModel> {
        val columnHeaderList = arrayListOf<CommunicationDataCellModel>()
        for (i in 1..5) {
            val columnHeader = CommunicationDataCellModel("平台$i")
            columnHeaderList.add(columnHeader)
        }
        return columnHeaderList
    }

    private fun getRowHeaderList(): ArrayList<CommunicationDataCellModel> {
        return arrayListOf(
            CommunicationDataCellModel("数据状态"),
            CommunicationDataCellModel("网络协议"),
            CommunicationDataCellModel("发送数据"),
            CommunicationDataCellModel("未发数据"),
            CommunicationDataCellModel("人工置数"),
            CommunicationDataCellModel("在线率"),
        )
    }

    private fun getCellDataList(communicationData: MRCommunicationData): MutableList<MutableList<CommunicationDataCellModel>> {
        val cellDataList: MutableList<MutableList<CommunicationDataCellModel>> = arrayListOf()
        getRowHeaderList().forEach { headerText ->
            val columnCellDataList = arrayListOf<CommunicationDataCellModel>()
            when (headerText.mData) {
                "数据状态" -> {
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (communicationData.status1 == "0") "未接入" else if (communicationData.status1 == "1") "在线" else "离线",
                            textColorResId = if (communicationData.status1 == "1") R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (communicationData.status2 == "0") "未接入" else if (communicationData.status2 == "1") "在线" else "离线",
                            textColorResId = if (communicationData.status2 == "1") R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (communicationData.status3 == "0") "未接入" else if (communicationData.status3 == "1") "在线" else "离线",
                            textColorResId = if (communicationData.status3 == "1") R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (communicationData.status4 == "0") "未接入" else if (communicationData.status4 == "1") "在线" else "离线",
                            textColorResId = if (communicationData.status4 == "1") R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (communicationData.status5 == "0") "未接入" else if (communicationData.status5 == "1") "在线" else "离线",
                            textColorResId = if (communicationData.status5 == "1") R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                }

                "网络协议" -> {
                    columnCellDataList.add(CommunicationDataCellModel(if (communicationData.agreem1 == "1") "IPV4" else "IPV6"))
                    columnCellDataList.add(CommunicationDataCellModel(if (communicationData.agreem2 == "1") "IPV4" else "IPV6"))
                    columnCellDataList.add(CommunicationDataCellModel(if (communicationData.agreem3 == "1") "IPV4" else "IPV6"))
                    columnCellDataList.add(CommunicationDataCellModel(if (communicationData.agreem4 == "1") "IPV4" else "IPV6"))
                    columnCellDataList.add(CommunicationDataCellModel(if (communicationData.agreem5 == "1") "IPV4" else "IPV6"))
                }

                "发送数据" -> {
                    columnCellDataList.add(CommunicationDataCellModel(communicationData.sdata1))
                    columnCellDataList.add(CommunicationDataCellModel(communicationData.sdata2))
                    columnCellDataList.add(CommunicationDataCellModel(communicationData.sdata3))
                    columnCellDataList.add(CommunicationDataCellModel(communicationData.sdata4))
                    columnCellDataList.add(CommunicationDataCellModel(communicationData.sdata5))
                }

                "未发数据" -> {
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            communicationData.ndata1,
                            R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            communicationData.ndata2,
                            R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            communicationData.ndata3,
                            R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            communicationData.ndata4,
                            R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            communicationData.ndata5,
                            R.color.device_offline_platform
                        )
                    )
                }

                "人工置数" -> {
                    columnCellDataList.add(CommunicationDataCellModel(communicationData.adata1))
                    columnCellDataList.add(CommunicationDataCellModel(communicationData.adata2))
                    columnCellDataList.add(CommunicationDataCellModel(communicationData.adata3))
                    columnCellDataList.add(CommunicationDataCellModel(communicationData.adata4))
                    columnCellDataList.add(CommunicationDataCellModel(communicationData.adata5))
                }

                "在线率" -> {
                    columnCellDataList.add(CommunicationDataCellModel("${communicationData.rate1}%"))
                    columnCellDataList.add(CommunicationDataCellModel("${communicationData.rate2}%"))
                    columnCellDataList.add(CommunicationDataCellModel("${communicationData.rate3}%"))
                    columnCellDataList.add(CommunicationDataCellModel("${communicationData.rate4}%"))
                    columnCellDataList.add(CommunicationDataCellModel("${communicationData.rate5}%"))
                }
            }
            cellDataList.add(columnCellDataList)
        }

        return cellDataList
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
        val communicationData = MRCommunicationData(
            status1 = "0",
            status2 = "1",
            status3 = "2",
            status4 = "1",
            status5 = "1",
            agreem1 = "1",
            agreem2 = "2",
            agreem3 = "1",
            agreem4 = "1",
            agreem5 = "1",
            sdata1 = "100",
            sdata2 = "100",
            sdata3 = "100",
            sdata4 = "100",
            sdata5 = "100",
            ndata1 = "100",
            ndata2 = "100",
            ndata3 = "100",
            ndata4 = "100",
            ndata5 = "100",
            adata1 = "100",
            adata2 = "100",
            adata3 = "100",
            adata4 = "100",
            adata5 = "100",
            rate1 = "100",
            rate2 = "100",
            rate3 = "100",
            rate4 = "100",
            rate5 = "100",
        )
        tableAdapter.setAllItems(
            getColumnHeaderList(),
            getRowHeaderList(),
            getCellDataList(communicationData)
        )
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