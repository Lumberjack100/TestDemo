package com.shmedo.mcloudapp.ui.page.device.m20.fragment.deviceinfo

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentM20CommunicationInfoBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.mr702.widget.tableview.CommunicationDataTableAdapter
import com.shmedo.mcloudapp.ui.page.device.mr702.widget.tableview.model.CommunicationDataCellModel
import com.shmedo.mcloudapp.ui.viewmodel.state.M20CommunicationInfoViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class M20CommunicationInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM20CommunicationInfoBinding
    private lateinit var mStates: M20CommunicationInfoViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val tableAdapter: CommunicationDataTableAdapter by lazy { CommunicationDataTableAdapter() }

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m20_communication_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM20CommunicationInfoBinding
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

        val command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        //判断是否页面是否处于 resume 状态
        if (!isResumed) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_DEVICE_STATUS
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
                        initStatusInfo(content)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
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

            mStates.starNum.set(commonCurrentStateInfo.starNum)
            mStates.amsState.set(
                if (commonCurrentStateInfo.dataCenter4 == 0) "未启用" else if (commonCurrentStateInfo.dataCenter4 == 1) "已连接" else "未连接"
            )
            mStates.signalValue.set(commonCurrentStateInfo._4g_signal.let {
                if (it <= 0)
                    it
                else
                    it * 2 - 113
            } ?: -113)
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
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
                            mData = if (commonCurrentStateInfo.dataCenter1 == 0) "未启用" else if (commonCurrentStateInfo.dataCenter1 == 1) "已连接" else "未连接",
                            textColorResId = if (commonCurrentStateInfo.dataCenter1 == 1) R.color.online_colorPrimary else R.color.offline_BABABA
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (commonCurrentStateInfo.dataCenter2 == 0) "未启用" else if (commonCurrentStateInfo.dataCenter2 == 1) "已连接" else "未连接",
                            textColorResId = if (commonCurrentStateInfo.dataCenter2 == 1) R.color.online_colorPrimary else R.color.offline_BABABA
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (commonCurrentStateInfo.dataCenter3 == 0) "未启用" else if (commonCurrentStateInfo.dataCenter3 == 1) "已连接" else "未连接",
                            textColorResId = if (commonCurrentStateInfo.dataCenter3 == 1) R.color.online_colorPrimary else R.color.offline_BABABA
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (commonCurrentStateInfo.dataCenter4 == 0) "未启用" else if (commonCurrentStateInfo.dataCenter4 == 1) "已连接" else "未连接",
                            textColorResId = if (commonCurrentStateInfo.dataCenter4 == 1) R.color.online_colorPrimary else R.color.offline_BABABA
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
        fun newInstance() = M20CommunicationInfoFragment()
    }
}