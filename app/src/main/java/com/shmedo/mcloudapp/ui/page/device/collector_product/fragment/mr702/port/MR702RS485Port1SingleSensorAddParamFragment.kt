package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702.port

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.extensions.hexStringToDecimalString
import com.shmedo.core.commonlib.extensions.stringToGBK16UByteString
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRRS485Port1SensorParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port1SingleSensorAddParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.MRRS485Port1
import com.shmedo.mcloudapp.model.MRSensorItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS485Port1SingleSensorParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject

class MR702RS485Port1SingleSensorAddParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs485Port1SingleSensorAddParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702RS485Port1SingleSensorParamViewModel by viewModels()
    private val portHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var sensorItem: MRSensorItem
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }
    private val dataFormatList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port1_sensor_data_format) }
    private val siteTypeList = mutableListOf("无", "测点", "参考点")
    private val calculateList = mutableListOf("不计算", "线性方程计算", "传感器联合计算")


    private var modelFieldIndex: Int = 0


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_rs485_port1_single_sensor_add_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs485Port1SingleSensorAddParamBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        registerOnBackPressedDispatcher {
            processBack(true)
        }
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            sensorItem = it.getParcelable(SENSOR_MODEL_ITEM)!!
        }
        binding.llToolbar.toolbar.title = sensorItem.sensorName

        portHomeViewModel.configPort4851SensorIDToSensorModelMap[sensorItem.sensorID]?.let {
            mStates.curSensorModel = it
        }
        mStates.isCustomSensor.set(mStates.curSensorModel.sensorID == "0")//是否自定义传感器

        mStates.modelName.set(mStates.curSensorModel.modelName)//物模型名称
        mStates.modelToken.set(mStates.curSensorModel.modelToken)//物模型编号
        mStates.address.set("1")//传感器地址,默认1
        resetDefaultParam()
    }

    /**
     * 重置采集项
     */
    private fun resetDefaultParam() {
        mStates.baudRate.set("9600")  //默认波特率
        mStates.dataBit.set(dataBitList[3])//默认数据位 8
        mStates.checkBit.set(checkBitList[0])//默认校验位 无
        mStates.stopBit.set(stopBitList[0])//默认停止位 1

        mStates.siteType.set(siteTypeList[0])//站点类型 默认无
        mStates.calculate.set(calculateList[0])//计算方式 默认不计算
        mStates.sensitivityK.set("1")//
        mStates.temperatureCorrectionCoefficientB.set("0")
        mStates.powValue.set("0")
        mStates.initialFrequencyF0.set("0")
        mStates.initialTemperatureT0.set("0")
        mStates.initialWaterLevel.set("0")
        mStates.initialMeasureValue.set("0")

        resetDefaultModelField1()
    }

    private fun resetDefaultModelField1() {
        //采集项名称
        mStates.modelFieldName.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[modelFieldIndex].fieldName
            else "采集项1"
        )
        //采集项单位
        mStates.modelFieldUnit.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[modelFieldIndex].engUnit
            else ""
        )
        //水文标识
        mStates.hydrologicalIdentification.set(
            if (checkModelFieldList()) {
                mStates.curSensorModel.modelFieldList[modelFieldIndex].hydrologicalIdentification
            } else ""
        )
        //采集指令
        mStates.collectionInstructions.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[modelFieldIndex].collectionInstructions
            else ""
        )
        //倍率
        mStates.ratio.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[modelFieldIndex].ratio
            else "1"
        )
        //数据类型
        mStates.dataFormat.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[modelFieldIndex].dataFormat
            else dataFormatList[0]
        )

        //触发值
        mStates.triggerValue.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[modelFieldIndex].triggerValue
            else "0"
        )
        //上限值
        mStates.upperLimit.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[modelFieldIndex].upperLimit
            else "100"
        )
        //下限值
        mStates.lowerLimit.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[modelFieldIndex].lowerLimit
            else "0"
        )
        //默认修正值 0
        mStates.correctValue.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[modelFieldIndex].correctValue
            else "0"
        )
        //阈值次数 3
        mStates.ngateval.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[modelFieldIndex].ngateval
            else "3"
        )
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择数据位
         */
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

        /**
         * 选择校验位
         */
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

        /**
         * 选择停止位
         */
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

        /**
         * 站点类型
         */
        fun onSiteTypeChooseClick() {
            val selectedIndex = siteTypeList.indexOf(mStates.siteType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择站点类型", siteTypeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.siteType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 计算方式
         */
        fun onIsCalculateChooseClick() {
            val selectedIndex = calculateList.indexOf(mStates.calculate.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择计算方式", calculateList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.calculate.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择数据类型
         */
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

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.modelName.get().isEmpty()) {
            showMessageDialog("请输入物模型名称")
            return
        }
        if (mStates.modelToken.get().isEmpty()) {
            showMessageDialog("请输入物模型编号")
            return
        }
        if (mStates.address.get().isEmpty()) {
            showMessageDialog("请输入地址")
            return
        }
        if (mStates.baudRate.get().isEmpty()) {
            showMessageDialog("请输入波特率")
            return
        }
        if (!checkModelField())
            return

        val entity = MRRS485Port1SensorParamEntity(
            model = mStates.modelToken.get() + "_" + mStates.address.get(),
            c_model = "1",
            num = modelFieldIndex.toString(),
            sensorlist = sensorItem.sensorID,
            baud = mStates.baudRate.get(),
            databit = mStates.dataBit.get(),
            parity = (checkBitList.indexOf(mStates.checkBit.get())).toString(),
            stopbit = (stopBitList.indexOf(mStates.stopBit.get())).toString(),
            baseflag = if (siteTypeList.indexOf(mStates.siteType.get()) == 0) IOTConstants.NULL_KEY else (siteTypeList.indexOf(
                mStates.siteType.get()
            ) - 1).toString(),
            calctype = calculateList.indexOf(mStates.calculate.get()).toString(),
            kvalue = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.sensitivityK.get() else
                IOTConstants.NULL_KEY,
            bvalue = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.temperatureCorrectionCoefficientB.get() else
                IOTConstants.NULL_KEY,
            powvalue = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.powValue.get() else
                IOTConstants.NULL_KEY,
            r0value = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialFrequencyF0.get() else
                IOTConstants.NULL_KEY,
            t0value = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialTemperatureT0.get() else
                IOTConstants.NULL_KEY,
            l0value = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialWaterLevel.get() else
                IOTConstants.NULL_KEY,
            lvalue = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialMeasureValue.get() else
                IOTConstants.NULL_KEY,
            initvalue = if (calculateList.indexOf(mStates.calculate.get()) == 2) mStates.initialValue.get() else
                IOTConstants.NULL_KEY,

            sgbk = mStates.modelName.get().stringToGBK16UByteString(),//传感器名称GBK编码
            mgbk = mStates.modelFieldName.get().stringToGBK16UByteString(),//采集项名称GBK编码
            egbk = mStates.modelFieldUnit.get().stringToGBK16UByteString(),//采集项单位GBK编码
            swtoken = mStates.hydrologicalIdentification.get().hexStringToDecimalString(),
            cmd = mStates.collectionInstructions.get(),
            ratio = mStates.ratio.get(),
            dataformat = (dataFormatList.indexOf(mStates.dataFormat.get())).toString(),
            gateval = mStates.triggerValue.get(),
            uplimit = mStates.upperLimit.get(),
            lowlimit = mStates.lowerLimit.get(),
            corrvalue = mStates.correctValue.get(),
            ngateval = mStates.ngateval.get(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS485_PORT1_SENSOR_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun checkModelField(): Boolean {
        if (mStates.modelFieldName.get().isEmpty()) {
            showMessageDialog("请输入采集项名称")
            return false
        }
        if (mStates.modelFieldUnit.get().isEmpty()) {
            showMessageDialog("请输入采集项单位")
            return false
        }
        if (mStates.hydrologicalIdentification.get().isEmpty()) {
            showMessageDialog("请输入水文标识")
            return false
        }
        if (mStates.collectionInstructions.get().isEmpty()) {
            showMessageDialog("请输入采集指令")
            return false
        }
        if (mStates.ratio.get().isEmpty()) {
            showMessageDialog("请输入倍率")
            return false
        }
        if (mStates.triggerValue.get().isEmpty()) {
            showMessageDialog("请输入触发值")
            return false
        }
        if (mStates.upperLimit.get().isEmpty()) {
            showMessageDialog("请输入上限值")
            return false
        }
        if (mStates.lowerLimit.get().isEmpty()) {
            showMessageDialog("请输入下限值")
            return false
        }
        if (mStates.correctValue.get().isEmpty()) {
            showMessageDialog("请输入修正值")
            return false
        }
        if (mStates.ngateval.get().isEmpty()) {
            Toaster.show("请输入阈值次数")
            return false
        }

        return true
    }

    private fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        commandType == IOTCommandType.MR_MD_SET_RS485_PORT1_SENSOR_PARAM

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
            IOTCommandType.MR_MD_SET_RS485_PORT1_SENSOR_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错：${result.message}"
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

    private fun processBack(isPressBackBtn: Boolean = false) {
        launchWithViewLifecycle {
            if (isPressBackBtn) {
                mMessenger.requestStatusBarColor(if (statusBarColor == 0) R.color.colorPrimary else statusBarColor)
                nav().navigateUp()
                return@launchWithViewLifecycle
            }

            delay(1500)
            //需要给上一级浏览页面传递最新的事件信息
            mMessenger.requestMR702Rs485PortSensorRefresh(MRRS485Port1)
            nav().navigateUp()
        }
    }


    private fun checkModelFieldList() =
        mStates.curSensorModel.modelFieldList.isNotEmpty() && modelFieldIndex < mStates.curSensorModel.modelFieldList.size

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        fun newInstance() = MR702RS485Port1SingleSensorAddParamFragment()
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
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}