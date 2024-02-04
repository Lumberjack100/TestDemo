package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.CompoundButton
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kongzue.dialogx.dialogs.PopTip
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeAutoMeasuringHoleDepthEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeLockedRotorDetectionEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeMeasuringHoleDepthEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeStepperMotorEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeLockedRotorDetectionInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMeasuringHoleDepthInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMotorMotionDistanceInfo
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
import com.shmedo.mcloudapp.common.ext.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentAdmeMeasuringHoleDepthBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeMeasuringHoleDepthViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.math.abs

class AdmeMeasuringHoleDepthFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeMeasuringHoleDepthBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeMeasuringHoleDepthViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val measureWayList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_measure_hole_depth_method) }
    private val motionTypeList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_measure_hole_depth_motor_motion_type) }

    private var safeDistance: String = "" //安全距离补偿
    private var lastMotionDistance: String = "" //上次停止时运动距离
    private var continueDistanceGoal: Double = 0.0 //继续运动时的目标距离
    private var repeatPollNum = 0 //当查询电机脉冲数重复超过一定次数时，判定电机停止

    private var autoMeasuringHoleDepthBottomDialog: AdmeAutoMeasuringHoleDepthBottomDialog? = null
    private var manualMeasuringHoleDepthBottomDialog: AdmeManualMeasuringHoleDepthBottomDialog? =
        null



    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_measuring_hole_depth,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeMeasuringHoleDepthBinding
        binding.llToolbar.toolbar.title = "孔深测量"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        toolbarViewModel.toolbarIvActionVisible.set(false)
        initRefresh()
        initTextChangedListener()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryParamData()
        }
    }

    private fun initTextChangedListener() {
        binding.etMotorSpeed.addTextChangedListener(afterTextChanged = { text: Editable? ->
            if (text.isNullOrEmpty()) {
                return@addTextChangedListener
            }
            if (mStates.motionType.get() == motionTypeList[0]) {
                val speed = text.toString().toInt()
                if (speed > 10) {
                    showMessageDialog("上拉触发磁开关最大速度为 10！")
                }
            }
        })
    }

    override fun initData() {
        super.initData()
        mStates.isAutoMode.set(true)
        mStates.measureWay.set(measureWayList[0])
        mStates.motionType.set(motionTypeList[0])
        mStates.downEnable.set(true)//进入页面默认自动测孔深，需要打开堵转检测使能
        mStates.isClearMotionDataVisible.set(true)
        loadAutoLastHistoryData()
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
         * 测量孔深模式
         */
        fun onMeasureModeClick() {
            val selectedIndex = measureWayList.indexOf(mStates.measureWay.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", measureWayList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.measureWay.set(text)
                        mStates.isAutoMode.set(position == 0)
                        if (position == 0) {
                            loadAutoLastHistoryData()
                            //自动测量孔深模式，需要打开堵转检测
                            if (mStates.lockedRotorDetectionInfoWrapper.get().lowtbtss == "0") {
                                mStates.downEnable.set(true)
                                enableOrDisableLockRotorParam(true)
                            }
                        } else
                            loadManualLastHistoryData()
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
                R.id.downEnableSBtn -> { //下放堵转检测使能
                    mStates.downEnable.set(isChecked)
                    enableOrDisableLockRotorParam(isChecked)
                }

                R.id.positiveAndNegativeSB -> { //正反测使能
                    mStates.positiveAndNegativeTest.set(isChecked)
                    enableOrDisableStepperMotorParam(isChecked)
                }
            }
        }

        /**
         * 选择运动方式
         */
        fun onMotionTypeClick() {
            val selectedIndex = motionTypeList.indexOf(mStates.motionType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", motionTypeList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.motionType.set(text)
                        loadManualLastHistoryData()
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onRunClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }

        fun onClearDataClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确认清除设备运动记录数据吗?", "温馨提示", "确定", {
                clearMotorMotionData()
            }, "取消")
        }
    }

    /**
     * 自动测孔深模式加载本地缓存的参数
     */
    private fun loadAutoLastHistoryData() {
        mStates.speed.set(MmkvCacheUtil.getAdmeAutoLastMotorDropSpeed())
    }

    /**
     * 手动测孔深模式加载本地缓存的参数
     */
    private fun loadManualLastHistoryData() {
        if (mStates.motionType.get() == motionTypeList[0]) {//上拉
            mStates.speed.set(MmkvCacheUtil.getAdmeManualLastMotorPullUpSpeed())
            mStates.distanceGoal.set(MmkvCacheUtil.getAdmeManualLastMotorPullUpDistance())
        } else {
            mStates.speed.set(MmkvCacheUtil.getAdmeManualLastMotorDropSpeed())
            mStates.distanceGoal.set(MmkvCacheUtil.getAdmeManualLastMotorDropDistance())
        }
    }

    /**
     * ADME的电机运动堵转检测使能
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
        if (mStates.speed.get().isEmpty()) {
            showMessageDialog("请输入电机速度!")
            return
        }
        try {
            val value = mStates.speed.get().toDouble()
            if (value < 1 || value > 100) {
                showMessageDialog("电机速度数值范围[1,100]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("电机速度数值范围[1,100]!")
            return
        }
        if (!mStates.isAutoMode.get()) {//手动测量孔深模式
            if (mStates.distanceGoal.get().isEmpty()) {
                showMessageDialog("请输入运动距离!")
                return
            }
            try {
                val value = mStates.distanceGoal.get().toDouble()
                if (value < 0) {
                    showMessageDialog("运动距离不能小于 0!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的运动距离!")
                return
            }
        }
        if (mStates.isAutoMode.get())
            setAutoMeasuringHoleDepth()
        else
            setManualMeasuringHoleDepth()
    }

    /**
     * 自动测孔深配置参数
     */
    private fun setAutoMeasuringHoleDepth() {
        //持久化保存电机速度
        MmkvCacheUtil.setAdmeAutoLastMotorDropSpeed(mStates.speed.get())
        val entity = AdmeAutoMeasuringHoleDepthEntity(mStates.speed.get())

        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_AUTO_MEASURING_HOLEDEPTH,
            entity.toCommandString()
        )
        commandItems.add(command)

        mStates.realHoleDepth.set("0")
        mStates.recommendHoleDepth.set("0")

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 手动测孔深配置参数
     */
    private fun setManualMeasuringHoleDepth() {
        if (mStates.motionType.get() == motionTypeList[0]) {//上拉
            MmkvCacheUtil.setAdmeManualLastMotorPullUpSpeed(mStates.speed.get())
            MmkvCacheUtil.setAdmeManualLastMotorPullUpDistance(mStates.distanceGoal.get())
        } else {
            MmkvCacheUtil.setAdmeManualLastMotorDropSpeed(mStates.speed.get())
            MmkvCacheUtil.setAdmeManualLastMotorDropDistance(mStates.distanceGoal.get())
        }
        val entity = AdmeMeasuringHoleDepthEntity(
            movementway = if (mStates.motionType.get() == motionTypeList[0]) "0" else "1",
            motorspeed = mStates.speed.get(),
            movedistance = mStates.distanceGoal.get()
        )
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_MEASURING_HOLEDEPTH,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 查询ADME测孔深运动的脉冲数、运动距离
     */
    private fun getMotorMotionData(timeMillis: Long = 0L) {
        launchWithViewLifecycle {
            delay(timeMillis)

            commandItems.clear()
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE
            )
            commandItems.add(command)
            sendCommandFromCmdList(isStartTimeoutJob = true)
        }
    }

    /**
     * 获取电机运动配置参数
     */
    private fun queryMotorMotionConfig() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 停止或者暂停电机运动
     */
    private fun stopMotorMotion() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_STOP_MEASURING_HOLEDEPTH
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 继续电机运动
     */
    private fun continueMotorMotion() {
        try {
            val distanceTotalGoal = abs(mStates.distanceGoal.get().toDouble())
            val distanceDiff =
                abs(mStates.motionDistance.get().toDouble()) - abs(lastMotionDistance.toDouble())
            //已达到设定运动目标
            if (distanceTotalGoal - distanceDiff <= 0) {
                mStates.isExitButtonVisible.set(true)
                Toaster.show("无法继续电机运动操作")
                return
            }
            continueDistanceGoal = distanceTotalGoal - distanceDiff
            val entity = AdmeMeasuringHoleDepthEntity(
                movementway = if (mStates.motionType.get() == motionTypeList[0]) "0" else "1",
                movedistance = continueDistanceGoal.toString()
            )
            commandItems.clear()
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.ADME_MD_SET_MEASURING_HOLEDEPTH,
                entity.toCommandString()
            )
            commandItems.add(command)
            sendCommandFromCmdList(isStartTimeoutJob = true)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 清空电机运动数据记录
     */
    private fun clearMotorMotionData() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryParamData() {
        commandItems.clear()

        //获取设备的步进电机正反测使能信息
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_STEPPER_MOTOR
        )
        commandItems.add(command)

        //获取堵转检测参数
        command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
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
                        mStates.positiveAndNegativeTest.set(result.data.posnegtest == "1")
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
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        mStates.lockedRotorDetectionInfoWrapper.set(result.data)
                        //进入页面默认自动测量孔深模式，需要打开堵转检测
                        if (result.data.lowtbtss == "0") {
                            enableOrDisableLockRotorParam(true)
                        }
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置堵转参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
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
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置步进电机参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
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
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_AUTO_MEASURING_HOLEDEPTH -> {//设置自动测量孔深参数
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "自动测量孔深出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        showAutoMotorMotionBottomDialog()
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_MEASURING_HOLEDEPTH -> {//设置手动测量孔深配置参数
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "手动测量孔深出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        showManualMotorMotionBottomDialog()
                    }
                }
            }

            IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH -> {
                val result = iotParseManager.parse<AdmeMeasuringHoleDepthInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取测量孔深配置参数出错: ${result.message}"
                        Timber.e(errMsg)
                        PopTip.show(errMsg).autoDismiss(4500).iconError()
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        //初次进入页面，初始化测量孔深配置参数
                        if (manualMeasuringHoleDepthBottomDialog == null && autoMeasuringHoleDepthBottomDialog == null) {
                            return
                        }
                        //当轮询 N 次电机脉冲数据没有变化时，根据电机运动状态进行后续处理
                        if (mStates.isAutoMode.get()) {
                            processAutoMotorMotionState(result.data)
                        } else {
                            processManualMotorMotionState(result.data)
                        }
                    }
                }
            }

            IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE -> {//查询ADME测孔深运动的脉冲数、运动距离
                val result = iotParseManager.parse<AdmeMotorMotionDistanceInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取电机的实时运动数据出错: ${result.message}"
                        Timber.e(errMsg)
                        PopTip.show(errMsg).autoDismiss(4500).iconError()
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initMotorMotionDistance(result.data)
                        if (mStates.isAutoMode.get()) {
                            autoMeasuringHoleDepthBottomDialog?.let { dialog ->
                                if (dialog.isResumed) {
                                    updateMotionData(result.data)
                                }
                            }
                        } else {
                            manualMeasuringHoleDepthBottomDialog?.let { dialog ->
                                if (dialog.isResumed) {
                                    updateMotionData(result.data)
                                }
                            }
                        }
                    }
                }
            }

            IOTCommandType.ADME_MD_STOP_MEASURING_HOLEDEPTH -> {//停止电机运动
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "停止电机出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        if (mStates.isAutoMode.get()) {
                            mStates.isExitButtonVisible.set(true)
                        } else {
                            if (mStates.isStopAction.get()) {
                                mStates.isStopAction.set(false)
                                mStates.isExitButtonVisible.set(true)
                                return
                            }
                            if (mStates.pauseButtonText.get() != "继续")//不是暂停按钮操作，是停止按钮操作
                                mStates.isExitButtonVisible.set(true)
                        }
                    }
                }
            }

            IOTCommandType.ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA -> {//ADME测量孔深清空数据
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "清空数据出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        mStates.isClearMotionDataVisible.set(false)
                        getMotorMotionData()
                    }
                }
            }

            else -> {}
        }
    }

    private fun initMotorMotionDistance(motorMotionDistanceInfo: AdmeMotorMotionDistanceInfo) {
        try {
            if (lastMotionDistance.isEmpty())
                lastMotionDistance = motorMotionDistanceInfo.realmovedistance
            if (safeDistance.isEmpty())
                safeDistance = motorMotionDistanceInfo.realholedepth
            if (motorMotionDistanceInfo.realholedepth.isNotEmpty() && safeDistance.isNotEmpty()) {
                val holeValue = abs(motorMotionDistanceInfo.realholedepth.toDouble())
                val safeValue = abs(safeDistance.toDouble())
                //测孔深值不等于安全补偿距离表示测孔深值有效
                if (holeValue != safeValue) {
                    mStates.realHoleDepth.set(motorMotionDistanceInfo.realholedepth)
                    mStates.recommendHoleDepth.set(motorMotionDistanceInfo.recoholedepth)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun showAutoMotorMotionBottomDialog() {
        autoMeasuringHoleDepthBottomDialog =
            AdmeAutoMeasuringHoleDepthBottomDialog.newInstance().apply {
                setOnDialogFragmentClickListener(object :
                    AdmeAutoMeasuringHoleDepthBottomDialog.OnDialogFragmentClickListener {
                    override fun onCloseClick() {
                        autoMeasuringHoleDepthBottomDialog = null
                        lastMotionDistance = ""
                        safeDistance = ""
                        //蓝牙未断开时先发送停止电机指令
                        if (bleViewModel.isConnected()) {
                            stopMotorMotion()
                        }
                        dismiss()
                    }

                    override fun onStopClick() {
                        if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                            return
                        }
                        stopMotorMotion()
                    }

                    override fun onExitClick() {
                        autoMeasuringHoleDepthBottomDialog = null
                        lastMotionDistance = ""
                        safeDistance = ""
                        dismiss()
                    }
                })
            }
        autoMeasuringHoleDepthBottomDialog?.show(childFragmentManager, "dialog")
        mStates.isClearMotionDataVisible.set(true)
        getMotorMotionData(800)
    }

    private fun showManualMotorMotionBottomDialog() {
        //数据运行弹框已经显示了
        if (manualMeasuringHoleDepthBottomDialog != null && manualMeasuringHoleDepthBottomDialog!!.isVisible) {
            Timber.d(
                "Continue Motion: lastDistance=%s,curDistance=%s,curPulse=%s,continueDistanceGoal=%s",
                lastMotionDistance,
                mStates.motionDistance.get(),
                mStates.motionPulse.get(),
                continueDistanceGoal,
            )
            getMotorMotionData(800)
            return
        }
        Timber.d(
            "start Motion: lastDistance=%s,totalDistanceGoal=%s",
            lastMotionDistance,
            mStates.distanceGoal.get()
        )
        manualMeasuringHoleDepthBottomDialog =
            AdmeManualMeasuringHoleDepthBottomDialog.newInstance().apply {
                setOnDialogFragmentClickListener(object :
                    AdmeManualMeasuringHoleDepthBottomDialog.OnDialogFragmentClickListener {
                    override fun onCloseClick() {
                        manualMeasuringHoleDepthBottomDialog = null
                        lastMotionDistance = ""
                        safeDistance = ""
                        //蓝牙未断开时先发送停止电机指令
                        if (bleViewModel.isConnected()) {
                            stopMotorMotion()
                        }
                        dismiss()
                    }

                    override fun onStopClick() {
                        if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                            return
                        }
                        stopMotorMotion()
                    }

                    override fun onPauseClick() {
                        if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                            return
                        }
                        if (mStates.pauseButtonText.get() == "继续") {
                            mStates.pauseButtonText.set("暂停")
                            continueMotorMotion()
                        } else {
                            mStates.pauseButtonText.set("继续")
                            stopMotorMotion()
                        }
                    }

                    override fun onExitClick() {
                        manualMeasuringHoleDepthBottomDialog = null
                        lastMotionDistance = ""
                        safeDistance = ""
                        dismiss()
                    }
                })
            }
        manualMeasuringHoleDepthBottomDialog?.show(childFragmentManager, "dialog")
        mStates.isClearMotionDataVisible.set(true)
        getMotorMotionData(800)
    }

    /**
     * 实时刷新脉冲和运动距离
     */
    private fun updateMotionData(motorMotionDistanceInfo: AdmeMotorMotionDistanceInfo) {
        try {
            if (mStates.motionPulse.get() == motorMotionDistanceInfo.pulsenumber) {
                repeatPollNum++
                Timber.d(
                    "updateMotionData: curDistance=%s,curPulse=%s,repeatNum=%s",
                    mStates.motionDistance.get(),
                    mStates.motionPulse.get(),
                    repeatPollNum
                )
                //轮询 N 次电机脉冲数据不变化时，查询电机运动状态，判断电机是否停止运动
                if (repeatPollNum >= 6) {
                    queryMotorMotionConfig()
                    return
                }
            } else
                repeatPollNum = 0

            mStates.motionPulse.set(motorMotionDistanceInfo.pulsenumber)
            mStates.motionDistance.set(motorMotionDistanceInfo.realmovedistance)
            //继续轮询电机脉冲数据
            getMotorMotionData(800)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 自动模式处理电机运动状态变化
     *
     * 在轮询 N 次电机脉冲数据没有见变化后，根据查询的电机运动状态更新底部弹框按钮状态
     */
    private fun processAutoMotorMotionState(measuringHoleDepthInfo: AdmeMeasuringHoleDepthInfo) {
        //虽然轮询了 N 次电机脉冲数据没有见变化，但是电机状态为"1",表示还在运动，则清空计数，继续轮询电机脉冲数据
        if (measuringHoleDepthInfo.morunstate == "1") {
            repeatPollNum = 0
            //继续轮询电机脉冲数据
            getMotorMotionData(800)
            return
        }
        repeatPollNum = 0
        //电机停止,更新运动状态页面
        mStates.isExitButtonVisible.set(true)
    }

    /**
     * 手动模式处理电机运动状态变化
     *
     * 在轮询 N 次电机脉冲数据没有见变化后，根据查询的电机运动状态更新底部弹框按钮状态
     */
    private fun processManualMotorMotionState(measuringHoleDepthInfo: AdmeMeasuringHoleDepthInfo) {
        //虽然轮询了 N 次电机脉冲数据没有见变化，但是电机状态为"1",表示还在运动，则清空计数，继续轮询电机脉冲数据
        if (measuringHoleDepthInfo.morunstate == "1") {
            repeatPollNum = 0
            //继续轮询电机脉冲数据
            getMotorMotionData(800)
            return
        }
        repeatPollNum = 0
        try {
            val distanceTotalGoal = abs(mStates.distanceGoal.get().toDouble())
            val distanceDiff =
                abs(mStates.motionDistance.get().toDouble()) - abs(lastMotionDistance.toDouble())
            //设定的运动距离目标值小于等于运动距离变化量时,电机停止
            if (distanceTotalGoal - distanceDiff <= 0) {
                //电机停止,更新运动状态页面
                mStates.isExitButtonVisible.set(true)
            } else {
                //电机暂停
                mStates.pauseButtonText.set("继续")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}