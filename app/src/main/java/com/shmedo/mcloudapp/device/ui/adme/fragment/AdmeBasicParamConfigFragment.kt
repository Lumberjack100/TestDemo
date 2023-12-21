package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeStepperMotorEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeBasicConfigInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeExecutiveAgencyInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeLockedRotorDetectionInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeStepperMotorInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.databinding.FragmentAdmeBasicParamConfigBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.AdmeTimeItem
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.adme.adapter.AdmeTimeAdapter
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeBasicParamConfigViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date
import java.util.Locale

class AdmeBasicParamConfigFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentAdmeBasicParamConfigBinding by lazy { getBinding() as FragmentAdmeBasicParamConfigBinding }
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: AdmeBasicParamConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val measureMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_measure_method) }
    private val settlementMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_settlement_method) }
    private val measIntervalPerRoundList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_measure_interval_per_rounds) }

    private val mData: MutableList<AdmeTimeItem> = ArrayList()
    private val mAdapter: AdmeTimeAdapter by lazy {
        AdmeTimeAdapter(
            requireContext(),
            mData
        )
    }
    val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_basic_param_config,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "基础参数"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        toolbarViewModel.toolbarIvActionVisible.set(mMessenger.admeDeviceMode.get() == "0")
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        initRefresh()
        initTimeAdapter()
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

    private fun initTimeAdapter() {
        binding.rv.setup { }
        binding.rv.apply {
            addItemDecoration(
                RecycleViewDivider(
                    LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(8f), ColorUtils.getColor(
                        R.color.transparent
                    )
                )
            )
            adapter = mAdapter
            mAdapter.itemMax = 8
            mAdapter.setOnItemClickListener(object : AdmeTimeAdapter.OnItemClickListener {
                override fun onItemClick(v: View?, position: Int) {

                }

                override fun addItem() {
                    showTimePickerDialog()
                }
            })
        }
    }

    private fun showTimePickerDialog() {
        TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            val time = String.format(Locale.getDefault(), "%02d:00:00", hourOfDay)
            for (item in mAdapter.data) {
                if (item.time.contains(time)) {
                    Toaster.show("不能设置重复时间点!")
                    return@OnTimeSetListener
                }
            }
            mAdapter.data.add(AdmeTimeItem(time))
            mAdapter.notifyItemInserted(mAdapter.data.size)
        }, 0, 0, true).show()

    }

    override fun initData() {
        super.initData()
        mStates.measureMethod.set(0)
        mStates.measureMethodText.set(measureMethodList[0])
        mStates.dataSettlementMethod.set(settlementMethodList[1])
        mStates.measurementIntervalPerRound.set(measIntervalPerRoundList[0])
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

        /**
         * 测量方式
         */
        fun onMeasureMethodClick() {
            val selectedIndex = measureMethodList.indexOf(mStates.measureMethodText.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", measureMethodList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.measureMethodText.set(text)
                        mStates.measureMethod.set(
                            position
                        )
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 数据解算方式
         */
        fun onDataSettlementMethodClick() {
            val selectedIndex = settlementMethodList.indexOf(mStates.dataSettlementMethod.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", settlementMethodList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataSettlementMethod.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 每轮测量间隔
         */
        fun onMeasIntervalPerRoundsClick() {
            val selectedIndex =
                measIntervalPerRoundList.indexOf(mStates.measurementIntervalPerRound.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", measIntervalPerRoundList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.measurementIntervalPerRound.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            when (button.id) {
                R.id.downEnableSBtn -> { //下放使能
                    mStates.downEnable.set(isChecked)
                    if (!isChecked) {
                        showMessage("确定使下放计米堵转检测不生效？", "温馨提示", "确定", {
                            closeDownOrPullUpEnable()
                        }, "取消", {
                            mStates.downEnable.set(true)
                            (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                        })
                    }
                }

                R.id.positiveAndNegativeSB -> { //正反测使能
                    enableOrDisableStepperMotorParam(isChecked)
                }
            }
        }

        fun onSubmitClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    /**
     * 步进电机正反测使能
     */
    private fun enableOrDisableStepperMotorParam(isChecked: Boolean) {
        commandItems.clear()
        val entity = AdmeStepperMotorEntity(
            posnegtest = if (isChecked) "1" else "0",
        )
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_STEPPER_MOTOR,
            entity.toCommandString()
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {

    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        //获取设备的基础配置参数
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_BASIC
        )
        commandItems.add(command)

        //获取执行机构参数
        command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_EXECUTIVE_AGENCY
        )
        commandItems.add(command)

        //获取堵转检测参数
        command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION
        )
        commandItems.add(command)

        //获取设备的步进电机正反测使能信息
        command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_STEPPER_MOTOR
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_BASIC -> {
                val result = iotParseManager.parse<AdmeBasicConfigInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_BASIC
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询基础配置参数出错: ${result.message}"
                        Timber.e(errMsg)
                        PopTip.show(errMsg).autoDismiss(4500).iconError()
                        //设备版本不支持，隐藏编辑按钮
                        toolbarViewModel.toolbarIvActionVisible.set(!errMsg.contains("设备版本不支持"))
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initLockedRotorDetectionInfo(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_GET_EXECUTIVE_AGENCY -> {
                val result = iotParseManager.parse<AdmeExecutiveAgencyInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_EXECUTIVE_AGENCY
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询执行机构参数出错: ${result.message}"
                        Timber.e(errMsg)
                        PopTip.show(errMsg).autoDismiss(4500).iconError()
                        //设备版本不支持，隐藏编辑按钮
                        toolbarViewModel.toolbarIvActionVisible.set(!errMsg.contains("设备版本不支持"))
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initExecutiveAgencyInfo(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION -> {
                val result = iotParseManager.parse<AdmeLockedRotorDetectionInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询堵转参数出错: ${result.message}"
                        Timber.e(errMsg)
                        PopTip.show(errMsg).autoDismiss(4500).iconError()
                        //设备版本不支持，隐藏编辑按钮
                        toolbarViewModel.toolbarIvActionVisible.set(!errMsg.contains("设备版本不支持"))
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        admeLockedRotorDetectionInfo = result.data
                        initLockedRotorDetectionInfo(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_GET_STEPPER_MOTOR -> {
                val result = iotParseManager.parse<AdmeStepperMotorInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_STEPPER_MOTOR
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询步进电机参数出错: ${result.message}"
                        Timber.e(errMsg)
                        PopTip.show(errMsg).autoDismiss(4500).iconError()
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initPositiveAndNegativeInfo(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_EXECUTIVE_AGENCY -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        var errMsg = "设置执行机构参数出错: ${result.message}"
                        Timber.e(errMsg)
                        if (errMsg.contains("time_err"))
                            errMsg = errMsg.replaceFirst(
                                "(time_err)(:?)".toRegex(),
                                "一轮测量时间不能少于"
                            ) + "小时"
                        MessageDialog.show("提示", errMsg, "我已知晓")
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.MD_SAVE_CONFIG_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "保存出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun initBasicConfigInfo(info: AdmeBasicConfigInfo) {

    }

    /**
     * 初始化执行结构参数
     */
    private fun initExecutiveAgencyInfo(info: AdmeExecutiveAgencyInfo) {
        val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))
        try {
            mStates.measureMethodText.set(if (info.meastype == "0") measureMethodList[0] else measureMethodList[1])
            mStates.measureMethod.set(info.meastype.toInt())
            mStates.dataSettlementMethod.set(if (info.datatype == "0") settlementMethodList[0] else settlementMethodList[1])
            mStates.dataResponse.set(if (info.datareply == "0") dataResponseTypeList[0] else dataResponseTypeList[1])
            mStates.waitingIntervalPerRound.set(info.roundwaitetime)
            mStates.measurementIntervalPerRound.set(info.roundmeasinval)
            mStates.modifiedDate.set(
                TimeUtils.date2String(
                    Date(info.updatedate.toLong() * 1000),
                    "yyyy-MM-dd"
                )
            )
            mStates.intervalDays.set(info.invalday)
            mStates.startTimePerRound.set(info.roundmeasstart)
            info.roundmeasstart.split("|").let { times ->
                mAdapter.data.clear()
                times.forEach { time ->
                    if (time.isNotEmpty()) {
                        mAdapter.data.add(
                            AdmeTimeItem(
                                String.format(
                                    Locale.getDefault(),
                                    "%02d:00:00",
                                    time.toInt()
                                )
                            )
                        )
                    }
                }
                mAdapter.notifyDataSetChanged()
            }
            mStates.dataReadingInterval.set(info.datainval)
            mStates.measurementCompensationTime.set(info.compensatetime)
            mStates.motorDriveAddress.set(info.driveaddress)
            mStates.decentralizationSpeed.set(info.downspeed)
            decimalFormat.applyPattern("#.##")
            mStates.inclinometerTubeHoleDepth.set(decimalFormat.format(info.interdeep.toDouble()))
            mStates.decentralizationWaitingTime.set(info.downwaitetime)
            mStates.pullUpSpeed.set(info.upspeed)
            decimalFormat.applyPattern("#.##")
            mStates.measuringDistance.set(decimalFormat.format(info.measpacing.toDouble()))
            mStates.measurementIntervalTime.set(info.meaintertime)
            decimalFormat.applyPattern("#.##")
            mStates.measuringReferenceDepth.set(decimalFormat.format(info.meabaseth.toDouble()))
            decimalFormat.applyPattern("#.###")
            mStates.intervalCompensation.set(decimalFormat.format(info.interval_compensation.toDouble()))
            decimalFormat.applyPattern("#.###")
            mStates.bottomSafetyDistance.set(decimalFormat.format(info.bottom_safe_distance.toDouble()))
            decimalFormat.applyPattern("#.#")
            mStates.intervalFitting.set(decimalFormat.format(info.interval_fitting.toDouble()))
            decimalFormat.applyPattern("#.###")
            mStates.pointOffset.set(decimalFormat.format(info.point_offset.toDouble()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 初始化堵转检测参数
     */
    private fun initLockedRotorDetectionInfo(info: AdmeLockedRotorDetectionInfo) {

    }

    /**
     * 初始化正反测使能参数
     */
    private fun initPositiveAndNegativeInfo(info: AdmeStepperMotorInfo) {

    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

}