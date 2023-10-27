package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRRS485Port1SensorParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port1SensorAddParamBinding
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.MRSensorItem
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS485Port1SensorAddParamViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat

class MR702RS485Port1SensorAddParamFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702Rs485Port1SensorAddParamBinding by lazy { getBinding() as FragmentMr702Rs485Port1SensorAddParamBinding }
    private val mStates: MR702RS485Port1SensorAddParamViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var sensorItem: MRSensorItem
    private val decimalFormat = DecimalFormat("#.#")
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }
    private val dataFormatList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port1_sensor_data_format) }
    private val solutionMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port1_sensor_solution_method) }

    private var modelFieldIndex: Int = 0

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_rs485_port1_sensor_add_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "RS485-1"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                processBack(true)
            }
        })
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            sensorItem = it.getParcelable(MR702RS485Port2SensorParamFragment.SENSOR_MODEL_ITEM)!!
        }
        mStates.modelField.set(sensorItem.modelFieldList[modelFieldIndex])
        mStates.sensorType.set(sensorItem.sensorType)
        mStates.sensorName.set(sensorItem.sensorName)
        mStates.modelToken.set(sensorItem.modelToken)
    }

    inner class ClickProxy : BaseClickProxy() {
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
                    { position, text ->
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
                    { position, text ->
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
                    { position, text ->
                        mStates.stopBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onDataFormatChooseClick() {
            val selectedIndex = dataFormatList.indexOf(mStates.dataFormat.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据类型", dataFormatList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataFormat.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSolutionMethodChooseClick() {
            val selectedIndex = solutionMethodList.indexOf(mStates.solutionMethod.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择解算", solutionMethodList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.solutionMethod.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSaveModelFieldClick() {
            if (mStates.saveModelFieldText.get() == "配置下一采集项") {
                mStates.saveModelFieldText.set("保存此模块")
                mStates.modelField.set(sensorItem.modelFieldList[modelFieldIndex])
            } else {
                if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                    Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                    return
                }
                initSaveCommand()
            }
        }

        fun onSubmitClick() {
            processBack()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.sensorAddress.get().isEmpty()) {
            Toaster.show("请输入传感器地址")
            return
        }
        if (mStates.modelToken.get().isEmpty()) {
            Toaster.show("请输入物模型")
            return
        }
        if (mStates.baudRate.get().isEmpty()) {
            Toaster.show("请输入波特率")
            return
        }
        if (mStates.hydrologicalIdentification.get().isEmpty()) {
            Toaster.show("请输入水文标识")
            return
        }
        if (mStates.collectionInstructions.get().isEmpty()) {
            Toaster.show("请输入采集指令")
            return
        }
        if (mStates.ratio.get().isEmpty()) {
            Toaster.show("请输入倍率")
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
        val entity = MRRS485Port1SensorParamEntity(
            c_model = "1",
            num = modelFieldIndex.toString(),
            sensoraddr = mStates.sensorAddress.get(),
            model = mStates.modelToken.get() + "_" + mStates.sensorAddress.get(),
            baud = mStates.baudRate.get(),
            databit = mStates.dataBit.get(),
            paritybit = (checkBitList.indexOf(mStates.checkBit.get()) + 1).toString(),
            stopbit = mStates.stopBit.get(),
            swtoken = mStates.hydrologicalIdentification.get(),
            cmd = mStates.collectionInstructions.get(),
            ratio = mStates.ratio.get(),
            dataformat = (dataFormatList.indexOf(mStates.dataFormat.get())).toString(),
            calctype = (solutionMethodList.indexOf(mStates.solutionMethod.get())).toString(),
            gateval = mStates.triggerValue.get(),
            uplimit = mStates.upperLimit.get(),
            lowlimit = mStates.lowerLimit.get(),
            corrvalue = mStates.correctValue.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RS485_PORT1_SENSOR_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_SET_RS485_PORT1_SENSOR_PARAM -> {
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
                            Toaster.show("已保存")
                            modelFieldIndex++
                            if (modelFieldIndex < sensorItem.modelFieldList.size - 1) {
                                mStates.saveModelFieldText.set("配置下一个采集项")
                            } else {
                                mStates.isConfirmBtnVisible.set(true)
                            }
                        }
                    }
                }
            }

            else -> {}
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
            //巡护事件需要给上一级浏览页面传递最新的事件信息
            setFragmentResult(
                MR702RS485Port1Fragment.FRAGMENT_RESULT_REQUEST_KEY,
                bundleOf(MR702RS485Port1Fragment.REFRESH_DATA to true)
            )
            nav().navigateUp()
        }
    }

    companion object {
        fun newInstance() = MR702RS485Port1SensorAddParamFragment()
        private const val SENSOR_MODEL_ITEM = "sensor_model_item"

        fun newBundleArguments(
            sensorItem: MRSensorItem,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(SENSOR_MODEL_ITEM, sensorItem)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }

}