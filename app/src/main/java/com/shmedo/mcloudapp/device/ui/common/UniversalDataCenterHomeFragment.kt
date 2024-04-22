package com.shmedo.mcloudapp.device.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.enums.ServerFive
import com.shmedo.lib.device.base.iot_cmd.enums.ServerFour
import com.shmedo.lib.device.base.iot_cmd.enums.ServerOne
import com.shmedo.lib.device.base.iot_cmd.enums.ServerThree
import com.shmedo.lib.device.base.iot_cmd.enums.ServerTwo
import com.shmedo.lib.device.base.iot_cmd.model.common.DataCenterStatus
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.databinding.FragmentUniversalDataCenterHomeBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.DataCenterStatusItem
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class UniversalDataCenterHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUniversalDataCenterHomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private val iotParseManager: IOTParserManager by inject()
    private var centerNum = 0;//数据中心数量
    private var productType = ProductType.UnKnown

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_universal_data_center_home,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUniversalDataCenterHomeBinding
        binding.llToolbar.toolbar.title = "数据中心"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initRefresh()
        initAdapter()
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            centerNum = it.getInt(CENTER_NUM)
            productType = it.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
        }
        binding.recyclerView.bindingAdapter.models = getAdapterData()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    private fun initAdapter() {
        binding.recyclerView.setup { rv ->
            rv.addItemDecoration(
                RecycleViewDivider(
                    LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(8f), ColorUtils.getColor(
                        R.color.transparent
                    )
                )
            )
            addType<DataCenterStatusItem>(R.layout.data_center_status_item)
            R.id.item.onClick {
                val item = getModel<DataCenterStatusItem>()
                val bundle = DataCenterParamFragment.newBundleArguments(
                    item,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(
                    R.id.action_global_dataCenterParamFragment,
                    bundle
                )
            }
        }
    }

    override fun createObserver() {
        super.createObserver()
        //从编辑页面返回需要刷新事件详情页面
        setFragmentResultListener(AppContants.Extras.FRAGMENT_DATA_CENTER_HOME_RESULT_REQUEST_KEY) { key, bundle ->
            val centerNumber =
                bundle.getInt(AppContants.Extras.REFRESH_DATA_CENTER_STATUS, ServerOne.centerId)
            commandItems.clear()

            val entity = CenterNumberEntity(centerNumber.toString())
            val command =
                IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, entity)
            commandItems.add(command)

            showLoadingDialog(StringUtils.getString(R.string.loading))
            sendCommandFromCmdList(isStartTimeoutJob = true)
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        for (i in 1..centerNum) {
            val entity = CenterNumberEntity(i.toString())
            val command =
                IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, entity)
            commandItems.add(command)
        }

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {

            IOTCommandType.MD_GET_DATA_CENTER_STATUS -> {
                val result = iotParseManager.parse<DataCenterStatus>(
                    cmdStr,
                    IOTCommandType.MD_GET_DATA_CENTER_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询数据中心状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDataCenterStatus(result.data)
                    }
                }
            }

            else -> {}
        }
    }

    private fun initDataCenterStatus(dataCenterStatus: DataCenterStatus) {
        when (dataCenterStatus.centerid) {
            ServerOne.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(0)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerTwo.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(1)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerThree.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(2)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerFour.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(3)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerFive.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(4)
                    .refreshStatus(dataCenterStatus.status)
            }
        }
    }

    private fun getAdapterData(): MutableList<DataCenterStatusItem> {
        val list = mutableListOf<DataCenterStatusItem>()
        //centerNum
        for (i in 1..centerNum) {
            list.add(DataCenterStatusItem(i, "数据中心$i", "0"))
        }
        return list
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private const val CENTER_NUM = "center_num"

        fun newBundleArguments(
            centerNum: Int = 1,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putInt(CENTER_NUM, centerNum)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}