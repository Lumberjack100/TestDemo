package com.shmedo.mcloudapp.device.ui.mr702.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.device.base.iot_cmd.IOTCommandManager
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRWirelessNet
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParseManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.dismissLoadingDialog
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.databinding.FragmentMr702ReportMethodBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.viewmodel.state.MR702ReportMethodViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：   上报方式
 */
class MR702ReportMethodFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702ReportMethodBinding by lazy { getBinding() as FragmentMr702ReportMethodBinding }
    private val mStates: MR702ReportMethodViewModel by viewModels()
    private val iotParseManager: IOTParseManager by inject()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_report_method, BR.vm, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData() {
        super.initData()
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onReportingMethodClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
        }

        fun onReportingStartTimeClick() {

        }

        fun onSubmitClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
//            initSaveCommand()
        }
    }

    override fun lazyLoadData() {
//        queryData()
    }

    private fun queryData() {
        commandItems.clear()

        var command = IOTCommandManager.getCommand(IOTCommandType.MD_MR_GET_DATA_NETWORK)
        commandItems.add(command)

        command = IOTCommandManager.getCommand(IOTCommandType.MD_MR_GET_WIRED_NETWORK)
        commandItems.add(command)

        sendCommandFromCmdList()
        showLoadingDialog(StringUtils.getString(R.string.loading))
        if (communicateWay is BleConnect) {
            startTimeoutJob()
        }
    }

    override fun doNetDispatchFailed(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_NETWORK -> Toaster.show("下发指令失败: $errorMsg")

            else -> {}
        }
    }

    override fun cancelTimeoutJob() {
        super.cancelTimeoutJob()
        dismissLoadingDialog()
    }

    override fun showTimeoutAlert() {
        // 关闭 loading 框并显示超时警告
        dismissLoadingDialog()
        Toaster.show("发送指令超时,请稍后尝试")
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_NETWORK -> {
                netIotCommandViewModel.processCmdResult()
            }

            else -> {}
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_NETWORK -> {
                val result = iotParseManager.parse<MRWirelessNet>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DATA_NETWORK
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelTimeoutJob()
                        val errMsg = "查询无线配置出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
//                        initWirelessData(result.data)
                    }
                }
            }

            else -> {}
        }
    }

    companion object {
        fun newInstance() = MR702ReportMethodFragment()
    }

}