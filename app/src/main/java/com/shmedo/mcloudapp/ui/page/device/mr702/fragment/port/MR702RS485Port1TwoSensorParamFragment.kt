package com.shmedo.mcloudapp.ui.page.device.mr702.fragment.port

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.extensions.decimalStringToHexString
import com.shmedo.core.commonlib.extensions.gbkHexToString
import com.shmedo.core.commonlib.extensions.hexStringToDecimalString
import com.shmedo.core.commonlib.extensions.stringToGBK16UByteString
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRRS485Port1SensorParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1SensorParam
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1SensorParamWrapper
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port1TwoSensorParamBinding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.MRRS485Port1
import com.shmedo.mcloudapp.model.MRSensorItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.mr702.fragment.port.MR702RS485Port2SensorParamFragment.Companion.SENSOR_MODEL_ITEM
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS485Port1TwoSensorParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702RS485Port1TwoSensorParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs485Port1TwoSensorParamBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: MR702RS485Port1TwoSensorParamViewModel
    private lateinit var portHomeViewModel: MR702PortHomeViewModel
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var sensorItem: MRSensorItem
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }
    private val dataFormatList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port1_sensor_data_format) }
    private val siteTypeList = mutableListOf("无", "测点", "参考点")
    private val calculateList = mutableListOf("不计算", "线性方程计算", "传感器联合计算")


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
        portHomeViewModel = getActivityScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_rs485_port1_two_sensor_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs485Port1TwoSensorParamBinding
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
            sensorItem = it.getParcelable(SENSOR_MODEL_ITEM)!!
        }
        binding.llToolbar.toolbar.title = sensorItem.sensorName

        portHomeViewModel.sensorIdToSensorModelMap[sensorItem.sensorId]?.let {
            mStates.curSensorModel = it
        }

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
        mStates.sensitivityK.set("1")
        mStates.temperatureCorrectionCoefficientB.set("0")
        mStates.initialFrequencyF0.set("0")
        mStates.initialTemperatureT0.set("0")
        mStates.initialWaterLevel.set("0")
        mStates.initialMeasureValue.set("0")

        resetDefaultModelField1()
        resetDefaultModelField2()
    }

    private fun resetDefaultModelField1() {
        //采集项名称
        mStates.modelFieldName.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[0].fieldName
            else "采集项1"
        )
        //采集项单位
        mStates.modelFieldUnit.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[0].engUnit
            else ""
        )
        //水文识别
        mStates.hydrologicalIdentification.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[0].hydrologicalIdentification
            else ""
        )
        //采集指令
        mStates.collectionInstructions.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[0].collectionInstructions
            else ""
        )
        //倍率
        mStates.ratio.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[0].ratio
            else "1"
        )
        //数据类型
        mStates.dataFormat.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[0].dataFormat
            else dataFormatList[0]
        )
        //触发值
        mStates.triggerValue.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[0].triggerValue
            else "0"
        )
        //上限值
        mStates.upperLimit.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[0].upperLimit
            else "100"
        )
        //下限值
        mStates.lowerLimit.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[0].lowerLimit
            else "0"
        )
        //默认修正值 0
        mStates.correctValue.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[0].correctValue
            else "0"
        )
        //阈值次数 3
        mStates.ngateval.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[0].ngateval
            else "3"
        )
    }

    private fun resetDefaultModelField2() {
        //采集项名称
        mStates.modelFieldName2.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[1].fieldName
            else "采集项2"
        )
        //采集项单位
        mStates.modelFieldUnit2.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[1].engUnit
            else ""
        )
        //水文标识
        mStates.hydrologicalIdentification2.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[1].hydrologicalIdentification.decimalStringToHexString()
            else ""
        )
        //采集指令
        mStates.collectionInstructions2.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[1].collectionInstructions
            else ""
        )
        //倍率
        mStates.ratio2.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[1].ratio
            else "1"
        )
        //数据类型
        mStates.dataFormat2.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[1].dataFormat
            else dataFormatList[0]
        )
        //触发值
        mStates.triggerValue2.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[1].triggerValue
            else "0"
        )
        //上限值
        mStates.upperLimit2.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[1].upperLimit
            else "100"
        )
        //下限值
        mStates.lowerLimit2.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[1].lowerLimit
            else "0"
        )
        //默认修正值 0
        mStates.correctValue2.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[1].correctValue
            else "0"
        )
        //阈值次数 3
        mStates.ngateval2.set(
            if (checkModelFieldList())
                mStates.curSensorModel.modelFieldList[1].ngateval
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

        /** 选择停止位 */
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

        if (!checkModelField2())
            return

        var entity = MRRS485Port1SensorParamEntity(
            model = mStates.modelToken.get() + "_" + mStates.address.get(),
            c_model = "0",
            num = "0",
            sensorlist = sensorItem.sensorId,
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
            r0value = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialFrequencyF0.get() else
                IOTConstants.NULL_KEY,
            t0value = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialTemperatureT0.get() else
                IOTConstants.NULL_KEY,
            l0value = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialWaterLevel.get() else
                IOTConstants.NULL_KEY,
            lvalue = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialMeasureValue.get() else
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
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RS485_PORT1_SENSOR_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)

        entity = MRRS485Port1SensorParamEntity(
            model = mStates.modelToken.get() + "_" + mStates.address.get(),
            c_model = "0",
            sensorlist = sensorItem.sensorId,
            num = "1",
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
            r0value = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialFrequencyF0.get() else
                IOTConstants.NULL_KEY,
            t0value = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialTemperatureT0.get() else
                IOTConstants.NULL_KEY,
            l0value = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialWaterLevel.get() else
                IOTConstants.NULL_KEY,
            lvalue = if (calculateList.indexOf(mStates.calculate.get()) == 1) mStates.initialMeasureValue.get() else
                IOTConstants.NULL_KEY,

            sgbk = mStates.modelName.get().stringToGBK16UByteString(),//传感器名称GBK编码
            mgbk = mStates.modelFieldName2.get().stringToGBK16UByteString(),//采集项名称GBK编码
            egbk = mStates.modelFieldUnit2.get().stringToGBK16UByteString(),//采集项单位GBK编码
            swtoken = mStates.hydrologicalIdentification2.get(),
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
            IOTCommandType.MD_MR_SET_RS485_PORT1_SENSOR_PARAM,
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

    private fun checkModelField2(): Boolean {
        if (mStates.modelFieldName2.get().isEmpty()) {
            showMessageDialog("请输入采集项名称")
            return false
        }
        if (mStates.modelFieldUnit2.get().isEmpty()) {
            showMessageDialog("请输入采集项单位")
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

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR_PARAM,
            "model=${sensorItem.modelToken}_${sensorItem.addr}&index=0"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR_PARAM,
            "model=${sensorItem.modelToken}_${sensorItem.addr}&index=1"
        )
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR_PARAM -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR_PARAM
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

            IOTCommandType.MD_MR_SET_RS485_PORT1_SENSOR_PARAM -> {
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

    private fun initParamData(content: String) {
        launchWithViewLifecycle {
            try {
                val sensorParamWrapper = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<MRRS485Port1SensorParamWrapper>(content)
                } ?: return@launchWithViewLifecycle

                sensorParamWrapper.port1_param.let { portParam ->
                    val sensorParam: MRRS485Port1SensorParam = portParam[0]
                    mStates.sensorParamWrapper.set(sensorParam)

                    val strs = sensorParam.model.split("_")
                    mStates.address.set(strs[1])
                    mStates.modelToken.set(strs[0])
                    mStates.baudRate.set(sensorParam.baud)
                    mStates.dataBit.set(sensorParam.databit)
                    sensorParam.parity.toIntOrNull()?.let { value ->
                        if (value in checkBitList.indices) {
                            mStates.checkBit.set(checkBitList[value])
                        }
                    }
                    sensorParam.stopbit.toIntOrNull()?.let { value ->
                        if (value in stopBitList.indices) {
                            mStates.stopBit.set(stopBitList[value])
                        }
                    }
                    sensorParam.baseflag.toIntOrNull()?.let { value ->
                        val index = value + 1
                        if (index in siteTypeList.indices) {
                            mStates.siteType.set(siteTypeList[index])
                        }
                    }
                    sensorParam.calctype.toIntOrNull()?.let { value ->
                        if (value in calculateList.indices) {
                            mStates.calculate.set(calculateList[value])
                        }
                    }
                    mStates.sensitivityK.set(sensorParam.kvalue.replace("-nan", "0").replace("nan", "0"))
                    mStates.temperatureCorrectionCoefficientB.set(sensorParam.bvalue.replace("-nan", "0").replace("nan", "0"))
                    mStates.initialFrequencyF0.set(sensorParam.r0value.replace("-nan", "0").replace("nan", "0"))
                    mStates.initialTemperatureT0.set(sensorParam.t0value.replace("-nan", "0").replace("nan", "0"))
                    mStates.initialWaterLevel.set(sensorParam.l0value.replace("-nan", "0").replace("nan", "0"))
                    mStates.initialMeasureValue.set(sensorParam.lvalue.replace("-nan", "0").replace("nan", "0"))
                    mStates.initialValue.set(sensorParam.initvalue.replace("-nan", "0").replace("nan", "0"))

                    if (sensorParam.num == "0") {
                        mStates.hydrologicalIdentification.set(sensorParam.swtoken.decimalStringToHexString())
                        mStates.collectionInstructions.set(sensorParam.cmd)
                        mStates.ratio.set(sensorParam.ratio)
                        sensorParam.dataformat.toIntOrNull()?.let { value ->
                            if (value in dataFormatList.indices) {
                                mStates.dataFormat.set(dataFormatList[value])
                            }
                        }
                        mStates.triggerValue.set(sensorParam.gateval)
                        mStates.upperLimit.set(sensorParam.uplimit)
                        mStates.lowerLimit.set(sensorParam.lowlimit)
                        mStates.correctValue.set(sensorParam.corrvalue)
                        mStates.ngateval.set(sensorParam.ngateval)

                        if (sensorItem.sensorId == "0") {
                            mStates.modelName.set(sensorParam.sgbk.gbkHexToString())
                            mStates.modelFieldName.set(sensorParam.mgbk.gbkHexToString())
                            mStates.modelFieldUnit.set(sensorParam.egbk.gbkHexToString())
                        }
                    } else {
                        if (sensorItem.sensorId == "0") {
                            mStates.modelName.set(sensorParam.sgbk.gbkHexToString())
                            mStates.modelFieldName2.set(sensorParam.mgbk.gbkHexToString())
                            mStates.modelFieldUnit2.set(sensorParam.egbk.gbkHexToString())
                        }

                        mStates.hydrologicalIdentification2.set(sensorParam.swtoken.decimalStringToHexString())
                        mStates.collectionInstructions2.set(sensorParam.cmd)
                        mStates.ratio2.set(sensorParam.ratio)
                        sensorParam.dataformat.toIntOrNull()?.let { value ->
                            if (value in dataFormatList.indices) {
                                mStates.dataFormat2.set(dataFormatList[value])
                            }
                        }
                        mStates.triggerValue2.set(sensorParam.gateval)
                        mStates.upperLimit2.set(sensorParam.uplimit)
                        mStates.lowerLimit2.set(sensorParam.lowlimit)
                        mStates.correctValue2.set(sensorParam.corrvalue)
                        mStates.ngateval2.set(sensorParam.ngateval)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
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
            //需要给上一级浏览页面传递最新的事件信息
            mMessenger.requestMR702Rs485PortSensorRefresh(MRRS485Port1)
            nav().navigateUp()
        }
    }

    private fun checkModelFieldList() =
        mStates.curSensorModel.modelFieldList.isNotEmpty() && mStates.curSensorModel.modelFieldList.size > 1

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}