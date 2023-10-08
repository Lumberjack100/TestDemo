package com.shmedo.mcloudapp.device.ui.mr702.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDataCenterParam
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.databinding.FragmentMr702DataCenterParamBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DataCenterParamViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702DataCenterParamFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702DataCenterParamBinding by lazy { getBinding() as FragmentMr702DataCenterParamBinding }
    private val mStates: MR702DataCenterParamViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_data_center_param, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
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
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            queryData()
        }
    }

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
        }

        /**
         * 通信方式
         */
        fun onCommunicateWaySwitchClick() {

        }

        fun onIPTypeSwitchClick() {

        }

        /**
         * 传输协议
         */
        fun onTransferProtocolSwitchClick() {

        }

        /**
         * 数据协议
         */
        fun onDataProtocolSwitchClick() {

        }

        /**
         * 平台类型
         */
        fun onPlatformTypeSwitchClick() {

        }

        /**
         * 测站分类
         */
        fun onStationClassificationSwitchClick() {

        }

        /**
         * 高级设置展开、折叠
         */
        fun onToggleAdvancedClick() {
            mStates.isAdvancedItemVisible.set(!mStates.isAdvancedItemVisible.get())
        }

        fun onSubmitClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()

    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        val entity = CenterNumberEntity("1")
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DATA_CENTER, entity)
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
            IOTCommandType.MD_MR_GET_DATA_CENTER -> {
                val result = iotParseManager.parse<MRDataCenterParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DATA_CENTER
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelTimeoutJob()
                        val errMsg = "查询数据中心参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initDataCenterParam(result.data)
                    }
                }
            }

            else -> {}
        }
    }

    private fun initDataCenterParam(data: Any) {

    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}