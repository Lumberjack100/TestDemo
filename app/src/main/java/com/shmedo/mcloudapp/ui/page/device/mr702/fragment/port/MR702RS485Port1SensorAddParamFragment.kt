package com.shmedo.mcloudapp.ui.page.device.mr702.fragment.port

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.model.AppConfigInfo
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRRS485Port1SensorParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port1SensorAddParamBinding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.extensions.stringToGBK16UByteString
import com.shmedo.mcloudapp.model.AppConfigContent
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.MRRS485Port1
import com.shmedo.mcloudapp.model.MRSensorItem
import com.shmedo.mcloudapp.model.ModelField
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS485Port1SensorAddParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702RS485Port1SensorAddParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs485Port1SensorAddParamBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: MR702RS485Port1SensorAddParamViewModel
    private lateinit var mInterfaceHomeViewModel: MR702PortHomeViewModel
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var sensorItem: MRSensorItem
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }
    private val dataFormatList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port1_sensor_data_format) }
    private val solutionMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port1_sensor_solution_method) }

    private var modelFieldIndex: Int = 0


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
        mInterfaceHomeViewModel = getActivityScopeViewModel()
    }

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
        binding = getBinding() as FragmentMr702Rs485Port1SensorAddParamBinding
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
            sensorItem = it.getParcelable(SENSOR_MODEL_ITEM)!!
        }
        binding.llToolbar.toolbar.title = "RS485-1-${sensorItem.sensorName}"

        mStates.isCustomSensor.set(sensorItem.modelFieldList.isEmpty())
        mStates.isFirstModelField.set(true)
        mStates.isSaveModelFieldBtnVisible.set(true)
        mStates.isConfirmBtnVisible.set(false)

        mStates.sensorName.set(sensorItem.sensorName)//传感器名称
        mStates.modelToken.set(sensorItem.modelToken)//物模型编号
        mStates.sensorAddress.set("")//传感器地址
        resetDefaultModelField()
    }

    /**
     * 重置采集项
     */
    private fun resetDefaultModelField() {
        mStates.baudRate.set("9600")  //默认波特率
        mStates.dataBit.set(dataBitList[3])//默认数据位 8
        mStates.checkBit.set(checkBitList[0])//默认校验位 无
        mStates.stopBit.set(stopBitList[0])//默认停止位 1

        mStates.modelFieldName.set(
            if (sensorItem.modelFieldList.isNotEmpty()) {
                sensorItem.modelFieldList[modelFieldIndex].fieldName
            } else {
                ""//采集项${modelFieldIndex + 1}
            }
        )
        mStates.modelFieldUnit.set(
            if (sensorItem.modelFieldList.isNotEmpty()) {
                sensorItem.modelFieldList[modelFieldIndex].engUnit
            } else {
                ""
            }
        )
        mStates.hydrologicalIdentification.set("")//水文识别
        mStates.collectionInstructions.set("")//采集指令
        mStates.ratio.set("1")//默认倍率 1
        mStates.dataFormat.set(dataFormatList[0])
        mStates.solutionMethod.set(solutionMethodList[0])//默认解算方法 加权平均
        mStates.triggerValue.set("0")//默认触发值 0
        mStates.upperLimit.set("100")//默认上限 100
        mStates.lowerLimit.set("0")//默认下限 0
        mStates.correctValue.set("0")//默认修正值 0
        mStates.ngateval.set("3")//默认阈值次数 3
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
            if (mStates.saveModelFieldBtnText.get() == "配置下一个采集项") {
                mStates.saveModelFieldBtnText.set("保存此采集项")
                resetDefaultModelField()
            } else {
                if (isBleDisconnected()) {
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
        if (mStates.sensorName.get().isEmpty()) {
            showMessageDialog("请输入物模型名称")
            return
        }
        if (mStates.modelToken.get().isEmpty()) {
            showMessageDialog("请输入物模型编号")
            return
        }
        if (mStates.sensorAddress.get().isEmpty()) {
            showMessageDialog("请输入地址")
            return
        }
        if (mStates.baudRate.get().isEmpty()) {
            showMessageDialog("请输入波特率")
            return
        }
        if (mStates.modelFieldName.get().isEmpty()) {
            showMessageDialog("请输入采集项名称")
            return
        }
        if (mStates.modelFieldName.get().isEmpty()) {
            showMessageDialog("请输入采集项单位")
            return
        }
        if (mStates.hydrologicalIdentification.get().isEmpty()) {
            showMessageDialog("请输入水文标识")
            return
        }
        if (mStates.collectionInstructions.get().isEmpty()) {
            showMessageDialog("请输入采集指令")
            return
        }
        if (mStates.ratio.get().isEmpty()) {
            showMessageDialog("请输入倍率")
            return
        }
        if (mStates.triggerValue.get().isEmpty()) {
            showMessageDialog("请输入触发值")
            return
        }
        if (mStates.upperLimit.get().isEmpty()) {
            showMessageDialog("请输入上限值")
            return
        }
        if (mStates.lowerLimit.get().isEmpty()) {
            showMessageDialog("请输入下限值")
            return
        }
        if (mStates.correctValue.get().isEmpty()) {
            showMessageDialog("请输入修正值")
            return
        }
        if (mStates.ngateval.get().isEmpty()) {
            Toaster.show("请输入阈值次数")
            return
        }
        val entity = MRRS485Port1SensorParamEntity(
            c_model = "1",
            num = modelFieldIndex.toString(),
            model = mStates.modelToken.get() + "_" + mStates.sensorAddress.get(),
            baud = mStates.baudRate.get(),
            databit = mStates.dataBit.get(),
            parity = (checkBitList.indexOf(mStates.checkBit.get())).toString(),
            stopbit = (stopBitList.indexOf(mStates.stopBit.get())).toString(),
            swtoken = mStates.hydrologicalIdentification.get(),
            cmd = mStates.collectionInstructions.get(),
            ratio = mStates.ratio.get(),
            dataformat = (dataFormatList.indexOf(mStates.dataFormat.get())).toString(),
            calctype = (solutionMethodList.indexOf(mStates.solutionMethod.get())).toString(),
            gateval = mStates.triggerValue.get(),
            uplimit = mStates.upperLimit.get(),
            lowlimit = mStates.lowerLimit.get(),
            corrvalue = mStates.correctValue.get(),
            ngateval = mStates.ngateval.get(),
            mgbk = mStates.modelFieldName.get().stringToGBK16UByteString(),//GBK编码
            egbk = mStates.modelFieldUnit.get().stringToGBK16UByteString(),
            sgbk = mStates.sensorName.get().stringToGBK16UByteString()

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
                        val errMsg = "设置参数出错：${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("已保存")
                            updateUnnamedSensorModel()
                            mStates.isFirstModelField.set(false)
                            modelFieldIndex++
                            //自定义物模型
                            if (mStates.isCustomSensor.get()) {
                                mStates.saveModelFieldBtnText.set("配置下一个采集项")
                                mStates.isConfirmBtnVisible.set(modelFieldIndex > 0)
                                return@sendCommandFromCmdList
                            }
                            //非自定义传感器
                            if (modelFieldIndex < sensorItem.modelFieldList.size) {
                                mStates.saveModelFieldBtnText.set("配置下一个采集项")
                            } else {
                                mStates.isSaveModelFieldBtnVisible.set(false)
                                mStates.isConfirmBtnVisible.set(true)
                            }
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    /**
     * 更新未命名传感器的物模型,需要保存到本地配置中
     */
    private fun updateUnnamedSensorModel() {
        if (!mStates.isCustomSensor.get())
            return

        if (modelFieldIndex == 0) {
            mStates.curSensorModel.sensorName = mStates.sensorName.get()
            mStates.curSensorModel.modelToken = mStates.modelToken.get()
            mStates.curSensorModel.sensorType = mStates.modelToken.get()
            mStates.curModelFieldList.clear()
        }
        mStates.curModelFieldList.add(
            ModelField(
                fieldName = mStates.modelFieldName.get(),
                engUnit = ""
            )
        )
    }

    private fun processBack(isPressBackBtn: Boolean = false) {
        launchWithViewLifecycle {
            if (isPressBackBtn) {
                mMessenger.requestStatusBarColor(if (statusBarColor == 0) R.color.colorPrimary else statusBarColor)
                nav().navigateUp()
                return@launchWithViewLifecycle
            }
            updateSensorModeConfig()
            delay(500)
            //需要给上一级浏览页面传递最新的事件信息
            mMessenger.requestMR702Rs485PortSensorRefresh(MRRS485Port1)
            nav().navigateUp()
        }
    }

    /**
     * 更新传感器配置信息
     */
    private suspend fun updateSensorModeConfig() {
        try {
            if (!mStates.isCustomSensor.get())
                return

            mStates.curSensorModel.modelFieldList = mStates.curModelFieldList
            mInterfaceHomeViewModel.portSensorModelListMap["485port1"]?.add(mStates.curSensorModel)
            mInterfaceHomeViewModel.sensorModelMap[mStates.curSensorModel.sensorType] =
                mStates.curSensorModel

            withContext(Dispatchers.IO) {
                val localAppConfigInfo: AppConfigInfo = MmkvCacheUtil.getAppConfigInfo()!!
                val jsonStr = localAppConfigInfo.configPara.replace("\\", "")
                MoshiUtil.fromJson<AppConfigContent>(jsonStr)
                    ?.let { appConfigContent: AppConfigContent ->
                        appConfigContent.mr702.first { it.portName == "485port1" }.sensors =
                            mInterfaceHomeViewModel.portSensorModelListMap["485port1"]!!
                        //将 " 转换为 \"
                        localAppConfigInfo.configPara =
                            MoshiUtil.toJson(appConfigContent).replace("\"", "\\\"")
                        localAppConfigInfo.lastTime = TimeUtils.getNowString()
                        //Timber.d("configPara = ${localAppConfigInfo.configPara}")
                        MmkvCacheUtil.setAppConfigInfo(localAppConfigInfo)
                    }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        fun newInstance() = MR702RS485Port1SensorAddParamFragment()
        private const val SENSOR_MODEL_ITEM = "sensor_model_item"

        fun newBundleArguments(
            sensorItem: MRSensorItem,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(SENSOR_MODEL_ITEM, sensorItem)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(com.shmedo.core.commonlib.utils.AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}