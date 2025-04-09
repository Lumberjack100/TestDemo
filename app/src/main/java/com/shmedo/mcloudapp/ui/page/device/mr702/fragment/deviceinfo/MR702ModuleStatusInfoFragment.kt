package com.shmedo.mcloudapp.ui.page.device.mr702.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRModuleStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentMr702ModuleStatusInfoBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.model.MRModuleStatusItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702DeviceStatusInfoParentViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import org.koin.android.ext.android.inject

class MR702ModuleStatusInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702ModuleStatusInfoBinding
    private lateinit var mStates: MR702DeviceStatusInfoParentViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_module_status_info, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702ModuleStatusInfoBinding
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
        val command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        //判断是否页面是否处于 resume 状态
        if (!isResumed) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO -> {
                val result = iotParseManager.parse<MRDeviceInfo>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询模块状态出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initData(result.data.moduleStatusInfo)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
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