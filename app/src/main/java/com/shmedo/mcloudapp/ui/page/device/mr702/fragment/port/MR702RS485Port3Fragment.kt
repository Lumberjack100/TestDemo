package com.shmedo.mcloudapp.ui.page.device.mr702.fragment.port

import android.os.Bundle
import androidx.fragment.app.setFragmentResultListener
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port3CameraParam
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port3SensorStatus
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port3Binding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.model.MRSensorItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import org.koin.android.ext.android.inject

class MR702RS485Port3Fragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs485Port3Binding
    private lateinit var mInterfaceHomeViewModel: MR702PortHomeViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        mInterfaceHomeViewModel = getActivityScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_rs485_port3,
            BR.homeVM,
            mInterfaceHomeViewModel
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs485Port3Binding
        initRefresh()
        initSensorAdapter()
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
                val item = getModel<MRSensorItem>()
                if (item.sensorName == "串口摄像头") {
                    val bundle = MR702RS485Port3CameraParamFragment.newBundleArguments(
                        item.chl.toInt(),
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().navigate(
                        R.id.action_mR702PortHomeFragment_to_mR702RS485Port3CameraParamFragment,
                        bundle
                    )
                } else {
                    val bundle = MR702RS485Port3SensorParamFragment.newBundleArguments(
                        modelPosition + 1,
                        productType,
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

        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_RS485_PORT3_SENSOR_STATUS)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_GET_RS485_PORT3_CAMERA_PARAM,
            "index=0"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_GET_RS485_PORT3_CAMERA_PARAM,
            "index=1"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_GET_RS485_PORT3_CAMERA_PARAM,
            "index=2"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_RS485_PORT3_SENSOR_STATUS -> {
                val result = iotParseManager.parse<MRRS485Port3SensorStatus>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT3_SENSOR_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initSensorData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_GET_RS485_PORT3_CAMERA_PARAM -> {
                val result = iotParseManager.parse<MRRS485Port3CameraParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT3_CAMERA_PARAM
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
                        initCameraData(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initSensorData(sensorStatus: MRRS485Port3SensorStatus) {
        binding.rv.models = arrayListOf()
        var item = MRSensorItem(
            isPlugin = sensorStatus.solarstatus == "1",
            addr = sensorStatus.solarid,
            addrDesc = "地址-${sensorStatus.solarid}",
            sensorName = "太阳能控制器",
        )
        binding.rv.mutable.add(item)
        binding.rv.bindingAdapter.notifyItemInserted(binding.rv.bindingAdapter.modelCount)

        item = MRSensorItem(
            isPlugin = sensorStatus.ysstatus == "1",
            addr = sensorStatus.ysid,
            addrDesc = "地址-${sensorStatus.ysid}",
            sensorName = "声光报警器",
        )
        binding.rv.mutable.add(item)
        binding.rv.bindingAdapter.notifyItemInserted(binding.rv.bindingAdapter.modelCount)

        item = MRSensorItem(
            isPlugin = sensorStatus.ledstatus == "1",
            addr = sensorStatus.ledid,
            addrDesc = "地址-${sensorStatus.ledid}",
            sensorName = "LED屏",
        )
        binding.rv.mutable.add(item)
        binding.rv.bindingAdapter.notifyItemInserted(binding.rv.bindingAdapter.modelCount)
    }

    private fun initCameraData(cameraParam: MRRS485Port3CameraParam) {
        val item = MRSensorItem(
            isPlugin = cameraParam.status == "1",
            chl = cameraParam.index,
            addr = cameraParam.addr,
            addrDesc = "序号-${cameraParam.index.toInt() + 1}",
            sensorName = "串口摄像头",
        )
        binding.rv.mutable.add(item)
        binding.rv.bindingAdapter.notifyItemInserted(binding.rv.bindingAdapter.modelCount)
    }


    companion object {
        fun newInstance() = MR702RS485Port3Fragment()
        const val FRAGMENT_RESULT_REQUEST_KEY = "MR702RS485Port3Fragment"
        const val REFRESH_DATA = "refresh_data"
    }
}