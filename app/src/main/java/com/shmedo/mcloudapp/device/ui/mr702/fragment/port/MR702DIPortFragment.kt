package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDIPortParam
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMr702DiPortBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.MRDODIPortItem
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DIPortViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702DIPortFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702DiPortBinding
    private lateinit var mStates: MR702DIPortViewModel
    private val iotParseManager: IOTParserManager by inject()

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_di_port, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702DiPortBinding
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
            addType<MRDODIPortItem>(R.layout.item_mr702_di_port)
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
//        testData()
    }

    private fun queryInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DI_PORT_PARAM)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
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
                        initParamData(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initParamData(diPortParam: MRDIPortParam) {
        try {
            val list = mutableListOf<MRDODIPortItem>()
            list.add(MRDODIPortItem(diPortParam.dstatus1 == "1", "DI-1"))
            list.add(MRDODIPortItem(diPortParam.dstatus2 == "1", "DI-2"))
            list.add(MRDODIPortItem(diPortParam.dstatus3 == "1", "DI-3"))
            list.add(MRDODIPortItem(diPortParam.dstatus4 == "1", "DI-4"))
            list.add(MRDODIPortItem(diPortParam.dstatus5 == "1", "DI-5"))
            list.add(MRDODIPortItem(diPortParam.dstatus6 == "1", "DI-6"))
            list.add(MRDODIPortItem(diPortParam.dstatus7 == "1", "DI-7"))
            list.add(MRDODIPortItem(diPortParam.dstatus8 == "1", "DI-8"))

            binding.rv.models = list
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        fun newInstance() = MR702DIPortFragment()
    }

    private fun testData() {
        val diPortParam = MRDIPortParam(
            dstatus1 = "1",
            dstatus2 = "0",
            dstatus3 = "1",
            dstatus4 = "0",
            dstatus5 = "1",
            dstatus6 = "0",
            dstatus7 = "1",
            dstatus8 = "0",
        )
        initParamData(diPortParam)
    }
}