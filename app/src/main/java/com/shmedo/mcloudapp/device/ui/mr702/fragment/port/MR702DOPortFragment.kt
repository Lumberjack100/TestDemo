package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRDOPortParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDOPortParam
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMr702DoPortBinding
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.MRDODIPortItem
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DOPortViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702DOPortFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702DoPortBinding by lazy { getBinding() as FragmentMr702DoPortBinding }
    private val mStates: MR702DOPortViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_do_port, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRefresh()
        initAdapter()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
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
                    ConvertUtils.dp2px(8f),
                    false
                )
            )
            addType<MRDODIPortItem>(R.layout.item_mr702_do_port)
            // 点击列表触发选中
            onClick(R.id.statusSB) {
                val item = getModel<MRDODIPortItem>()
                if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                    Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                    return@onClick
                }
                if (!item.checked) {
                    showMessage("确定要打开 ${item.name} 吗？", "提示", "确定", {
                        item.checked = true
                        notifyItemChanged(modelPosition, true)
                        toggleSwitch(modelPosition)
                    }, "取消", {
                        item.checked = false
                        notifyItemChanged(modelPosition)
                    })
                } else {
                    item.checked = false
                    notifyItemChanged(modelPosition)
                    toggleSwitch(modelPosition)
                }
            }
        }
    }

    private fun toggleSwitch(position: Int, isOpen: Boolean = false) {
        commandItems.clear()
        val entity = MRDOPortParamEntity()
        when (position) {
            0 -> entity.kstatus1 = if (isOpen) "1" else "0"
            1 -> entity.kstatus2 = if (isOpen) "1" else "0"
            2 -> entity.kstatus3 = if (isOpen) "1" else "0"
            3 -> entity.kstatus4 = if (isOpen) "1" else "0"
            4 -> entity.kstatus5 = if (isOpen) "1" else "0"
            5 -> entity.kstatus6 = if (isOpen) "1" else "0"
            6 -> entity.kstatus7 = if (isOpen) "1" else "0"
            7 -> entity.kstatus8 = if (isOpen) "1" else "0"
        }
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
//        testData()
    }

    private fun queryInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DO_PORT_PARAM)
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
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        binding.refreshLayout.finish()
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_SET_DO_PORT_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun initParamData(doPortParam: MRDOPortParam) {
        try {
            val list = arrayListOf<MRDODIPortItem>()
            list.add(MRDODIPortItem(doPortParam.kstatus1 == "1", "K1"))
            list.add(MRDODIPortItem(doPortParam.kstatus2 == "1", "K2"))
            list.add(MRDODIPortItem(doPortParam.kstatus3 == "1", "K3"))
            list.add(MRDODIPortItem(doPortParam.kstatus4 == "1", "K4"))
            list.add(MRDODIPortItem(doPortParam.kstatus5 == "1", "K5"))
            list.add(MRDODIPortItem(doPortParam.kstatus6 == "1", "K6"))
            list.add(MRDODIPortItem(doPortParam.kstatus7 == "1", "K7"))
            list.add(MRDODIPortItem(doPortParam.kstatus8 == "1", "K8"))

            binding.rv.models = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun newInstance() = MR702DOPortFragment()
    }

    private fun testData() {
        val doPortParam = MRDOPortParam(
            kstatus1 = "1",
            kstatus2 = "1",
            kstatus3 = "1",
            kstatus4 = "1",
            kstatus5 = "1",
            kstatus6 = "0",
            kstatus7 = "1",
            kstatus8 = "0"
        )
        initParamData(doPortParam)
    }
}