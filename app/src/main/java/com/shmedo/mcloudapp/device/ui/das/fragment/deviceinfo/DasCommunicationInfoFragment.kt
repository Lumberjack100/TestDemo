package com.shmedo.mcloudapp.device.ui.das.fragment.deviceinfo

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.das.DasNetStatusInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.FragmentDasCommunicationInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.CommunicationDataTableAdapter
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.model.CommunicationDataCellModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class DasCommunicationInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasCommunicationInfoBinding
    private lateinit var mStates: EmptyViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val tableAdapter: CommunicationDataTableAdapter by lazy { CommunicationDataTableAdapter() }


    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_das_communication_info, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasCommunicationInfoBinding
        refreshLayout = binding.refreshLayout
        initRefresh()
        initCommunicateDataTableView()
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
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

        val command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_NET_STATUS, "index=0")
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_NET_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_NET_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询通讯状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        val content: String = result.data
                        initCommunicationData(content)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initCommunicationData(content: String) {
        try {
            val dataList = MoshiUtil.fromJson<List<DasNetStatusInfo>>(content) ?: return

            tableAdapter.setAllItems(
                getColumnHeaderList(),
                getRowHeaderList(),
                getCellDataList(dataList)
            )
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
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
            CommunicationDataCellModel("发送数据"),
            CommunicationDataCellModel("未发数据"),
            CommunicationDataCellModel("在线率"),
        )
    }

    private fun getCellDataList(dataList: List<DasNetStatusInfo>): MutableList<MutableList<CommunicationDataCellModel>> {
        val cellDataList: MutableList<MutableList<CommunicationDataCellModel>> = arrayListOf()
        getRowHeaderList().forEach { headerText ->
            val columnCellDataList = arrayListOf<CommunicationDataCellModel>()
            when (headerText.mData) {
                "数据状态" -> {

                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (dataList[0].errno.toString() == "0") "未开启" else if (dataList[0].errno.toString() == "1") "已连接" else "未连接",
                            textColorResId = if (dataList[0].errno.toString() == "1") R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (dataList[1].errno.toString() == "0") "未开启" else if (dataList[1].errno.toString() == "1") "已连接" else "未连接",
                            textColorResId = if (dataList[1].errno.toString() == "1") R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (dataList[2].errno.toString() == "0") "未开启" else if (dataList[2].errno.toString() == "1") "已连接" else "未连接",
                            textColorResId = if (dataList[2].errno.toString() == "1") R.color.device_online_platform else R.color.device_offline_platform
                        )
                    )
                }

                "发送数据" -> {
                    columnCellDataList.add(CommunicationDataCellModel(dataList[0].send.toString()))
                    columnCellDataList.add(CommunicationDataCellModel(dataList[1].send.toString()))
                    columnCellDataList.add(CommunicationDataCellModel(dataList[2].send.toString()))
                }

                "未发数据" -> {
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            dataList[0].unsend.toString(),
                            R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            dataList[1].unsend.toString(),
                            R.color.device_offline_platform
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            dataList[2].unsend.toString(),
                            R.color.device_offline_platform
                        )
                    )
                }

                "在线率" -> {
                    columnCellDataList.add(CommunicationDataCellModel("${dataList[0].rate}%"))
                    columnCellDataList.add(CommunicationDataCellModel("${dataList[1].rate}%"))
                    columnCellDataList.add(CommunicationDataCellModel("${dataList[2].rate}%"))
                }
            }
            cellDataList.add(columnCellDataList)
        }

        return cellDataList
    }

    companion object {
        const val centerNum = 3
        fun newInstance() = DasCommunicationInfoFragment()
    }
}