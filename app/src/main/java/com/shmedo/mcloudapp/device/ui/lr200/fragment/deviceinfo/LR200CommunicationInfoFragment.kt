package com.shmedo.mcloudapp.device.ui.lr200.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.CommonCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.FragmentLr200CommunicationInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.CommunicationDataTableAdapter
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.model.CommunicationDataCellModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class LR200CommunicationInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLr200CommunicationInfoBinding
    private lateinit var mStates: EmptyViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val tableAdapter: CommunicationDataTableAdapter by lazy { CommunicationDataTableAdapter() }

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_lr200_communication_info, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLr200CommunicationInfoBinding
        refreshLayout = binding.refreshLayout
        initRefresh()
        initCommunicateDataTableView()
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

    private fun initCommunicateDataTableView() {
        binding.tableview.isIgnoreSelectionColors = true
        binding.tableview.setAdapter(tableAdapter)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

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
                        val errMsg = "查询通讯状态出错: ${result.message}"
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

            tableAdapter.setAllItems(
                getColumnHeaderList(),
                getRowHeaderList(),
                getCellDataList(commonCurrentStateInfo)
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun getColumnHeaderList(): ArrayList<CommunicationDataCellModel> {
        val columnHeaderList = arrayListOf<CommunicationDataCellModel>()
        for (i in 1..centerNum) {
            val columnHeader = CommunicationDataCellModel("平台$i")
            columnHeaderList.add(columnHeader)
        }
        return columnHeaderList
    }

    private fun getRowHeaderList(): ArrayList<CommunicationDataCellModel> {
        return arrayListOf(
            CommunicationDataCellModel("数据状态"),
        )
    }

    private fun getCellDataList(commonCurrentStateInfo: CommonCurrentStateInfo): MutableList<MutableList<CommunicationDataCellModel>> {
        val cellDataList: MutableList<MutableList<CommunicationDataCellModel>> = arrayListOf()
        getRowHeaderList().forEach { headerText ->
            val columnCellDataList = arrayListOf<CommunicationDataCellModel>()
            when (headerText.mData) {
                "数据状态" -> {
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (commonCurrentStateInfo.dataCenter1 == 0) "未开启" else if (commonCurrentStateInfo.dataCenter1 == 1) "在线" else "离线",
                            textColorResId = if (commonCurrentStateInfo.dataCenter1 == 1) R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (commonCurrentStateInfo.dataCenter2 == 0) "未开启" else if (commonCurrentStateInfo.dataCenter2 == 1) "在线" else "离线",
                            textColorResId = if (commonCurrentStateInfo.dataCenter2 == 1) R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (commonCurrentStateInfo.dataCenter3 == 0) "未开启" else if (commonCurrentStateInfo.dataCenter3 == 1) "在线" else "离线",
                            textColorResId = if (commonCurrentStateInfo.dataCenter3 == 1) R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (commonCurrentStateInfo.dataCenter4 == 0) "未开启" else if (commonCurrentStateInfo.dataCenter4 == 1) "在线" else "离线",
                            textColorResId = if (commonCurrentStateInfo.dataCenter4 == 1) R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                }
            }
            cellDataList.add(columnCellDataList)
        }

        return cellDataList
    }

    companion object {
        const val centerNum = 4
        fun newInstance() = LR200CommunicationInfoFragment()
    }
}