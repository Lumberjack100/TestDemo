package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS485Port3SensorStatus
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port3Binding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.MRSensorItem
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS485Port3ViewModel
import org.koin.android.ext.android.inject

class MR702RS485Port3Fragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702Rs485Port3Binding by lazy { getBinding() as FragmentMr702Rs485Port3Binding }
    private val mInterfaceHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val mStates: MR702RS485Port3ViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_rs485_port3, BR.stateVM, mStates)
            .addBindingParam(BR.homeVM, mInterfaceHomeViewModel)
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRefresh()
        initSensorAdapter()
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

    private fun initSensorAdapter() {
        binding.rv.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(8f),
                    false
                )
            )
            addType<MRSensorItem>(R.layout.item_mr702_port_sensor)
            R.id.item.onClick {
                val bundle = MR702RS485Port3SensorParamFragment.newBundleArguments(
                    modelPosition + 1,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(
                    R.id.action_mR702PortHomeFragment_to_mR702RS485Port3SensorParamFragment,
                    bundle
                )
            }
        }
    }
    override fun createObserver() {
        super.createObserver()
        //从编辑页面返回需要刷新事件详情页面
        setFragmentResultListener(FRAGMENT_RESULT_REQUEST_KEY) { key, bundle ->
            val refreshData = bundle.getBoolean(REFRESH_DATA)
            if (refreshData) {
                binding.refreshLayout.autoRefresh()
            }
        }
    }
    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_RS485_PORT3_SENSOR_STATUS)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        if (viewLifecycleOwner.lifecycle.currentState < androidx.lifecycle.Lifecycle.State.RESUMED) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_RS485_PORT3_SENSOR_STATUS -> {
                val result = iotParseManager.parse<MRRS485Port3SensorStatus>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT3_SENSOR_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询状态出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        binding.refreshLayout.finish()
                        initSensorData(result.data)
                    }
                }
            }

            else -> {}
        }
    }

    private fun initSensorData(sensorStatus: MRRS485Port3SensorStatus) {
        val list = mutableListOf<MRSensorItem>()
        var item = MRSensorItem(
            isPlugin = sensorStatus.solarstatus == "1",
            addr = sensorStatus.solarid,
            addrDesc = "地址-${sensorStatus.solarid}",
            sensorName = "太阳能控制器",
        )
        list.add(item)

        item = MRSensorItem(
            isPlugin = sensorStatus.ysstatus == "1",
            addr = sensorStatus.ysid,
            addrDesc = "地址-${sensorStatus.ysid}",
            sensorName = "声光报警器",
        )
        list.add(item)

        item = MRSensorItem(
            isPlugin = sensorStatus.ledstatus == "1",
            addr = sensorStatus.ledid,
            addrDesc = "地址-${sensorStatus.ledid}",
            sensorName = "LED屏",
        )
        list.add(item)
        binding.rv.models = list
    }

    companion object {
        fun newInstance() = MR702RS485Port3Fragment()
        const val FRAGMENT_RESULT_REQUEST_KEY = "MR702RS485Port3Fragment"
        const val REFRESH_DATA = "refresh_data"
    }
}