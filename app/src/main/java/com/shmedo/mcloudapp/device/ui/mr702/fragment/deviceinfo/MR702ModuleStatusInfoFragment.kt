package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinfo

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRModuleStatusInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMr702ModuleStatusInfoBinding
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.MRModuleStatusItem
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DeviceInfoViewModel
import org.koin.android.ext.android.inject

class MR702ModuleStatusInfoFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702ModuleStatusInfoBinding by lazy { getBinding() as FragmentMr702ModuleStatusInfoBinding }
    private val mStates: MR702DeviceInfoViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_module_status_info, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRefresh()
        initAdapter()
//        testData()
    }

    private fun testData() {
        val list = mutableListOf<MRModuleStatusItem>()
        list.add(
            MRModuleStatusItem(
                "触摸屏",
                "正常"
            )
        )
        list.add(
            MRModuleStatusItem(
                "4G模块",
                "异常"
            )
        )
        list.add(
            MRModuleStatusItem(
                "北斗定位模块",
                "正常"
            )
        )
        list.add(
            MRModuleStatusItem(
                "有线网模块",
                "异常"
            )
        )
        list.add(
            MRModuleStatusItem(
                "Flash",
                "正常"
            )
        )
        list.add(
            MRModuleStatusItem(
                "EMMC存储模块",
                "正常"
            )
        )
        binding.rv.models = list
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            queryInfo()
        }
    }

    private fun initAdapter() {
        binding.rv.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(10f),
                    false
                )
            )
            addType<MRModuleStatusItem>(R.layout.item_mr702_device_info_module_status)
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryInfo() {
        commandItems.clear()

        val entity = MRDeviceInfoEntity(pages = 4, label = 1)
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)
        sendCommandFromCmdList(isShowLoadingDialog = false)
    }

    override fun cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog: Boolean) {
        super.cancelNearbyCommunicationTimeoutJob(false)
        binding.refreshLayout.finish(false)
    }

    override fun showNearbyCommunicationTimeoutAlert(
        isDismissLoadingDialog: Boolean,
        isShowMsg: Boolean,
        msg: String
    ) {
        Toaster.show("发送指令超时,请稍后尝试")
        binding.refreshLayout.finish(false)
    }

    override fun doNetDispatchFailed(cmdStr: String, errorMsg: String) {
        Toaster.show("下发指令失败: $errorMsg")
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        netIotCommandViewModel.processCmdResult()
    }

    override fun doCmdResponseResultError(errorMsg: String) {
        Toaster.show("指令响应错误: $errorMsg")
        binding.refreshLayout.finish(false)
    }

    override fun doCmdResponseResultTimeOut(errorMsg: String) {
        Toaster.show("指令响应超时: $errorMsg")
        binding.refreshLayout.finish(false)
    }

    override fun setResultData(cmdStr: String) {
        if (viewLifecycleOwner.lifecycle.currentState < androidx.lifecycle.Lifecycle.State.RESUMED) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO -> {
                val result = iotParseManager.parse<MRDeviceInfo>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询模块状态出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList(isShowLoadingDialog = false)
                        binding.refreshLayout.finish()
                        initData(result.data.moduleStatusInfo)
                    }
                }
            }

            else -> {}
        }
    }

    private fun initData(moduleStatusInfo: MRModuleStatusInfo) {
        val list = mutableListOf<MRModuleStatusItem>()
        list.add(
            MRModuleStatusItem(
                "触摸屏",
                if (moduleStatusInfo.screen == "1") "正常" else "异常"
            )
        )
        list.add(
            MRModuleStatusItem(
                "4G模块",
                if (moduleStatusInfo.datanet == "1") "正常" else "异常"
            )
        )
        list.add(
            MRModuleStatusItem(
                "北斗定位模块",
                if (moduleStatusInfo.beidou == "1") "正常" else "异常"
            )
        )
        list.add(
            MRModuleStatusItem(
                "有线网模块",
                if (moduleStatusInfo.wirednet == "1") "正常" else "异常"
            )
        )
        list.add(
            MRModuleStatusItem(
                "Flash",
                if (moduleStatusInfo.flash == "1") "正常" else "异常"
            )
        )
        list.add(
            MRModuleStatusItem(
                "EMMC存储模块",
                if (moduleStatusInfo.emmc == "1") "正常" else "异常"
            )
        )
        binding.rv.models = list
    }

    companion object {
        fun newInstance() = MR702ModuleStatusInfoFragment()
    }
}