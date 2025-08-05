package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.cmd.base.iot_cmd.model.lb20s.LB20SCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentCommonCommunicationInfoBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.CommonCommunicationInfoViewModel
import com.shmedo.mcloudapp.ui.widget.tableview.CommunicationDataTableAdapter
import com.shmedo.mcloudapp.ui.widget.tableview.model.CommunicationDataCellModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

@Deprecated("This class is deprecated", ReplaceWith(""))
class CommonCommunicationInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentCommonCommunicationInfoBinding
    private val mStates: CommonCommunicationInfoViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val tableAdapter: CommunicationDataTableAdapter by lazy { CommunicationDataTableAdapter() }
    private var centerNum = 0//数据链路数量

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_common_communication_info,
            BR.stateVM,
            mStates
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentCommonCommunicationInfoBinding
        refreshLayout = binding.refreshLayout
        initRefresh()
        initCommunicateDataTableView()
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            centerNum = it.getInt(CENTER_NUM_PARAM, 4)
        }
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
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

        val command = if (productType == ProductType.LB20S) IOTCommandUtil.getCommand(
            IOTCommandType.QUERY_DEVICE_STATUS
        ) else IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_DEVICE_STATUS
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        //判断是否页面是否处于 resume 状态
        if (!isResumed) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
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
                        if (productType == ProductType.LB20S) {
                            initLB20SStatusInfo(content)
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val commonCurrentStateInfoList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<CommonCurrentStateInfo2>>(content)
                } ?: return@launchWithViewLifecycle

                if (commonCurrentStateInfoList.isEmpty()) {
                    Toaster.show("数据为空")
                    return@launchWithViewLifecycle
                }
                val info = commonCurrentStateInfoList[0]
                if (info.dataCenterUseSta != IOTConstants.NULL_KEY && info.dataCenterStatus != IOTConstants.NULL_KEY && info.dataCenterUseSta.isNotEmpty() && info.dataCenterStatus.isNotEmpty()) {
                    //根据逗号分隔
                    val enableStatusList =
                        info.dataCenterUseSta.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    val onlineStatusList =
                        info.dataCenterStatus.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    tableAdapter.setAllItems(
                        getColumnHeaderList(enableStatusList),
                        getRowHeaderList(),
                        getCellDataList(enableStatusList, onlineStatusList)
                    )
                }

                info.csq.notNullKey {
                    val temp = it.toIntOrNull() ?: 0
                    mStates.signalValue.set(
                        if (temp <= 0)
                            temp
                        else
                            temp * 2 - 113
                    )
                }
                info.signal.notNullKey {
                    val temp = it.toIntOrNull() ?: 0
                    mStates.signalValue.set(
                        if (temp <= 0)
                            temp
                        else
                            temp * 2 - 113
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun initLB20SStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val dataMap = MoshiUtil.fromJson<Map<String, LB20SCurrentStateInfo>>(content)
                    ?: return@launchWithViewLifecycle
                val info = dataMap["000_1"] ?: return@launchWithViewLifecycle

                //根据逗号分隔
                val enableStatusList = ArrayList<String>()
                for (i in 1..centerNum)
                    enableStatusList.add("1")

                if (info.datacenterStatus != IOTConstants.NULL_KEY && info.datacenterStatus.isNotEmpty()) {
                    //根据逗号分隔
                    val onlineStatusList =
                        info.datacenterStatus.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    tableAdapter.setAllItems(
                        getColumnHeaderList(enableStatusList),
                        getRowHeaderList(),
                        getCellDataList(enableStatusList, onlineStatusList)
                    )
                }
                mStates.signalValue.set(info._4g_signal.let {
                    if (it <= 0)
                        it
                    else
                        it * 2 - 113
                })
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun getColumnHeaderList(centerStatusList: List<String>): ArrayList<CommunicationDataCellModel> {
        val columnHeaderList = arrayListOf<CommunicationDataCellModel>()
        for (i in 1..centerStatusList.size.coerceAtMost(centerNum)) {
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

    private fun getCellDataList(
        enableStatusList: List<String>,
        onlineStatusList: List<String>
    ): MutableList<MutableList<CommunicationDataCellModel>> {
        if (enableStatusList.isEmpty()) {
            return arrayListOf()
        }
        val cellDataList: MutableList<MutableList<CommunicationDataCellModel>> = arrayListOf()
        getRowHeaderList().forEach { headerText ->
            val columnCellDataList = arrayListOf<CommunicationDataCellModel>()
            when (headerText.mData) {
                "数据状态" -> {
                    for (i in 0..<enableStatusList.size.coerceAtMost(centerNum)) {
                        columnCellDataList.add(
                            CommunicationDataCellModel(
                                mData = if (enableStatusList[i] == "0") "未启用" else if (onlineStatusList[i] == "1") "已连接" else "未连接",
                                textColorResId = if (enableStatusList[i] == "1" && onlineStatusList[i] == "1") R.color.online_colorPrimary else R.color.offline_BABABA
                            )
                        )
                    }
                }
            }
            cellDataList.add(columnCellDataList)
        }

        return cellDataList
    }

    companion object {
        private const val CENTER_NUM_PARAM = "center_num_param"

        fun newBundleArguments(
            centerNum: Int = 1,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putInt(CENTER_NUM_PARAM, centerNum)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }

    }
}