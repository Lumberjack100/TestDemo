package com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.deviceinfo

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasNetStatusInfo
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.model.common.DeviceNetStatus
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentDasCommunicationInfoBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.mr702.widget.tableview.CommunicationDataTableAdapter
import com.shmedo.mcloudapp.ui.page.device.mr702.widget.tableview.model.CommunicationDataCellModel
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 */
class BleDasCommunicationInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasCommunicationInfoBinding
    private lateinit var mStates: EmptyViewModel
    private val mdParseManager: MDParserManager by inject()
    private val tableAdapter: CommunicationDataTableAdapter by lazy { CommunicationDataTableAdapter() }
    private val netStatusInfoList: MutableList<DasNetStatusInfo> = mutableListOf()
    private val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))

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
            netStatusInfoList.clear()
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

        /**
         * 查询数据链路网络状态 ##044n\r\n<br/>
         * 应答:$$044n,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)\r\n<br/>
         * 注：n取值1，2，3<br/>
         * （1）已发送数据<br/>
         * （2）已生成数据<br/>
         * （3）flash使能，取值0,1，1表示使能，0表示未使能<br/>
         * （4）flash读指针<br/>
         * （5）flash写指针<br/>
         * （6）链路使能：取值0,1，1表示使能，0表示未使能<br/>
         * （7）链路状态：取值0,1，1表示已上线，0表示未上线<br/>
         * （8）4G模块状态：<br/>
         * （9）MQTT状态：	<br/>
         * （10）在线率，单位%<br/>
         * 示例：$$0441,7,7,1,0x001000D4,0x001000D4,1,1,4,7,93.6
         */
        for (i in 1..3) {
            val command = MDCommandUtil.getCommand(MDCommandType.QUERY_NETWORK_STATUS, i.toString())
            commandItems.add(command)
        }

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        //判断是否页面是否处于 resume 状态
        if (!isResumed) {
            return
        }
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.QUERY_NETWORK_STATUS -> {
                val result = mdParseManager.parse<DeviceNetStatus>(
                    cmdStr,
                    MDCommandType.QUERY_NETWORK_STATUS
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询数据链路状态错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDataCenterStatus(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initDataCenterStatus(info: DeviceNetStatus) {
        decimalFormat.applyPattern("#.#")
        try {
            netStatusInfoList.add(
                DasNetStatusInfo(
                    index = info.linkNumber.toInt(),
                    errno = if (info.linkEnable == "0") 0 else if (info.linkStatus == "1") 1 else 2,
                    send = info.sentData.toInt(),
                    unsend = info.generatedData.toInt() - info.sentData.toInt(),
                    rate = info.onlineRate.toFloatOrNull() ?: 0f
                )
            )
            if (info.linkNumber.toInt() == CENTER_NUM) {
                tableAdapter.setAllItems(
                    getColumnHeaderList(),
                    getRowHeaderList(),
                    getCellDataList(netStatusInfoList)
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun getColumnHeaderList(): ArrayList<CommunicationDataCellModel> {
        val columnHeaderList = arrayListOf<CommunicationDataCellModel>()
        for (i in 1..CENTER_NUM) {
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
                            textColorResId = if (dataList[0].errno.toString() == "1") R.color.online_colorPrimary else R.color.error_FF4400
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (dataList[1].errno.toString() == "0") "未开启" else if (dataList[1].errno.toString() == "1") "已连接" else "未连接",
                            textColorResId = if (dataList[1].errno.toString() == "1") R.color.online_colorPrimary else R.color.error_FF4400
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            mData = if (dataList[2].errno.toString() == "0") "未开启" else if (dataList[2].errno.toString() == "1") "已连接" else "未连接",
                            textColorResId = if (dataList[2].errno.toString() == "1") R.color.online_colorPrimary else R.color.error_FF4400
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
                            R.color.error_FF4400
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            dataList[1].unsend.toString(),
                            R.color.error_FF4400
                        )
                    )
                    columnCellDataList.add(
                        CommunicationDataCellModel(
                            dataList[2].unsend.toString(),
                            R.color.error_FF4400
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
        const val CENTER_NUM = 3
        fun newInstance() = BleDasCommunicationInfoFragment()
    }

}