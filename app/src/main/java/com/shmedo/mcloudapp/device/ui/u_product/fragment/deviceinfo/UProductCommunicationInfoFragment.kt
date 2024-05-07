package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentUProductCommunicationInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.CommunicationDataTableAdapter
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.model.CommunicationDataCellModel
import com.shmedo.mcloudapp.device.viewmodel.state.UProductCommunicationInfoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

class UProductCommunicationInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUProductCommunicationInfoBinding
    private lateinit var mStates: UProductCommunicationInfoViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val tableAdapter: CommunicationDataTableAdapter by lazy { CommunicationDataTableAdapter() }
    private var centerNum = 0//数据中心数量

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_u_product_communication_info,
            BR.stateVM,
            mStates
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUProductCommunicationInfoBinding
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

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
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
                    val enableStatusList = info.dataCenterUseSta.split(",")
                    val onlineStatusList = info.dataCenterStatus.split(",")
                    tableAdapter.setAllItems(
                        getColumnHeaderList(enableStatusList),
                        getRowHeaderList(),
                        getCellDataList(enableStatusList, onlineStatusList)
                    )
                }

                if (info.csq != IOTConstants.NULL_KEY) {
                    mStates.signal.set(info.csq.toIntOrNull()?.let {
                        if (it < 0)
                            it.toString() + "dBm"
                        else
                            (it * 2 - 113).toString() + "dBm"
                    } ?: "--dBm"
                    )
                    mStates.signalValue.set(info.csq.toIntOrNull()?.let {
                        if (it < 0)
                            it
                        else
                            it * 2 - 113
                    } ?: -113)
                }

                if (info.signal != IOTConstants.NULL_KEY) {
                    mStates.signal.set(info.signal.toIntOrNull()?.let {
                        if (it < 0)
                            it.toString() + "dBm"
                        else
                            (it * 2 - 113).toString() + "dBm"
                    } ?: "--dBm"
                    )
                    mStates.signalValue.set(info.signal.toIntOrNull()?.let {
                        if (it < 0)
                            it
                        else
                            it * 2 - 113
                    } ?: -113)
                }

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
                                mData = if (enableStatusList[i] == "0") "未开启" else if (onlineStatusList[i] == "1") "在线" else "离线",
                                textColorResId = if (enableStatusList[i] == "1" && onlineStatusList[i] == "1") R.color.device_online_platform else R.color.device_offline_platform
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

        fun newInstance() = UProductCommunicationInfoFragment()
    }
}