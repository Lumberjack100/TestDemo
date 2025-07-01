package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702.port

import android.os.Bundle
import androidx.fragment.app.activityViewModels
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
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port3SensorParam
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port3SensorStatus
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port3Binding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.MRSensorItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import org.koin.android.ext.android.inject

class MR702RS485Port3Fragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs485Port3Binding
    private val mInterfaceHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val iotParseManager: IOTParserManager by inject()

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
                    val bundle = MR702RS485Port3CameraParamFragment.Companion.newBundleArguments(
                        item.chl.toInt(),
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().safeNavigate(
                        R.id.action_global_to_mR702RS485Port3CameraParamFragment,
                        bundle
                    )
                } else if (item.sensorName == "声光报警器") {
                    val bundle =
                        MR702RS485Port3AcousticOpticalAlarmParamFragment.Companion.newBundleArguments(
                            productType,
                            communicateWay,
                            deviceInfo,
                            bleDevice
                        )
                    nav().safeNavigate(
                        R.id.action_global_to_mR702RS485Port3AcousticOpticalAlarmParamFragment,
                        bundle
                    )
                } else {
                    val bundle = MR702RS485Port3SensorParamFragment.Companion.newBundleArguments(
                        modelPosition + 1,
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().safeNavigate(
                        R.id.action_global_to_mR702RS485Port3SensorParamFragment,
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

        var command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_RS485_PORT3_MODULE_STATUS)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_GET_RS485_PORT3_SENSOR_PARAM,
            "device=2"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_GET_RS485_PORT3_SENSOR_PARAM,
            "device=3"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_GET_RS485_PORT3_CAMERA_PARAM,
            "index=0"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_GET_RS485_PORT3_CAMERA_PARAM,
            "index=1"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_GET_RS485_PORT3_CAMERA_PARAM,
            "index=2"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }
    private fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.MR_MD_GET_RS485_PORT3_MODULE_STATUS)
                || (commandType == IOTCommandType.MR_MD_GET_RS485_PORT3_SENSOR_PARAM)
                || (commandType == IOTCommandType.MR_MD_GET_RS485_PORT3_CAMERA_PARAM)
                || (commandType == IOTCommandType.MD_SET_RADIO_CTRL)

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultError(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
        )
    }

    /**
     * 4G 下发指令响应超时
     */
    override fun doCmdResponseResultTimeOut(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultTimeOut(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
        )
    }

    /**
     * 蓝牙下发指令响应超时
     */
    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean,
        errMsg: String
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.showNearbyCommunicationTimeoutAlert(
            cmdStr = cmdStr,
            isDismissLoadingDialog = isDismissLoadingDialog,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage,
            errMsg = errMsg
        )
    }
    override fun setResultData(cmdStr: String) {
        if (isRestrictHiddenMode() && isHidden) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_RS485_PORT3_MODULE_STATUS -> {
                val result = iotParseManager.parse<MRRS485Port3SensorStatus>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS485_PORT3_MODULE_STATUS
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
                        initSolarSensorData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_GET_RS485_PORT3_SENSOR_PARAM -> {
                val result = iotParseManager.parse<MRRS485Port3SensorParam>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS485_PORT3_SENSOR_PARAM
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
                        initAudibleAndLedData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_GET_RS485_PORT3_CAMERA_PARAM -> {
                val result = iotParseManager.parse<MRRS485Port3CameraParam>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS485_PORT3_CAMERA_PARAM
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

    /**
     * 处理太阳能控制器接入状态
     */
    private fun initSolarSensorData(sensorStatus: MRRS485Port3SensorStatus) {
        binding.rv.models = arrayListOf()
        var item = MRSensorItem(
            isPlugin = sensorStatus.solarstatus == "1",
            addr = sensorStatus.solarid,
            addrDesc = "地址-${sensorStatus.solarid}",
            sensorName = "太阳能控制器",
            isShowDel = false
        )
        binding.rv.mutable.add(item)
        binding.rv.bindingAdapter.notifyItemInserted(binding.rv.bindingAdapter.modelCount)
    }

    private fun initAudibleAndLedData(sensorParam: MRRS485Port3SensorParam) {
        if (sensorParam.device != "1") {
            val item = MRSensorItem(
                isPlugin = sensorParam.switch == "1",
                addr = sensorParam.addr,
                addrDesc = "地址-${sensorParam.addr}",
                sensorName = if (sensorParam.device == "2") "声光报警器" else "LED屏",
                isShowDel = false
            )
            binding.rv.mutable.add(item)
            binding.rv.bindingAdapter.notifyItemInserted(binding.rv.bindingAdapter.modelCount)
        }
    }

    private fun initCameraData(cameraParam: MRRS485Port3CameraParam) {
        val item = MRSensorItem(
            isPlugin = cameraParam.status == "1" && cameraParam.switch == "1",
            chl = cameraParam.index,
            addr = cameraParam.addr,
            addrDesc = "序号-${cameraParam.index.toInt() + 1}",
            sensorName = "串口摄像头",
            isShowDel = false
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