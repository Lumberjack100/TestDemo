package com.shmedo.mcloudapp.ui.page.device.mr702.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDataCenterStatus
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUniversalDataCenterHomeBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterParamFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UniversalDataCenterHomeViewModel
import org.koin.android.ext.android.inject

class MR702DataCenterHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUniversalDataCenterHomeBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: UniversalDataCenterHomeViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_universal_data_center_home,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUniversalDataCenterHomeBinding
        toolbarViewModel.toolbarTitleText.set("数据链路")
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initRefresh()
        initAdapter()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryStatusInfo()
        }
    }

    private fun initAdapter() {
        binding.recyclerView.setup { rv ->
            addType<DataCenterStatusItem>(R.layout.data_center_status_item)
            R.id.item.onClick {
                val item = getModel<DataCenterStatusItem>()
                val bundle = UniversalDataCenterParamFragment.newBundleArguments(
                        item,
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                nav().navigate(
                    R.id.action_mR702DataCenterHomeFragment_to_mR702DataCenterParamFragment,
                    bundle
                )
            }
        }
    }

    override fun createObserver() {
        super.createObserver()
        //从编辑页面返回需要刷新事件详情页面
        setFragmentResultListener(AppContants.Extras.FRAGMENT_DATA_CENTER_HOME_RESULT_REQUEST_KEY) { key, bundle ->
            val refreshData = bundle.getBoolean(REFRESH_DATA)
            if (refreshData) {
                binding.refreshLayout.autoRefresh()
            }
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DATA_CENTER_STATUS)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        if (isRestrictHiddenMode() && isHidden) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_CENTER_STATUS -> {
                val result = iotParseManager.parse<MRDataCenterStatus>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DATA_CENTER_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询数据链路状态出错: ${result.message}"
                        handleFailureResult(errMsg)
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

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initDataCenterStatus(dataCenterStatus: MRDataCenterStatus) {
        binding.recyclerView.models = mutableListOf(
            DataCenterStatusItem(
                centerid = 1,
                name = "数据链路1",
                status = dataCenterStatus.status1,
                bgResId = R.drawable.layer_common_click_item_top_corner_4_with_divider
            ),
            DataCenterStatusItem(
                centerid = 2,
                name = "数据链路2",
                status = dataCenterStatus.status2,
                bgResId = R.drawable.layer_common_click_item_with_divider
            ),
            DataCenterStatusItem(
                centerid = 3,
                name = "数据链路3",
                status = dataCenterStatus.status3,
                bgResId = R.drawable.layer_common_click_item_with_divider
            ),
            DataCenterStatusItem(
                centerid = 4,
                name = "数据链路4",
                status = dataCenterStatus.status4,
                bgResId = R.drawable.layer_common_click_item_with_divider
            ),
            DataCenterStatusItem(
                centerid = 5,
                name = "数据链路5",
                status = dataCenterStatus.status5,
                bgResId = R.drawable.shape_common_click_item_bottom_corner_4
            )
        )
    }

    companion object {
        const val REFRESH_DATA = "refresh_data"
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}