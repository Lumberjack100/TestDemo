package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeStepperMotorEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeAndNegativeTestExceptionHandlingInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeAnthropomorphicMovementInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeBrakePadControlInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeLowEnergyModeInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMotorPowerInfo
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeStepperMotorInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.databinding.FragmentAdmeIntelligentControlBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeIntelligentControlViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2023/12/14
 * @desc: ADME 智能控制参数配置页面
 *
 */
class AdmeIntelligentControlFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeIntelligentControlBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeIntelligentControlViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val brakePadControlList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_brakepad_control) }


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_intelligent_control,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeIntelligentControlBinding
        binding.llToolbar.toolbar.title = "智能控制"
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

    override fun initData() {
        super.initData()
        mStates.brakePadControl.set(brakePadControlList[0])
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

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            when (button.id) {
                R.id.positiveAndNegativeSB -> { //正反测使能
                    enableOrDisableStepperMotorParam(isChecked)
                }
                R.id.positiveAndNegativeTestExceptionHandlingSB -> { //正反测异常智能处理
                    enableOrDisableAndNegativeTestExceptionHandling(isChecked)
                }

                R.id.lowPowerEnableSBtn -> { //低功耗使能
                    enableOrDisableLowEnergy(isChecked)
                }

                R.id.anthropomorphicEnableSBtn -> { //拟人运动使能
                    enableOrDisableAnthropomorphicMovement(isChecked)
                }

                R.id.motorPowerEnableSBtn -> { //电机电源使能
                    enableOrDisableMotorPower(isChecked)
                }
            }
        }

        fun onTorqueMotorRebootClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定重启力矩电机吗？", "温馨提示", "确定", {
                commandItems.clear()
                val command = IOTCommandUtil.getCommand(IOTCommandType.TORQUE_MOTOR_REBOOT)
                commandItems.add(command)
                showLoadingDialog(StringUtils.getString(R.string.processing))
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }, "取消")
        }

        /**
         * 刹车片控制方式
         */
        fun onBrakePadControlChooseClick() {
            val selectedIndex = brakePadControlList.indexOf(mStates.brakePadControl.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", brakePadControlList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.brakePadControl.set(text)
                        setBrakePadControl(if (position == 0) "0" else "1")
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onClearDeviceDropNumberClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定清空设备下降次数吗？", "温馨提示", "确定", {
                commandItems.clear()
                val command =
                    IOTCommandUtil.getCommand(
                        IOTCommandType.ADME_MD_CLEAR_DEVICE_RUNNING_DATA,
                        "type=1"
                    )
                commandItems.add(command)

                showLoadingDialog(StringUtils.getString(R.string.processing))
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }, "取消")
        }

        fun onClearDeviceMileageClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定清空钢丝绳运行里程吗？", "温馨提示", "确定", {
                commandItems.clear()
                val command =
                    IOTCommandUtil.getCommand(
                        IOTCommandType.ADME_MD_CLEAR_DEVICE_RUNNING_DATA,
                        "type=2"
                    )
                commandItems.add(command)

                showLoadingDialog(StringUtils.getString(R.string.processing))
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }, "取消")
        }

        fun onClearVerticalMagneticSwitchTriggerClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定清空竖向磁开关触发次数吗？", "温馨提示", "确定", {
                commandItems.clear()
                val command =
                    IOTCommandUtil.getCommand(
                        IOTCommandType.ADME_MD_CLEAR_DEVICE_RUNNING_DATA,
                        "type=3"
                    )
                commandItems.add(command)

                showLoadingDialog(StringUtils.getString(R.string.processing))
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }, "取消")
        }

        fun onClearRotationMagneticSwitchTriggerClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定清空旋转磁开关触发次数吗？", "温馨提示", "确定", {
                commandItems.clear()
                val command =
                    IOTCommandUtil.getCommand(
                        IOTCommandType.ADME_MD_CLEAR_DEVICE_RUNNING_DATA,
                        "type=4"
                    )
                commandItems.add(command)

                showLoadingDialog(StringUtils.getString(R.string.processing))
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }, "取消")
        }

        fun onClearPadOpenCloseClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定清空刹车片启闭次数吗？", "温馨提示", "确定", {
                commandItems.clear()
                val command =
                    IOTCommandUtil.getCommand(
                        IOTCommandType.ADME_MD_CLEAR_DEVICE_RUNNING_DATA,
                        "type=5"
                    )
                commandItems.add(command)

                showLoadingDialog(StringUtils.getString(R.string.processing))
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }, "取消")
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

    /**
     * 正反测异常智能处理 使能
     */
    private fun enableOrDisableAndNegativeTestExceptionHandling(isChecked: Boolean) {
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_AND_NEGATIVE_TEST_EXCEPTION_HANDLING,
            "model=${if (isChecked) "1" else "0"}"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 力矩电机继电器低功耗使能
     */
    private fun enableOrDisableLowEnergy(isChecked: Boolean) {
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_LOW_ENERGY_MODE,
            "model=${if (isChecked) "1" else "0"}"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 拟人运动使能
     */
    private fun enableOrDisableAnthropomorphicMovement(isChecked: Boolean) {
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_ANTHROPOMORPHIC_MOVEMENT_MODE,
            "model=${if (isChecked) "1" else "0"}"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 电机电源使能
     */
    private fun enableOrDisableMotorPower(isChecked: Boolean) {
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_MOTOR_POWER,
            "model=${if (isChecked) "1" else "0"}"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 刹车片控制
     */
    private fun setBrakePadControl(mode: String) {
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_BRAKE_PAD_CONTROL,
            "model=$mode"
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

        //获取设备的步进电机正反测使能信息
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_STEPPER_MOTOR
        )
        commandItems.add(command)

        //获取正反测异常智能处理使能信息
        command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_AND_NEGATIVE_TEST_EXCEPTION_HANDLING
        )
        commandItems.add(command)

        //获取低功耗使能信息
        command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_LOW_ENERGY_MODE
        )
        commandItems.add(command)

        //获取拟人运动使能信息
        command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE
        )
        commandItems.add(command)

        //获取刹车片控制方式信息
        command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_BRAKE_PAD_CONTROL
        )
        commandItems.add(command)

        //获取电机电源使能信息
        command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_MOTOR_POWER
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
                        val errMsg = "查询正反测使能状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        //设备版本不支持，隐藏正反测使能
                        mStates.isPositiveAndNegativeTestSupport.set(!errMsg.contains("设备版本不支持"))
                        //return
                    }

                    is IOTCommandResult.Success -> {
                        val admeStepperMotorInfo = result.data
                        mStates.positiveAndNegativeTest.set(admeStepperMotorInfo.posnegtest == "1")
                    }
                }
                sendCommandFromCmdList {
                    binding.refreshLayout.finish()
                }
            }

            IOTCommandType.ADME_MD_GET_AND_NEGATIVE_TEST_EXCEPTION_HANDLING -> {
                val result = iotParseManager.parse<AdmeAndNegativeTestExceptionHandlingInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_AND_NEGATIVE_TEST_EXCEPTION_HANDLING
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询正反测异常智能处理使能状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        //设备版本不支持，隐藏正反测异常智能处理使能
                        mStates.isPositiveAndNegativeTestExceptionHandlingSupport.set(!errMsg.contains("设备版本不支持"))
                        //return
                    }

                    is IOTCommandResult.Success -> {
                        val info = result.data
                        mStates.isPositiveAndNegativeTestExceptionHandling.set(info.mode == "1")
                    }
                }
                sendCommandFromCmdList {
                    binding.refreshLayout.finish()
                }
            }

            IOTCommandType.ADME_MD_GET_LOW_ENERGY_MODE -> {
                val result = iotParseManager.parse<AdmeLowEnergyModeInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_LOW_ENERGY_MODE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询低功耗使能状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        //设备版本不支持，隐藏低功耗使能
                        mStates.isLowPowerAlarmSupport.set(!errMsg.contains("设备版本不支持"))
                        //return
                    }

                    is IOTCommandResult.Success -> {
                        val admeLowEnergyModeInfo = result.data
                        mStates.lowPowerAlarm.set(admeLowEnergyModeInfo.mode == "1")
                    }
                }
                sendCommandFromCmdList {
                    binding.refreshLayout.finish()
                }
            }

            IOTCommandType.ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE -> {
                val result = iotParseManager.parse<AdmeAnthropomorphicMovementInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询拟人运动使能状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        //设备版本不支持，隐藏拟人运动使能
                        mStates.isAnthropomorphicMovementSupport.set(!errMsg.contains("设备版本不支持"))
                        //return
                    }

                    is IOTCommandResult.Success -> {
                        val anthropomorphicMovementInfo = result.data
                        mStates.anthropomorphicMovement.set(anthropomorphicMovementInfo.mode == "1")
                    }
                }
                sendCommandFromCmdList {
                    binding.refreshLayout.finish()
                }
            }

            IOTCommandType.ADME_MD_GET_BRAKE_PAD_CONTROL -> {
                val result = iotParseManager.parse<AdmeBrakePadControlInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_BRAKE_PAD_CONTROL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询刹车片控制方式出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        //设备版本不支持，隐藏刹车片控制
                        mStates.isBrakePadControlSupport.set(!errMsg.contains("设备版本不支持"))
                        //return
                    }

                    is IOTCommandResult.Success -> {
                        val padControlInfo = result.data
                        mStates.brakePadControl.set(if (padControlInfo.mode == "1") brakePadControlList[1] else brakePadControlList[0])
                    }
                }
                sendCommandFromCmdList {
                    binding.refreshLayout.finish()
                }
            }

            IOTCommandType.ADME_MD_GET_MOTOR_POWER -> {
                val result = iotParseManager.parse<AdmeMotorPowerInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_MOTOR_POWER
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询电机电源使能状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        //设备版本不支持，隐藏电机电源使能
                        mStates.isMotorPowerSupport.set(!errMsg.contains("设备版本不支持"))
                        //return
                    }

                    is IOTCommandResult.Success -> {
                        val info = result.data
                        mStates.motorPower.set(info.mode == "1")
                    }
                }
                sendCommandFromCmdList {
                    binding.refreshLayout.finish()
                }
            }

            IOTCommandType.ADME_MD_SET_STEPPER_MOTOR -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置正反测使能出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_AND_NEGATIVE_TEST_EXCEPTION_HANDLING -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置正反测异常智能处理使能出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_LOW_ENERGY_MODE -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置低功耗使能出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_ANTHROPOMORPHIC_MOVEMENT_MODE -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置拟人运动使能出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.TORQUE_MOTOR_REBOOT -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "重启失败: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("力矩电机即将重启")
                        }
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_BRAKE_PAD_CONTROL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置刹车片控制方式出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_MOTOR_POWER -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置电机电源使能出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.ADME_MD_CLEAR_DEVICE_RUNNING_DATA -> {//
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

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}