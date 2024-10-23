package com.shmedo.mcloudapp.ui.page.device.adme.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.adme.AdmeBasicConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.adme.AdmeExecutiveAgencyInfoEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.adme.AdmeLockedRotorDetectionEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.adme.AdmeStepperMotorEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeBasicConfigInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeExecutiveAgencyInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeLockedRotorDetectionInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeStepperMotorInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAdmeBasicParamConfigBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.adapter.AdmeTimeAdapter
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.AdmeBasicParamConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import com.shmedo.mcloudapp.utils.IOTRegexContants
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.Date
import java.util.Locale

class AdmeBasicParamConfigFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeBasicParamConfigBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeBasicParamConfigViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val measureMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_measure_method) }
    private val settlementMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_settlement_method) }
    private val measIntervalPerRoundList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_measure_interval_per_rounds) }

    private val mData: MutableList<com.shmedo.mcloudapp.model.AdmeTimeItem> = ArrayList()
    private val mAdapter: AdmeTimeAdapter by lazy {
        AdmeTimeAdapter(
            requireContext(),
            mData
        )
    }

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

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
        binding = getBinding() as FragmentAdmeBasicParamConfigBinding
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
            if (isBleDisconnected()) {
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
            mAdapter.data.add(com.shmedo.mcloudapp.model.AdmeTimeItem(time))
            mAdapter.notifyItemInserted(mAdapter.data.size)
        }, 0, 0, true).show()

    }

    override fun initData() {
        super.initData()
        mStates.measureMethod.set(0)
        mStates.measureMethodText.set(measureMethodList[0])
        mStates.dataSettlementMethod.set(settlementMethodList[1])
        mStates.measurementIntervalPerRound.set(measIntervalPerRoundList[0])
        mStates.decentralizationWaitingTime.set("180")//下放等待时间
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
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            when (button.id) {
                R.id.downEnableSBtn -> { //下放使能
                    mStates.downEnable.set(isChecked)
                    enableOrDisableLockRotorParam(isChecked)
                }

                R.id.positiveAndNegativeSB -> { //正反测使能
                    mStates.positiveAndNegativeTest.set(isChecked)
                    enableOrDisableStepperMotorParam(isChecked)
                }
            }
        }

        fun onSubmitClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    /**
     *
     */
    private fun enableOrDisableLockRotorParam(isChecked: Boolean) {
        commandItems.clear()
        val entity = AdmeLockedRotorDetectionEntity(
            lowtbtss = if (isChecked) "1" else "0",//下放堵转缓停（0:关闭，1:开启）
            numpput = mStates.lockedRotorDetectionInfoWrapper.get().numpput,//单位时间脉冲数
            pdajtime = mStates.lockedRotorDetectionInfoWrapper.get().pdajtime,//脉冲检测判断时间
            lowsusranb = mStates.lockedRotorDetectionInfoWrapper.get().lowsusranb,//下放缓起区间终值(加速阶段)
            lowsusrana = mStates.lockedRotorDetectionInfoWrapper.get().lowsusrana,//下放缓停区间起始值(减速阶段)
            detintiona = mStates.lockedRotorDetectionInfoWrapper.get().detintiona,//堵转检测区间起始值
            detintionb = mStates.lockedRotorDetectionInfoWrapper.get().detintionb,//堵转检测区间终值
            lowtorblothr = mStates.lockedRotorDetectionInfoWrapper.get().lowtorblothr,//下放力矩堵转阈值
            lowtordetime = mStates.lockedRotorDetectionInfoWrapper.get().lowtordetime,//下放力矩检测判断时间
            uptbtss = mStates.lockedRotorDetectionInfoWrapper.get().uptbtss,//上拉堵转缓停（0:关闭，1:开启）
            upsusranb = mStates.lockedRotorDetectionInfoWrapper.get().upsusranb,//上拉缓起区间终值(加速阶段)
            upsusrana = mStates.lockedRotorDetectionInfoWrapper.get().upsusrana,//上拉缓停区间起始值(减速阶段)
            uptorblothr = mStates.lockedRotorDetectionInfoWrapper.get().uptorblothr,//下放力矩堵转阈值
            uptordetime = mStates.lockedRotorDetectionInfoWrapper.get().uptordetime,//下放力矩检测判断时间
        )
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION,
            entity.toCommandString()
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
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
        if (mStates.address.get().isEmpty()) {
            showMessageDialog("请输入Mac地址!")
            return
        }
        if (!RegexUtils.isMatch(
                IOTRegexContants.REGEX_MAC_ADDRESS_NO_COLON,
                mStates.address.get()
            )
        ) {
            showMessageDialog("请输入正确的Mac地址!")
            return
        }

        if (mStates.measureMethod.get() == 0) {
            if (mStates.waitingIntervalPerRound.get().isEmpty()) {
                showMessageDialog("请输入每轮等待时间!")
                return
            }
            try {
                val value = mStates.waitingIntervalPerRound.get().toDouble()
                if (value < 1) {
                    showMessageDialog("请输入正确的每轮等待时间!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的每轮等待时间!")
                return
            }
        }

        if (mStates.measureMethod.get() == 2) {
            if (mStates.intervalDays.get().isEmpty()) {
                showMessageDialog("请输入间隔时间!")
                return
            }
            try {
                val value = mStates.intervalDays.get().toDouble()
                if (value < 0) {
                    showMessageDialog("请输入正确的间隔时间!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的间隔时间!")
                return
            }
            if (mAdapter.data.size < 1) {
                showMessageDialog("请设置测量时间点!")
                return
            }
        }

        if (mStates.inclinometerTubeHoleDepth.get().isEmpty()) {
            showMessageDialog("请输入测斜管孔深!")
            return
        }
        try {
            val value = mStates.inclinometerTubeHoleDepth.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的测斜管孔深!")
            return
        }

        if (mStates.decentralizationSpeed.get().isEmpty()) {
            showMessageDialog("请输入电机下放速度!")
            return
        }
        try {
            val value = mStates.decentralizationSpeed.get().toDouble()
            if (value < 1 || value > 180) {
                showMessageDialog("电机下放速度数值范围[1,180]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("电机下放速度数值范围[1,180]!")
            return
        }

        if (mStates.decentralizationWaitingTime.get().isEmpty()) {
            showMessageDialog("请输入下放等待时间!")
            return
        }
        try {
            val value = mStates.decentralizationWaitingTime.get().toDouble()
            if (value < 30 || value > 86400) {
                showMessageDialog("下放等待时间数值范围[30,86400]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("下放等待时间数值范围[30,86400]!")
            return
        }

        val basicConfigEntity = AdmeBasicConfigEntity(
            inctype = mStates.basicConfigInfoWrapper.get().inctype,
            address = mStates.address.get(),
            interdeep = mStates.inclinometerTubeHoleDepth.get(),
            downspeed = mStates.decentralizationSpeed.get(),
            downwaitetime = mStates.decentralizationWaitingTime.get(),
            datatype = if (mStates.dataSettlementMethod.get() == settlementMethodList[0]) "0" else "1",
        )
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_BASIC,
            basicConfigEntity.toCommandString()
        )
        commandItems.add(command)

        val executiveAgencyInfoEntity = AdmeExecutiveAgencyInfoEntity(
            meastype = mStates.measureMethod.get().toString(),
            datatype = if (mStates.dataSettlementMethod.get() == settlementMethodList[0]) "0" else "1",
            datareply = mStates.executiveAgencyInfoWrapper.get().datareply,
            roundwaitetime = mStates.waitingIntervalPerRound.get(),
            roundmeasinval = mStates.measurementIntervalPerRound.get(),
            invalday = mStates.intervalDays.get(),
            roundmeasstart = if (mStates.measureMethod.get() == 2)
                mAdapter.data.joinToString("|") { it.time.substring(0, 2) }
            else
                mStates.startTimePerRound.get(),
            datainval = mStates.executiveAgencyInfoWrapper.get().datainval,
            clin_compen = mStates.executiveAgencyInfoWrapper.get().clin_compen,
            compensatetime = mStates.executiveAgencyInfoWrapper.get().compensatetime,
            interdeep = mStates.inclinometerTubeHoleDepth.get(),
            driveaddress = mStates.executiveAgencyInfoWrapper.get().driveaddress,
            downspeed = mStates.decentralizationSpeed.get(),
            downwaitetime = mStates.decentralizationWaitingTime.get(),
            upspeed = mStates.executiveAgencyInfoWrapper.get().upspeed,
            pzspeed = mStates.executiveAgencyInfoWrapper.get().pzspeed,
            measpacing = mStates.executiveAgencyInfoWrapper.get().measpacing,
            meaintertime = mStates.executiveAgencyInfoWrapper.get().meaintertime,
            meabaseth = mStates.executiveAgencyInfoWrapper.get().meabaseth,
            interval_compensation = mStates.executiveAgencyInfoWrapper.get().interval_compensation,
            bottom_safe_distance = mStates.executiveAgencyInfoWrapper.get().bottom_safe_distance,
            interval_fitting = mStates.executiveAgencyInfoWrapper.get().interval_fitting,
            point_offset = mStates.executiveAgencyInfoWrapper.get().point_offset,
        )
        command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_EXECUTIVE_AGENCY,
            executiveAgencyInfoEntity.toCommandString()
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
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
                        val errMsg = "查询基础配置参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        //设备版本不支持，隐藏编辑按钮
                        toolbarViewModel.toolbarIvActionVisible.set(!errMsg.contains("设备版本不支持"))
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBasicConfigInfo(result.data)
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
                        val errMsg = "查询执行机构参数出错: ${result.message}"
                        handleFailureResult(errMsg)
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
                        val errMsg = "查询堵转参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        //设备版本不支持，隐藏编辑按钮
                        toolbarViewModel.toolbarIvActionVisible.set(!errMsg.contains("设备版本不支持"))
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
//                        admeLockedRotorDetectionInfo = result.data
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
                        val errMsg = "查询步进电机参数出错: ${result.message}"
                        handleFailureResult(errMsg)
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

            IOTCommandType.ADME_MD_SET_BASIC -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置基础配置参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_EXECUTIVE_AGENCY -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        var errMsg = "设置执行机构参数出错: ${result.message}"
                        if (errMsg.contains("time_err"))
                            errMsg = errMsg.replaceFirst(
                                "(time_err)(:?)".toRegex(),
                                "一轮测量时间不能少于"
                            ) + "小时"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置堵转参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_STEPPER_MOTOR -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置步进电机参数出错: ${result.message}"
                        handleFailureResult(errMsg)
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
                        val errMsg = "保存出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initBasicConfigInfo(info: AdmeBasicConfigInfo) {
        mStates.basicConfigInfoWrapper.set(info)
        mStates.address.set(info.address)
    }

    /**
     * 初始化执行结构参数
     */
    private fun initExecutiveAgencyInfo(info: AdmeExecutiveAgencyInfo) {
        mStates.executiveAgencyInfoWrapper.set(info)
        try {
            mStates.measureMethodText.set(if (info.meastype == "0") measureMethodList[0] else measureMethodList[1])
            mStates.measureMethod.set(info.meastype.toInt())
            mStates.dataSettlementMethod.set(if (info.datatype == "0") settlementMethodList[0] else settlementMethodList[1])
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
            mAdapter.data.clear()
            info.roundmeasstart.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                .forEach { time ->
                    if (time.isNotEmpty()) {
                        mAdapter.data.add(
                            com.shmedo.mcloudapp.model.AdmeTimeItem(
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

            mStates.inclinometerTubeHoleDepth.set(
                DeviceStatusInfoProcessor.formatDoubleValue(
                    info.interdeep,
                    "",
                    2
                )
            )
            mStates.decentralizationSpeed.set(info.downspeed)
            mStates.decentralizationWaitingTime.set(info.downwaitetime)
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 初始化堵转检测参数
     */
    private fun initLockedRotorDetectionInfo(info: AdmeLockedRotorDetectionInfo) {
        mStates.lockedRotorDetectionInfoWrapper.set(info)
        mStates.downEnable.set(info.lowtbtss == "1")
    }

    /**
     * 初始化正反测使能参数
     */
    private fun initPositiveAndNegativeInfo(info: AdmeStepperMotorInfo) {
        mStates.positiveAndNegativeTest.set(info.posnegtest == "1")
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

}