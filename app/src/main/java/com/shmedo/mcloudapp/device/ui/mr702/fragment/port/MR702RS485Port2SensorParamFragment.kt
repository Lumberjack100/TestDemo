package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRRS485Port2SensorParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS485Port2SensorParam
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port2SensorParamBinding
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.common.MRRS485Port2
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.MRSensorItem
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS485Port2SensorParamViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat

class MR702RS485Port2SensorParamFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702Rs485Port2SensorParamBinding by lazy { getBinding() as FragmentMr702Rs485Port2SensorParamBinding }
    private val mStates: MR702RS485Port2SensorParamViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var isAdd: Boolean = false
    private lateinit var sensorItem: MRSensorItem

    private val decimalFormat = DecimalFormat("#.#")

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_rs485_port2_sensor_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "RS485-2"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                processBack(true)
            }
        })
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            isAdd = it.getBoolean(ADD_SENSOR)
            sensorItem = it.getParcelable(SENSOR_MODEL_ITEM)!!
        }
        if (isAdd) {
            binding.refreshLayout.setEnableRefresh(false)
            mStates.isEditable.set(true)
        }
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(!isAdd)
        mStates.sensorType.set(sensorItem.sensorType)
        mStates.sensorName.set(sensorItem.sensorName)
        mStates.modelToken.set(sensorItem.modelToken)
    }

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
        }

        fun onSubmitClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.modelToken.get().isEmpty()) {
            Toaster.show("请输入物模型")
            return
        }
        if (mStates.channelNumber.get().isEmpty()) {
            Toaster.show("请输入通道编号")
            return
        }
        if (mStates.hydrologicalIdentification.get().isEmpty()) {
            Toaster.show("请输入水文标识")
            return
        }
        if (mStates.filterCoefficient.get().isEmpty()) {
            Toaster.show("请输入滤波系数")
            return
        }
        if (mStates.triggerValue.get().isEmpty()) {
            Toaster.show("请输入触发值")
            return
        }
        if (mStates.upperLimit.get().isEmpty()) {
            Toaster.show("请输入上限值")
            return
        }
        if (mStates.lowerLimit.get().isEmpty()) {
            Toaster.show("请输入下限值")
            return
        }
        if (mStates.correctValue.get().isEmpty()) {
            Toaster.show("请输入修正值")
            return
        }
        val entity = MRRS485Port2SensorParamEntity(
            sensortype = mStates.sensorType.get(),
            model = mStates.modelToken.get() + "_" + mStates.sensorType.get(),
            chl = mStates.channelNumber.get(),
            swtoken = mStates.hydrologicalIdentification.get(),
            filtercnt = mStates.filterCoefficient.get(),
            gateval = mStates.triggerValue.get(),
            uplimit = mStates.upperLimit.get(),
            lowlimit = mStates.lowerLimit.get(),
            corrvalue = mStates.correctValue.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RS485_PORT2_SENSOR_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        if (isAdd) return
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_GET_RS485_PORT2_SENSOR_PARAM,
            "chl=${sensorItem.chl}"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_RS485_PORT2_SENSOR_PARAM -> {
                val result = iotParseManager.parse<MRRS485Port2SensorParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT2_SENSOR_PARAM
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

            IOTCommandType.MD_MR_SET_RS485_PORT2_SENSOR_PARAM -> {
                setEditable(false)
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
                            processBack()
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun initParamData(sensorParam: MRRS485Port2SensorParam) {
        try {
            mStates.sensorAddress.set(sensorParam.sensoraddr)
            mStates.sensorType.set(sensorParam.sensortype)
            mStates.modelToken.set(sensorParam.model.substring(0, sensorParam.model.indexOf("_")))
            mStates.channelNumber.set(sensorParam.chl)
            mStates.hydrologicalIdentification.set(sensorParam.swtoken)
            mStates.filterCoefficient.set(sensorParam.filtercnt)
            mStates.triggerValue.set(sensorParam.gateval)
            mStates.upperLimit.set(sensorParam.uplimit)
            mStates.lowerLimit.set(sensorParam.lowlimit)
            mStates.correctValue.set(sensorParam.corrvalue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    private fun processBack(isPressBackBtn: Boolean = false) {
        lifecycleScope.launch {
            if (isPressBackBtn) {
                mMessenger.requestStatusBarColor(if (statusBarColor == 0) R.color.colorPrimary else statusBarColor)
                nav().navigateUp()
                return@launch
            }
            delay(1000)
            //需要给上一级浏览页面传递最新的事件信息
            mMessenger.requestMR702Rs485PortSensorRefresh(MRRS485Port2)
            nav().navigateUp()
        }
    }

    companion object {
        const val SENSOR_MODEL_ITEM = "sensor_model_item"
        const val ADD_SENSOR = "add_sensor"

        fun newBundleArguments(
            sensorItem: MRSensorItem,
            isAdd: Boolean = false,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(SENSOR_MODEL_ITEM, sensorItem)
            putBoolean(ADD_SENSOR, isAdd)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}