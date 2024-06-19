package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRDOPortParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDIPortParam
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDOPortParam
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMr702IoPortBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.model.MRDODIPortItem
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702IOPortFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702IoPortBinding
    private lateinit var mStates: EmptyViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_io_port, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702IoPortBinding
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
                    ConvertUtils.dp2px(8f),
                    false
                )
            )
            addType<DeviceStatusInfoGroupItem>(R.layout.item_mr702_di_do_port_group)
            addType<MRDODIPortItem>(R.layout.item_mr702_do_port)
            // 点击列表触发选中
            onClick(R.id.statusSB) {
                val item = getModel<MRDODIPortItem>()
                if (isBleDisconnected()) {
                    Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                    return@onClick
                }
                if (!item.isOpen) {
                    showMessage("确定要打开 ${item.name} 吗？", "提示", "确定", {
                        item.isOpen = true
                        notifyItemChanged(modelPosition, true)
                        toggleSwitch(item)
                    }, "取消", {
                        item.isOpen = false
                        notifyItemChanged(modelPosition)
                    })
                } else {
                    item.isOpen = false
                    notifyItemChanged(modelPosition)
                    toggleSwitch(item)
                }
            }
        }
    }

    private fun toggleSwitch(item: MRDODIPortItem) {
        commandItems.clear()
        val entity = MRDOPortParamEntity(item.ktype, if (item.isOpen) "1" else "0")
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_DO_PORT_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryInfo() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DO_PORT_PARAM)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DI_PORT_PARAM)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DO_PORT_PARAM -> {
                val result = iotParseManager.parse<MRDOPortParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DO_PORT_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDOData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_GET_DI_PORT_PARAM -> {
                val result = iotParseManager.parse<MRDIPortParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DI_PORT_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDIData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_SET_DO_PORT_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("设置成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initDOData(doPortParam: MRDOPortParam) {
        launchWithViewLifecycle {
            try {
                val tempList = mutableListOf<Any>()

                tempList.add(DeviceStatusInfoGroupItem("继电器状态"))
                tempList.add(DeviceStatusInfoGroupItem(""))
                tempList.add(MRDODIPortItem(doPortParam.kstatus5 == "1", "5", "K1"))
                tempList.add(MRDODIPortItem(doPortParam.kstatus6 == "1", "6", "K2"))
                tempList.add(MRDODIPortItem(doPortParam.kstatus7 == "1", "7", "K3"))
                tempList.add(MRDODIPortItem(doPortParam.kstatus8 == "1", "8", "K4"))

                tempList.add(DeviceStatusInfoGroupItem("开关状态输出"))
                tempList.add(DeviceStatusInfoGroupItem(""))
                tempList.add(MRDODIPortItem(doPortParam.kstatus1 == "1", "1", "DO1"))
                tempList.add(MRDODIPortItem(doPortParam.kstatus2 == "1", "2", "DO2"))
                tempList.add(MRDODIPortItem(doPortParam.kstatus3 == "1", "3", "DO3"))
                tempList.add(MRDODIPortItem(doPortParam.kstatus4 == "1", "4", "DO4"))

                binding.rv.models = tempList
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initDIData(diPortParam: MRDIPortParam) {
        launchWithViewLifecycle {
            try {
                val tempList = mutableListOf<Any>()

                tempList.add(DeviceStatusInfoGroupItem("开关状态输入"))
                tempList.add(DeviceStatusInfoGroupItem(""))
                tempList.add(MRDODIPortItem(diPortParam.dstatus1 == "1", "1", "DI1", false))
                tempList.add(MRDODIPortItem(diPortParam.dstatus2 == "1", "2", "DI2", false))
                tempList.add(MRDODIPortItem(diPortParam.dstatus3 == "1", "3", "DI3", false))
                tempList.add(MRDODIPortItem(diPortParam.dstatus4 == "1", "4", "DI4", false))

                binding.rv.bindingAdapter.addModels(tempList)
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = MR702IOPortFragment()
    }
}