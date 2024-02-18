package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRRS485Port3SensorParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS485Port3SensorParam
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.ext.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port3SensorParamBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS485Port3SensorParamViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/16 <br/>
 * 描述：     RS485-3接口传感器参数
 */
class MR702RS485Port3SensorParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs485Port3SensorParamBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: MR702RS485Port3SensorParamViewModel
    private val iotParseManager: IOTParserManager by inject()

    private var sensorType: Int = 0
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }
    private val ledTypeList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs485_port3_led_type) }

    private val decimalFormat = DecimalFormat("#.#")


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_rs485_port3_sensor_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs485Port3SensorParamBinding
        binding.llToolbar.toolbar.title = "RS485-3"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                processBack(true)
            }
        })
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(true)
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            sensorType = it.getInt(SENSOR_TYPE)
            mStates.sensorType.set(sensorType)
            mStates.sensorName.set(if (sensorType == 1) "太阳能控制器" else if (sensorType == 2) "声光报警器" else "LED屏")
        }
        mStates.dataBit.set(dataBitList[3])
        mStates.checkBit.set(checkBitList[0])
        mStates.stopBit.set(stopBitList[0])
        mStates.ledType.set(ledTypeList[0])
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

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isOpened.set(isChecked)
            if (!isChecked) {
                showMessage("确定要关闭吗？", "温馨提示", "确定", {
                    closeSwitch()
                }, "取消", {
                    mStates.isOpened.set(true)
                    (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                })
            }
        }

        fun onDataBitChooseClick() {
            val selectedIndex = dataBitList.indexOf(mStates.dataBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据位", dataBitList,
                    null, selectedIndex,
                    { _, text ->
                        mStates.dataBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onCheckBitChooseClick() {
            val selectedIndex = checkBitList.indexOf(mStates.checkBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择校验位", checkBitList,
                    null, selectedIndex,
                    { _, text ->
                        mStates.checkBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onStopBitChooseClick() {
            val selectedIndex = stopBitList.indexOf(mStates.stopBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择停止位", stopBitList,
                    null, selectedIndex,
                    { _, text ->
                        mStates.stopBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSwitchLedTypeClick() {
            val selectedIndex = ledTypeList.indexOf(mStates.ledType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择LED", ledTypeList,
                    null, selectedIndex,
                    { _, text ->
                        mStates.ledType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSubmitClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun closeSwitch() {
        commandItems.clear()
        val entity = MRRS485Port3SensorParamEntity(
            device = sensorType.toString(),
            switch = "0"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RS485_PORT3_SENSOR_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.address.get().isEmpty()) {
            showMessageDialog("请输入地址")
            return
        }
        if (mStates.baudRate.get().isEmpty()) {
            showMessageDialog("请输入波特率")
            return
        }
        val entity = MRRS485Port3SensorParamEntity(
            device = sensorType.toString(),
            switch = "1",
            addr = mStates.address.get(),
            baud = mStates.baudRate.get(),
            databit = mStates.dataBit.get(),
            paritybit = (checkBitList.indexOf(mStates.checkBit.get())).toString(),
            stopbit = (stopBitList.indexOf(mStates.stopBit.get())).toString(),
        )
        when (sensorType) {
            //TODO: 太阳能控制器参数不需要设置，只是展示
//            1 -> {
//                if (mStates.solarVoltage.get().isEmpty()) {
//                    showMessageDialog("请输入太阳能板电压")
//                    return
//                }
//                if (mStates.batteryVoltage.get().isEmpty()) {
//                    showMessageDialog("请输入电池电压")
//                    return
//                }
//                if (mStates.solarPower.get().isEmpty()) {
//                    showMessageDialog("请输入太阳板能功率")
//                    return
//                }
//                if (mStates.loadPower.get().isEmpty()) {
//                    showMessageDialog("请输入负载功率")
//                    return
//                }
//                entity.svolt = mStates.solarVoltage.get()
//                entity.bvolt = mStates.batteryVoltage.get()
//                entity.spower = mStates.solarPower.get()
//                entity.lpower = mStates.loadPower.get()
//            }

            2 -> {
                if (mStates.duration.get().isEmpty()) {
                    showMessageDialog("请输入播放时长")
                    return
                }
                if (mStates.interval.get().isEmpty()) {
                    showMessageDialog("请输入切换间隔")
                    return
                }
                if (mStates.rainTriggerValueLevel1.get().isEmpty()) {
                    showMessageDialog("请输入降雨量一级报警")
                    return
                }
                if (mStates.rainTriggerValueLevel2.get().isEmpty()) {
                    showMessageDialog("请输入降雨量二级报警")
                    return
                }
                if (mStates.rainTriggerValueLevel3.get().isEmpty()) {
                    showMessageDialog("请输入降雨量三级报警")
                    return
                }
                if (mStates.waterTriggerValueLevel1.get().isEmpty()) {
                    showMessageDialog("请输入水位一级报警")
                    return
                }
                if (mStates.waterTriggerValueLevel2.get().isEmpty()) {
                    showMessageDialog("请输入水位二级报警")
                    return
                }
                if (mStates.waterTriggerValueLevel3.get().isEmpty()) {
                    showMessageDialog("请输入水位三级报警")
                    return
                }
                entity.duration = mStates.duration.get()
                entity.interval = mStates.interval.get()
                entity.volume = mStates.volume.get().toString()
                entity.rlevel1 = mStates.rainTriggerValueLevel1.get()
                entity.rlevel2 = mStates.rainTriggerValueLevel2.get()
                entity.rlevel3 = mStates.rainTriggerValueLevel3.get()
                entity.wlevel1 = mStates.waterTriggerValueLevel1.get()
                entity.wlevel2 = mStates.waterTriggerValueLevel2.get()
                entity.wlevel3 = mStates.waterTriggerValueLevel3.get()
            }

            3 -> {
                if (mStates.duration.get().isEmpty()) {
                    showMessageDialog("请输入显示时长")
                    return
                }
                if (mStates.interval.get().isEmpty()) {
                    showMessageDialog("请输入更新间隔")
                    return
                }
                if (mStates.screenTime.get().isEmpty()) {
                    showMessageDialog("请输入熄屏时长")
                    return
                }
                entity.type = (ledTypeList.indexOf(mStates.ledType.get()) + 1).toString()
                entity.duration = mStates.duration.get()
                entity.interval = mStates.interval.get()
                entity.stime = mStates.screenTime.get()
            }
        }
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RS485_PORT3_SENSOR_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_GET_RS485_PORT3_SENSOR_PARAM,
            "device=$sensorType"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_RS485_PORT3_SENSOR_PARAM -> {
                val result = iotParseManager.parse<MRRS485Port3SensorParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT3_SENSOR_PARAM
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

            IOTCommandType.MD_MR_SET_RS485_PORT3_SENSOR_PARAM -> {
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

    private fun initParamData(sensorParam: MRRS485Port3SensorParam) {
        try {
            mStates.status.set(if (sensorParam.status == "1") "已接入" else "未接入")
            mStates.isOpened.set(sensorParam.switch == "1")
            mStates.address.set(sensorParam.addr)
            mStates.baudRate.set(sensorParam.baud)
            mStates.dataBit.set(sensorParam.databit)
            mStates.checkBit.set(checkBitList[sensorParam.paritybit.toInt()])
            mStates.stopBit.set(stopBitList[sensorParam.stopbit.toInt()])
            when (sensorType) {
                1 -> {
                    sensorParam.svolt.toDoubleOrNull()?.let {
                        mStates.solarVoltage.set(decimalFormat.format(it))
                    }
                    sensorParam.bvolt.toDoubleOrNull()?.let {
                        mStates.batteryVoltage.set(decimalFormat.format(it))
                    }
                    sensorParam.spower.toDoubleOrNull()?.let {
                        mStates.solarPower.set(decimalFormat.format(it))
                    }
                    sensorParam.lpower.toDoubleOrNull()?.let {
                        mStates.loadPower.set(decimalFormat.format(it))
                    }
                }

                2 -> {
                    mStates.duration.set(sensorParam.duration)
                    mStates.interval.set(sensorParam.interval)
                    mStates.volume.set(sensorParam.volume.toInt())
                    //判断 sensorParam.rlevel1 是否可以转为 double
                    sensorParam.rlevel1.toDoubleOrNull()?.let {
                        mStates.rainTriggerValueLevel1.set(decimalFormat.format(it))
                    }
                    sensorParam.rlevel2.toDoubleOrNull()?.let {
                        mStates.rainTriggerValueLevel2.set(decimalFormat.format(it))
                    }
                    sensorParam.rlevel3.toDoubleOrNull()?.let {
                        mStates.rainTriggerValueLevel3.set(decimalFormat.format(it))
                    }
                    mStates.waterTriggerValueLevel1.set(sensorParam.wlevel1)
                    mStates.waterTriggerValueLevel2.set(sensorParam.wlevel2)
                    mStates.waterTriggerValueLevel3.set(sensorParam.wlevel3)
                }

                else -> {
                    mStates.ledType.set(ledTypeList[sensorParam.type.toInt() - 1])
                    mStates.duration.set(sensorParam.duration)
                    mStates.interval.set(sensorParam.interval)
                    mStates.screenTime.set(sensorParam.stime)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    private fun processBack(isPressBackBtn: Boolean = false) {
        launchAndRepeatWithViewLifecycle {
            if (isPressBackBtn) {
                mMessenger.requestStatusBarColor(if (statusBarColor == 0) R.color.colorPrimary else statusBarColor)
                nav().navigateUp()
                return@launchAndRepeatWithViewLifecycle
            }
            delay(1000)
            //巡护事件需要给上一级浏览页面传递最新的事件信息
            setFragmentResult(
                MR702RS485Port3Fragment.FRAGMENT_RESULT_REQUEST_KEY,
                bundleOf(MR702RS485Port3Fragment.REFRESH_DATA to true)
            )
            nav().navigateUp()
        }
    }

    companion object {
        const val SENSOR_TYPE = "sensor_type"
        fun newBundleArguments(
            sensorType: Int = 0,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putInt(SENSOR_TYPE, sensorType)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}