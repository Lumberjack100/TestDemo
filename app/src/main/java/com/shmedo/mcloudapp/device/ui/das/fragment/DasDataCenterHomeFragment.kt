package com.shmedo.mcloudapp.device.ui.das.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.enums.ServerOne
import com.shmedo.lib.device.base.iot_cmd.enums.ServerThree
import com.shmedo.lib.device.base.iot_cmd.enums.ServerTwo
import com.shmedo.lib.device.base.iot_cmd.model.common.DataCenterStatus
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.databinding.FragmentDasDataCenterHomeBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.DataCenterStatusItem
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.DataCenterParamFragment
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class DasDataCenterHomeFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentDasDataCenterHomeBinding by lazy { getBinding() as FragmentDasDataCenterHomeBinding }
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_data_center_home,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
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
                    ProductType.DAS,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(
                    R.id.action_dasDataCenterHomeFragment_to_dataCenterParamFragment,
                    bundle
                )
            }
        }.models = getAdapterData()
    }

    override fun createObserver() {
        super.createObserver()
        //从编辑页面返回需要刷新事件详情页面
        setFragmentResultListener(FRAGMENT_RESULT_REQUEST_KEY) { key, bundle ->
            val refreshData = bundle.getBoolean(REFRESH_DATA)
            if (refreshData) {
                binding.refreshLayout.autoRefresh()
            }
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        var entity = CenterNumberEntity(ServerOne.centerid.toString())
        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, entity)
        commandItems.add(command)

        entity = CenterNumberEntity(ServerTwo.centerid.toString())
        command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, entity)
        commandItems.add(command)

        entity = CenterNumberEntity(ServerThree.centerid.toString())
        command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, entity)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog: Boolean) {
        super.cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog)
        binding.refreshLayout.finish(false)
    }

    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowMsg: Boolean,
        msg: String
    ) {
        super.showNearbyCommunicationTimeoutAlert(cmdStr, isDismissLoadingDialog, isShowMsg, msg)
        binding.refreshLayout.finish(false)
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
            ServerOne.centerid -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(0)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerTwo.centerid -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(1)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerThree.centerid -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(2)
                    .refreshStatus(dataCenterStatus.status)
            }
        }


    }

    private fun getAdapterData() = mutableListOf(
        DataCenterStatusItem(1, "数据中心01", "0"),
        DataCenterStatusItem(2, "数据中心02", "0"),
        DataCenterStatusItem(3, "数据中心03", "0"),
    )

    companion object {
        const val FRAGMENT_RESULT_REQUEST_KEY = "DasDataCenterHomeFragment"
        const val REFRESH_DATA = "refresh_data"
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}