package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702.port

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRRS485Port3SensorParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port3SensorParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port3SensorParamBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS485Port3SensorParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
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

    private val volumeList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs485_port3_volume) }
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
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        registerOnBackPressedDispatcher {
            processBack(true)
        }

        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
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
            binding.llToolbar.toolbar.title =
                if (sensorType == 1) "太阳能控制器" else if (sensorType == 2) "声光报警器" else "LED屏"
        }
        initDefaultParam()
    }

    /**
     * 初始化默认参数
     */
    private fun initDefaultParam() {
        mStates.baudRate.set("9600")
        mStates.dataBit.set(dataBitList[3])
        mStates.checkBit.set(checkBitList[0])
        mStates.stopBit.set(stopBitList[0])

        mStates.volume.set(volumeList[0])
        mStates.ledType.set(ledTypeList[0])
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
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

        fun onVolumeChooseClick() {
            val selectedIndex = volumeList.indexOf(mStates.volume.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择音量", volumeList,
                    null, selectedIndex,
                    { _, text ->
                        mStates.volume.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onLedTypeChooseClick() {
            val selectedIndex = ledTypeList.indexOf(mStates.ledType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择屏幕规格", ledTypeList,
                    null, selectedIndex,
                    { _, text ->
                        mStates.ledType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
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
            IOTCommandType.MR_MD_SET_RS485_PORT3_SENSOR_PARAM,
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
                entity.type = if (mStates.ledType.get() == "P10") "1" else "4"
                entity.duration = mStates.duration.get()
                entity.interval = mStates.interval.get()
                entity.stime = mStates.screenTime.get()
            }
        }
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS485_PORT3_SENSOR_PARAM,
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
            IOTCommandType.MR_MD_GET_RS485_PORT3_SENSOR_PARAM,
            "device=$sensorType"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.MR_MD_GET_RS485_PORT3_SENSOR_PARAM)
                || (commandType == IOTCommandType.MR_MD_SET_RS485_PORT3_SENSOR_PARAM)

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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
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
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_SET_RS485_PORT3_SENSOR_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            processBack()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initParamData(sensorParam: MRRS485Port3SensorParam) {
        try {
            mStates.status.set(
                when (sensorParam.device.toInt()) {
                    1 -> if (sensorParam.status == "1") "已接入" else "未接入"
                    else -> if (sensorParam.switch == "1") "已接入" else "未接入"
                }
            )
            mStates.isOpened.set(sensorParam.switch == "1")
            mStates.address.set(sensorParam.addr)
            mStates.baudRate.set(sensorParam.baud)
            mStates.dataBit.set(sensorParam.databit)
            sensorParam.paritybit.toInt().let {
                if (it in checkBitList.indices) {
                    mStates.checkBit.set(checkBitList[it])
                }
            }
            sensorParam.stopbit.toInt().let {
                if (it in stopBitList.indices) {
                    mStates.stopBit.set(stopBitList[it])
                }
            }
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

                else -> {
                    if (sensorParam.type.toInt() == 1)
                        mStates.ledType.set(ledTypeList[0])
                    else
                        mStates.ledType.set(ledTypeList[1])

                    mStates.duration.set(sensorParam.duration)
                    mStates.interval.set(sensorParam.interval)
                    mStates.screenTime.set(sensorParam.stime)
                }
            }
        } catch (e: Exception) {
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    private fun processBack(isPressBackBtn: Boolean = false) {
        launchWithViewLifecycle {
            if (isPressBackBtn) {
                mMessenger.requestStatusBarColor(if (statusBarColor == 0) R.color.colorPrimary else statusBarColor)
                nav().navigateUp()
                return@launchWithViewLifecycle
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
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putInt(SENSOR_TYPE, sensorType)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}