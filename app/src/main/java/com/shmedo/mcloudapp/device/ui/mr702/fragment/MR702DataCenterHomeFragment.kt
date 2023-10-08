package com.shmedo.mcloudapp.device.ui.mr702.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDataCenterStatus
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.databinding.FragmentMr702DataCenterHomeBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.DataCenterStatusItem
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702DataCenterHomeFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702DataCenterHomeBinding by lazy { getBinding() as FragmentMr702DataCenterHomeBinding }
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_data_center_home,
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
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
        initRefresh()
        initAdapter()
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
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
                val bundle = MR702DataCenterParamFragment.newBundleArguments(
                    item,
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

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DATA_CENTER_STATUS)
        commandItems.add(command)

        sendCommandFromCmdList(isShowLoadingDialog = false)
    }

    override fun cancelTimeoutJob(isDismissLoadingDialog: Boolean) {
        super.cancelTimeoutJob(false)
        binding.refreshLayout.finish(false)
    }

    override fun showTimeoutAlert(
        isDismissLoadingDialog: Boolean,
        isShowMsg: Boolean,
        msg: String
    ) {
        binding.refreshLayout.finish(false)
        Toaster.show("发送指令超时,请稍后尝试")
    }

    override fun doNetDispatchFailed(cmdStr: String, errorMsg: String) {
        Toaster.show("下发指令失败: $errorMsg")
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        netIotCommandViewModel.processCmdResult()
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_CENTER_STATUS -> {
                val result = iotParseManager.parse<MRDataCenterStatus>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DATA_CENTER_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelTimeoutJob()
                        val errMsg = "查询数据中心状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initDataCenterStatus(result.data)
                    }
                }
            }

            else -> {}
        }
    }

    private fun initDataCenterStatus(dataCenterStatus: MRDataCenterStatus) {
        binding.refreshLayout.addData(
            mutableListOf(
                DataCenterStatusItem(1, "数据中心01", dataCenterStatus.status1),
                DataCenterStatusItem(2, "数据中心02", dataCenterStatus.status2),
                DataCenterStatusItem(3, "数据中心03", dataCenterStatus.status3),
                DataCenterStatusItem(4, "数据中心04", dataCenterStatus.status4),
                DataCenterStatusItem(5, "数据中心05", dataCenterStatus.status5),
            ), hasMore = {
                false
            }
        )
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}