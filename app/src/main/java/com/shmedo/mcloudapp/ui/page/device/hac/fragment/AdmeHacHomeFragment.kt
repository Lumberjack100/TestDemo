package com.shmedo.mcloudapp.ui.page.device.hac.fragment

import com.drake.brv.utils.models
import com.shmedo.lib.cmd.base.iot_cmd.enums.AdmeCTRMotionState
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMotionState
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceOperationModule
import com.shmedo.mcloudapp.model.RunningStatusModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDeviceHomeFragment
import kotlinx.coroutines.delay

/**
 * 创建者：gonghe
 * 创建时间：2024/5/8
 * 描述： TODO
 */
class AdmeHacHomeFragment : UniversalDeviceHomeFragment() {

    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionVisible.set(true)
        //mHeadStates.productLightResId.set(R.drawable.device_logo_gateway)
        //mHeadStates.productGrayResId.set(R.drawable.device_logo_gateway_gray)
        mHeadStates.isRunningStateVisible.set(true)
    }

    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    resID = R.drawable.ic_module_current_state,
                    navId = R.id.action_admeHacHomeFragment_to_admeCurrentStateFragment
                )
            )
        )
        if (communicateWay is BleConnect) {
            moduleList.add(
                ConfigModule(
                    CommonModule(
                        name = "孔深测量",
                        desc = "测量测斜管深度",
                        resID = R.drawable.ic_measuring_hole_depth,
                        navId = R.id.action_global_to_admeHacMeasuringHoleDepthFragment
                    )
                )
            )
        }
        moduleList.add(
            ConfigModule(
                DeviceOperationModule(
                    name = "高级配置",
                    desc = "计米轮、测斜议、执行机构等",
                    navId = R.id.action_global_to_admeAdvancedConfigurationFragment
                )
            )
        )
        if (communicateWay is BleConnect) {
            moduleList.add(
                ConfigModule(
                    CommonModule(
                        name = "开始测斜",
                        desc = "测量位移",
                        resID = R.drawable.ic_measuring_hole_depth,
                        navId = R.id.action_global_to_admeHacMeasuringDataFragment
                    )
                )
            )
        }
        if (communicateWay is BleConnect) {
            moduleList.add(
                ConfigModule(
                    CommandDebugConfigModule()
                )
            )
        }

        moduleList.add(
            ConfigModule(AdvancedSettingsModule(navId = R.id.action_global_to_admeAdvancedSettingFragment))
        )
        binding.rvModule.models = moduleList
    }

    override fun onNetPlatformReady() {
        //米度平台在线
        if (deviceInfo.onlineStatus) {
            getMotorMotionData()
        }
    }

    override fun onBleDeviceReady() {
        super.onBleDeviceReady()
        getMotorMotionData()
    }

    /**
     * 获取电机的运行状态
     */
    private fun getMotorMotionData(timeMillis: Long = 0L) {
        launchWithViewLifecycle {
            delay(timeMillis)

            commandItems.clear()
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE
            )
            commandItems.add(command)
            sendCommandFromCmdList(isStartTimeoutJob = false)
        }
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {
            is CommandDebugConfigModule -> {
                val bundle = BleCustomCommandLogPrintFragment.newBundleArguments(
                    true,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(configModule.navId, bundle)
            }

            else -> {
                if (configModule.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().navigate(
                        configModule.navId,
                        bundle
                    )
                }
            }
        }
    }


    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE -> {
                dismissLoadingDialog()
            }
            else -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog
                )
            }
        }
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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE -> {
                dismissLoadingDialog()
            }

            else -> {
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog
                )
            }
        }
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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = false,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }

            else -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }
        }
    }

    override fun processOtherCmdResult(commandType: IOTCommandType, cmdStr: String) {
        when (commandType) {
            IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE -> {//获取ADME的运行状态
                val result = iotParseManager.parse<HacMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "获取设备的运行状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        updateMotionState(result.data)
                    }
                }
            }

            else -> {

            }
        }
    }

    /**
     * 刷新电机运动状态
     */
    private fun updateMotionState(hacMotionState: HacMotionState) {
        AdmeCTRMotionState.valueByCode(hacMotionState.motorinfo).let { state ->
            mHeadStates.runningStateText.set(state.simpleDesc)
            if (state.code == "8" || state.code == "9")
                mMessenger.admeDeviceMode.set("0")
            else
                mMessenger.admeDeviceMode.set("1")
        }
    }
}