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
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.MRModuleStatusItem
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DeviceInfoViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

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
            addType<ConfigModule>(R.layout.item_mr702_device_info_module_status)
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
            IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO -> {
                val result = iotParseManager.parse<MRDeviceInfo>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelTimeoutJob()
                        val errMsg = "查询模块状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        binding.refreshLayout.finish()
                        val moduleStatusInfo: MRModuleStatusInfo = result.data.moduleStatusInfo

                        val list = mutableListOf<MRModuleStatusItem>()
                        list.add(
                            MRModuleStatusItem(
                                "触摸屏",
                                if (moduleStatusInfo.screen == "1") "正常" else "异常",
                                if (moduleStatusInfo.screen == "1") R.drawable.bg_mr702_module_status_normal else R.drawable.bg_mr702_module_status_abnormal
                            )
                        )
                        list.add(
                            MRModuleStatusItem(
                                "4G模块",
                                if (moduleStatusInfo.datanet == "1") "正常" else "异常",
                                if (moduleStatusInfo.datanet == "1") R.drawable.bg_mr702_module_status_normal else R.drawable.bg_mr702_module_status_abnormal
                            )
                        )
                        list.add(
                            MRModuleStatusItem(
                                "北斗定位模块",
                                if (moduleStatusInfo.beidou == "1") "正常" else "异常",
                                if (moduleStatusInfo.beidou == "1") R.drawable.bg_mr702_module_status_normal else R.drawable.bg_mr702_module_status_abnormal
                            )
                        )
                        list.add(
                            MRModuleStatusItem(
                                "有线网模块",
                                if (moduleStatusInfo.wirednet == "1") "正常" else "异常",
                                if (moduleStatusInfo.wirednet == "1") R.drawable.bg_mr702_module_status_normal else R.drawable.bg_mr702_module_status_abnormal
                            )
                        )
                        list.add(
                            MRModuleStatusItem(
                                "Flash",
                                if (moduleStatusInfo.flash == "1") "正常" else "异常",
                                if (moduleStatusInfo.flash == "1") R.drawable.bg_mr702_module_status_normal else R.drawable.bg_mr702_module_status_abnormal
                            )
                        )
                        list.add(
                            MRModuleStatusItem(
                                "EMMC存储模块",
                                if (moduleStatusInfo.emmc == "1") "正常" else "异常",
                                if (moduleStatusInfo.emmc == "1") R.drawable.bg_mr702_module_status_normal else R.drawable.bg_mr702_module_status_abnormal
                            )
                        )
                        binding.rv.models = list
                    }
                }
            }

            else -> {}
        }
    }

    companion object {
        fun newInstance() = MR702ModuleStatusInfoFragment()
    }
}