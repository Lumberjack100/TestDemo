package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702.port

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
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
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port1TwoSensorAddParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.MRSensorItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS485Port1TwoSensorParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.UUID

class MR702RS485Port1TwoSensorAddParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs485Port1TwoSensorAddParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702RS485Port1TwoSensorParamViewModel by viewModels()
    private val portHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var sensorItem: MRSensorItem
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }
    private val dataFormatList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs485_sensor_data_format) }
    private val siteTypeList = mutableListOf("无", "测点", "参考点")
    private val calculateList = mutableListOf("不计算", "线性方程计算", "传感器联合计算")


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_rs485_port1_two_sensor_add_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs485Port1TwoSensorAddParamBinding
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
            mStates.defaultSensorModel = it
        }
        mStates.isCustomSensor.set(mStates.defaultSensorModel.sensorID == "0")//是否自定义传感器


        resetDefaultParam()
    }

    /**
     * 重置采集项
     */
    private fun resetDefaultParam() {
        val siteTypeIndex = siteTypeList.indexOf(mStates.defaultSensorModel.stationType)
        val defaultSiteType =
            if (siteTypeIndex in siteTypeList.indices) siteTypeList[siteTypeIndex] else siteTypeList[0]
        val calculateIndex = calculateList.indexOf(mStates.defaultSensorModel.calcType)
        val defaultCalculate =
            if (calculateIndex in calculateList.indices) calculateList[calculateIndex] else calculateList[0]

        mStates.modelName.set(mStates.defaultSensorModel.modelName)//物模型名称
        mStates.modelToken.set(mStates.defaultSensorModel.modelToken)//物模型编号
        mStates.address.set(mStates.defaultSensorModel.collAddr)//传感器地址,默认1

        mStates.baudRate.set(mStates.defaultSensorModel.baud)  //默认波特率
        mStates.dataBit.set(dataBitList[3])//默认数据位 8
        mStates.checkBit.set(checkBitList[0])//默认校验位 无
        mStates.stopBit.set(stopBitList[0])//默认停止位 1

        mStates.siteType.set(defaultSiteType)//站点类型
        mStates.calculate.set(defaultCalculate)//计算方式
        mStates.sensitivityK.set("1")//灵敏度K
        mStates.temperatureCorrectionCoefficientB.set("0")//温度修正系数 b
        mStates.powValue.set("1")//指数
        mStates.initialFrequencyF0.set("0")//初始频率 F0
        mStates.initialTemperatureT0.set("0")//初始温度 T0
        mStates.initialWaterLevel.set("0")//初始水位
        mStates.initialMeasureValue.set("0")//初始测量值
        mStates.initialValue.set("0")//初始测量值

        resetDefaultModelField1()
        resetDefaultModelField2()
    }

    private fun resetDefaultModelField1() {
        //采集项名称
        mStates.modelFieldName.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[0].fieldName
            else "采集项1"
        )
        //采集项单位
        mStates.modelFieldUnit.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[0].engUnit
            else ""
        )
        //水文标识
        mStates.hydrologicalIdentification.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[0].hydrologicalIdentification
            else ""
        )
        //采集指令
        mStates.collectionInstructions.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[0].collectionInstructions
            else ""
        )
        //倍率
        mStates.ratio.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[0].ratio
            else "1"
        )
        //数据类型
        mStates.dataFormat.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[0].dataFormat
            else dataFormatList[0]
        )
        //触发值
        mStates.triggerValue.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[0].triggerValue
            else "0"
        )
        //上限值
        mStates.upperLimit.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[0].upperLimit
            else "100"
        )
        //下限值
        mStates.lowerLimit.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[0].lowerLimit
            else "0"
        )
        //默认修正值 0
        mStates.correctValue.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[0].correctValue
            else "0"
        )
        //阈值次数 3
        mStates.ngateval.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[0].ngateval
            else "3"
        )
    }

    private fun resetDefaultModelField2() {
        //采集项名称
        mStates.modelFieldName2.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[1].fieldName
            else "采集项2"
        )
        //采集项单位
        mStates.modelFieldUnit2.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[1].engUnit
            else ""
        )
        //水文标识
        mStates.hydrologicalIdentification2.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[1].hydrologicalIdentification
            else ""
        )
        //采集指令
        mStates.collectionInstructions2.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[1].collectionInstructions
            else ""
        )
        //倍率
        mStates.ratio2.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[1].ratio
            else "1"
        )
        //数据类型
        mStates.dataFormat2.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[1].dataFormat
            else dataFormatList[0]
        )
        //触发值
        mStates.triggerValue2.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[1].triggerValue
            else "0"
        )
        //上限值
        mStates.upperLimit2.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[1].upperLimit
            else "100"
        )
        //下限值
        mStates.lowerLimit2.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[1].lowerLimit
            else "0"
        )
        //默认修正值 0
        mStates.correctValue2.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[1].correctValue
            else "0"
        )
        //阈值次数 3
        mStates.ngateval2.set(
            if (checkModelFieldList())
                mStates.defaultSensorModel.modelFieldList[1].ngateval
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
        fun onDataFormatChooseClick(view: View) {
            val selectedIndex =
                if (view.id == R.id.ll_data_process) dataFormatList.indexOf(mStates.dataFormat.get()) else dataFormatList.indexOf(
                    mStates.dataFormat2.get()
                )
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据类型", dataFormatList,
                    null, selectedIndex,
                    { position, text ->
                        if (view.id == R.id.ll_data_process)
                            mStates.dataFormat.set(text)
                        else
                            mStates.dataFormat2.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onSubmitButtonClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        val commands = mutableListOf<String>()

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

        if (!checkModelField2())
            return

        var entity = MRRS485Port1SensorParamEntity(
            model = mStates.modelToken.get() + "_" + mStates.address.get(),
            c_model = "1",
            num = "0",
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
            egbk = if (mStates.modelFieldUnit.get().isEmpty()) IOTConstants.NULL_KEY else
                mStates.modelFieldUnit.get().stringToGBK16UByteString(),//采集项单位GBK编码
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
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS485_PORT1_SENSOR_PARAM,
            entity.toCommandString()
        )
        commands.add(command)

        entity = MRRS485Port1SensorParamEntity(
            model = mStates.modelToken.get() + "_" + mStates.address.get(),
            c_model = "1",
            num = "1",
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
            mgbk = mStates.modelFieldName2.get().stringToGBK16UByteString(),//采集项名称GBK编码
            egbk = if (mStates.modelFieldUnit2.get().isEmpty()) IOTConstants.NULL_KEY else
                mStates.modelFieldUnit2.get().stringToGBK16UByteString(),//采集项单位GBK编码
            swtoken = mStates.hydrologicalIdentification2.get().hexStringToDecimalString(),
            cmd = mStates.collectionInstructions2.get(),
            ratio = mStates.ratio2.get(),
            dataformat = (dataFormatList.indexOf(mStates.dataFormat2.get())).toString(),
            gateval = mStates.triggerValue2.get(),
            uplimit = mStates.upperLimit2.get(),
            lowlimit = mStates.lowerLimit2.get(),
            corrvalue = mStates.correctValue2.get(),
            ngateval = mStates.ngateval2.get(),
        )
        command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS485_PORT1_SENSOR_PARAM,
            entity.toCommandString()
        )
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    private fun checkModelField(): Boolean {
        if (mStates.modelFieldName.get().isEmpty()) {
            showMessageDialog("请输入采集项名称")
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

    private fun checkModelField2(): Boolean {
        if (mStates.modelFieldName2.get().isEmpty()) {
            showMessageDialog("请输入采集项名称")
            return false
        }
        if (mStates.hydrologicalIdentification2.get().isEmpty()) {
            showMessageDialog("请输入水文标识")
            return false
        }
        if (mStates.collectionInstructions2.get().isEmpty()) {
            showMessageDialog("请输入采集指令")
            return false
        }
        if (mStates.ratio2.get().isEmpty()) {
            showMessageDialog("请输入倍率")
            return false
        }
        if (mStates.triggerValue2.get().isEmpty()) {
            showMessageDialog("请输入触发值")
            return false
        }
        if (mStates.upperLimit2.get().isEmpty()) {
            showMessageDialog("请输入上限值")
            return false
        }
        if (mStates.lowerLimit2.get().isEmpty()) {
            showMessageDialog("请输入下限值")
            return false
        }
        if (mStates.correctValue2.get().isEmpty()) {
            showMessageDialog("请输入修正值")
            return false
        }
        if (mStates.ngateval2.get().isEmpty()) {
            Toaster.show("请输入阈值次数")
            return false
        }

        return true
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_SET_RS485_PORT1_SENSOR_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错：${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            Toaster.show("数据保存成功")
                            processBack()
                        }
                    }
                }
            }

            else -> {

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
            delay(1000)

            // 使用优化的事件机制通知传感器添加
            val newSensor = MRSensorItem(
                sensorID = sensorItem.sensorID,
                sensorName = sensorItem.sensorName,
                modelToken = mStates.modelToken.get(),
                addr = mStates.address.get(),
                addrDesc = "地址-${mStates.address.get()}",
                isPlugin = true,// 新添加的传感器默认在线
                uuid = UUID.randomUUID().toString()
            )

            // 通过共享的 ViewModel 通知传感器更新
            portHomeViewModel.notifyPort1SensorUpdate(newSensor)

            Timber.d("通过 ViewModel 发送传感器更新事件: ${newSensor.sensorName}")

            nav().navigateUp()
        }
    }

    private fun checkModelFieldList() =
        mStates.defaultSensorModel.modelFieldList.isNotEmpty() && mStates.defaultSensorModel.modelFieldList.size > 1


    companion object {
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