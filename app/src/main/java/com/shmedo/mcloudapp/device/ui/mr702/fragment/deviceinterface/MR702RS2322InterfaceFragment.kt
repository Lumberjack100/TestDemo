package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinterface

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs2322InterfaceBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702InterfaceHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS2322InterfaceViewModel

class MR702RS2322InterfaceFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702Rs2322InterfaceBinding by lazy { getBinding() as FragmentMr702Rs2322InterfaceBinding }
    private val mInterfaceHomeViewModel: MR702InterfaceHomeViewModel by activityViewModels()
    private val mStates: MR702RS2322InterfaceViewModel by viewModels()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_rs232_2_interface, BR.vm, mStates)
            .addBindingParam(BR.homeVM, mInterfaceHomeViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            queryInfo()
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onSubmitClick() {

        }
    }

    /**
     * 保存采集参数
     */
    private fun initSaveCommand() {
        commandItems.clear()
    }

    override fun lazyLoadData() {
//        binding.refreshLayout.autoRefresh()
//        testData()
    }

    private fun queryInfo() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_485_PORT1_COLL)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_485_PORT1_SENSOR, "index=0")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        if (viewLifecycleOwner.lifecycle.currentState < androidx.lifecycle.Lifecycle.State.RESUMED) {
            return
        }

    }
    companion object {
        fun newInstance() = MR702RS2322InterfaceFragment()
    }
}