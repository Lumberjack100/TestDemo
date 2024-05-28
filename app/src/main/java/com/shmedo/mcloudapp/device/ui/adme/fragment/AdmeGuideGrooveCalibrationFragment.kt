package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeGuideGrooveCalibrationEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeGuideGrooveCalibrationInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMotorMotionAngleInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentAdmeGuideGrooveCalibrationBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeGuideGrooveCalibrationViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.ext.showMessageDialog
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.math.abs

/**
 * @author：gonghe
 * @time: 2024/1/4
 * @desc:  ADME 导槽校准
 *
 */
class AdmeGuideGrooveCalibrationFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeGuideGrooveCalibrationBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeGuideGrooveCalibrationViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val motionTypeList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_guide_groove_calibration_motor_motion_type) }

    private var lastPulse: String = "" //上次停止时脉冲数
    private var continuePulseGoal: Int = 0 //继续运动脉冲目标数
    private var repeatPollNum = 0 //当查询电机脉冲数重复超过一定次数时，判定电机停止

    private var motorMotionAngleFragmentBottomDialog: AdmeMotorMotionAngleBottomDialog? = null


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_guide_groove_calibration,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeGuideGrooveCalibrationBinding
        binding.llToolbar.toolbar.title = "导槽校准"
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
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryMotorMotionConfig()
        }
    }

    override fun initData() {
        super.initData()
        mStates.motionType.set(motionTypeList[0])
        mStates.isClearMotionDataVisible.set(true)
    }

    inner class ClickProxy : BaseClickProxy() {

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
                    { _, text ->
                        mStates.motionType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onRunClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }

        fun onClearDataClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确认清除设备运动记录数据吗?", "温馨提示", "确定", {
                clearMotorMotionData()
            }, "取消")
        }
    }

    private fun initSaveCommand() {
        if (mStates.speed.get().isEmpty()) {
            showMessageDialog("请输入电机速度!")
            return
        }
        try {
            val value = mStates.speed.get().toDouble()
            if (value < 1 || value > 600) {
                showMessageDialog("电机速度数值范围[1,600]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("电机速度数值范围[1,600]!")
            return
        }

        if (mStates.pulseGoal.get().isEmpty()) {
            showMessageDialog("请输入运动脉冲!")
            return
        }
        try {
            val value = mStates.pulseGoal.get().toDouble()
            if (value < 0) {
                showMessageDialog("运动脉冲不能小于 0!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的运动脉冲!")
            return
        }
        val entity = AdmeGuideGrooveCalibrationEntity(
            movementway = if (mStates.motionType.get() == motionTypeList[0]) "0" else "1",
            motorspeed = mStates.speed.get(),
            movepulse = mStates.pulseGoal.get()
        )
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_GUIDE_GROOVE_CALIBRATION,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 获取导槽校准配置参数
     */
    private fun queryMotorMotionConfig() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 查询ADME测孔深运动的脉冲数、运动角度
     */
    private fun getMotorMotionData(timeMillis: Long = 0L) {
        if (mStates.isStopQueryMotorState.get()) return

        launchWithViewLifecycle {
            delay(timeMillis)

            commandItems.clear()
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE
            )
            commandItems.add(command)
            sendCommandFromCmdList(isStartTimeoutJob = true)
        }
    }

    /**
     * 停止或者暂停电机运动
     */
    private fun stopMotorMotion() {
        stopQueryMotorState()

        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_STOP_GUIDE_GROOVE_CALIBRATION
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 继续电机运动
     */
    private fun continueMotorMotion() {
        try {
            val pulseTotalGoal = abs(mStates.pulseGoal.get().toInt())
            val pulseDiff = abs(mStates.motionPulse.get().toInt()) - abs(lastPulse.toInt())
            //已达到设定运动目标
            if (pulseTotalGoal - pulseDiff <= 0) {
                mStates.isExitButtonVisible.set(true)
                Toaster.show("无法继续电机运动操作")
                return
            }
            continuePulseGoal = pulseTotalGoal - pulseDiff
            val entity = AdmeGuideGrooveCalibrationEntity(
                movementway = if (mStates.motionType.get() == motionTypeList[0]) "0" else "1",
                movepulse = continuePulseGoal.toString()
            )
            commandItems.clear()
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.ADME_MD_SET_GUIDE_GROOVE_CALIBRATION,
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
            IOTCommandType.ADME_MD_CLEAR_GUIDE_GROOVE_CALIBRATION_DATA
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION -> {//获取ADME的导槽校准配置参数
                val result = iotParseManager.parse<AdmeGuideGrooveCalibrationInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        handleFailureResult("获取导槽校准配置参数出错: ${result.message}")
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        motorMotionAngleFragmentBottomDialog?.let {
                            if (it.isVisible) {
                                processMotorMotionState(result.data)
                            }
                        } ?: initParamConfigInfo(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_GUIDE_GROOVE_CALIBRATION -> {//设置ADME的导槽校准配置参数
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置导槽校准配置参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        showMotorMotionBottomDialog()
                    }
                }
            }

            IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE -> {//查询ADME测孔深运动的脉冲数、运动角度
                val result = iotParseManager.parse<AdmeMotorMotionAngleInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "获取电机的实时运动数据出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        if (lastPulse.isEmpty())
                            lastPulse = result.data.pulsenumber
                        motorMotionAngleFragmentBottomDialog?.let {
                            if (it.isVisible) {
                                updateMotionData(result.data)
                            }
                        }
                    }
                }
            }

            IOTCommandType.ADME_MD_STOP_GUIDE_GROOVE_CALIBRATION -> {//停止电机运动
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "停止电机出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        stopQueryMotorState()
                        //电机停止,更新运动状态页面
                        mStates.isExitButtonVisible.set(mStates.isDoStopAction.get())
                    }
                }
            }

            IOTCommandType.ADME_MD_CLEAR_GUIDE_GROOVE_CALIBRATION_DATA -> {//ADME 深清空数据
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "清空数据出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        mStates.isClearMotionDataVisible.set(false)
                        getMotorMotionData()
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initParamConfigInfo(grooveCalibrationInfo: AdmeGuideGrooveCalibrationInfo) {
        try {
            grooveCalibrationInfo.movementway.toInt().let {
                if (it in motionTypeList.indices) {
                    mStates.motionType.set(motionTypeList[it])
                }
            }
            mStates.speed.set(grooveCalibrationInfo.motorspeed)
            mStates.pulseGoal.set(grooveCalibrationInfo.movepulse)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    private fun showMotorMotionBottomDialog() {
        //数据运行弹框已经显示了
        motorMotionAngleFragmentBottomDialog?.let {
            if (it.isVisible) {
                Timber.d(
                    "Continue Motion: lastPulse=%s,curPulse=%s,continuePulseGoal=%s",
                    lastPulse,
                    mStates.motionPulse.get(),
                    continuePulseGoal,
                )
                mStates.isStopQueryMotorState.set(false)
                getMotorMotionData(DELAY_2000_MILLIS)
                return
            }
        }

        Timber.d("start Motion: lastPulse=$lastPulse,totalPulseGoal=${mStates.pulseGoal.get()}")
        motorMotionAngleFragmentBottomDialog =
            AdmeMotorMotionAngleBottomDialog.newInstance().apply {
                setOnDialogFragmentClickListener(object :
                    AdmeMotorMotionAngleBottomDialog.OnDialogFragmentClickListener {
                    override fun onCloseClick() {
                        if (!bleViewModel.isConnected() || mStates.isExitButtonVisible.get()) {
                            resetPulseData()
                            dismiss()
                            return
                        }
                        showMessage("确认退出数据运行吗?", "温馨提示", "确定", {
                            //蓝牙未断开时先发送停止电机指令，再关闭运行页面
                            if (bleViewModel.isConnected()) {
                                stopMotorMotion()
                            }
                            resetPulseData()
                            dismiss()
                        }, "取消")
                    }

                    override fun onStopClick() {
                        if (isBleDisconnected()) {
                            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                            return
                        }
                        stopMotorMotion()
                    }

                    override fun onPauseClick() {
                        if (isBleDisconnected()) {
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
                        resetPulseData()
                        dismiss()
                    }
                })
            }
        motorMotionAngleFragmentBottomDialog?.show(childFragmentManager, "dialog")
        mStates.isClearMotionDataVisible.set(true)
        mStates.isStopQueryMotorState.set(false)
        getMotorMotionData(DELAY_2000_MILLIS)
    }

    /**
     * 实时刷新脉冲和运动距离
     */
    private fun updateMotionData(motorMotionAngleInfo: AdmeMotorMotionAngleInfo) {
        try {
            if (mStates.motionPulse.get() == motorMotionAngleInfo.pulsenumber) {
                repeatPollNum++
                Timber.d(
                    "updateMotionData: lastPulse=%s,curPulse=%s,repeatNum=%s",
                    lastPulse,
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

            mStates.motionPulse.set(motorMotionAngleInfo.pulsenumber)
            mStates.motionAngle.set(motorMotionAngleInfo.realmoveangle)
            //继续轮询电机脉冲数据
            getMotorMotionData(DELAY_2000_MILLIS)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 处理电机运动状态变化
     *
     * 在轮询 N 次电机脉冲数据没有见变化后，根据查询的电机运动状态更新底部弹框按钮状态
     */
    private fun processMotorMotionState(grooveCalibrationInfo: AdmeGuideGrooveCalibrationInfo) {
        //虽然轮询了 N 次电机脉冲数据没有见变化，但是电机状态为"1",表示还在运动，则清空计数，继续轮询电机脉冲数据
        if (grooveCalibrationInfo.morunstate == "1") {
            repeatPollNum = 0
            //继续轮询电机脉冲数据
            getMotorMotionData(DELAY_2000_MILLIS)
            return
        }
        //下面电机状态表示停止运动
        repeatPollNum = 0
        try {
            val pulseTotalGoal = abs(mStates.pulseGoal.get().toInt())
            val pulseDiff = abs(mStates.motionPulse.get().toInt()) - abs(lastPulse.toInt())
            //设定的运动目标值小于等于运动变化量时,电机停止
            if (pulseTotalGoal - pulseDiff <= 0) {
                //电机停止,更新运动状态页面
                mStates.isExitButtonVisible.set(true)
            } else {
                //电机暂停
                mStates.pauseButtonText.set("继续")
            }
            stopQueryMotorState()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun resetPulseData() {
        motorMotionAngleFragmentBottomDialog = null
        lastPulse = ""
        repeatPollNum = 0
    }

    private fun stopQueryMotorState() {
        mStates.isStopQueryMotorState.set(true)
        cancelNearbyCommunicationTimeoutJob()
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        const val DELAY_2000_MILLIS = 2000L
    }
}