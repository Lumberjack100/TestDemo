package com.shmedo.mcloudapp.ui.page.device.lb20s.fragment

import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.OneClickSilenceModule
import com.shmedo.mcloudapp.model.RebootModule
import com.shmedo.mcloudapp.model.RunningStatusModule
import com.shmedo.mcloudapp.model.TimeCalibrationModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDeviceHomeFragment

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： 无线预警广播(江苏赛立科技有限公司)
 */
class LB20SHomeFragment : UniversalDeviceHomeFragment() {
    override fun initData() {
        super.initData()
//        mHeadStates.productLightResId.set(R.drawable.device_logo_gateway)
//        mHeadStates.productGrayResId.set(R.drawable.device_logo_gateway_gray)
//        mHeadStates.productLogoResId.set(mHeadStates.productLightResId.get())
        mHeadStates.isIOTPlatformStateVisible.set(false)
    }

    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    resID = R.drawable.ic_module_current_state,
                    navId = R.id.action_global_to_commonRunningDeviceInfoFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                TimeCalibrationModule(
                    resID = R.drawable.ic_module_terminal_time
                )
            )
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "自组网设置",
                    desc = "传感器LoRa电台设置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_uProductHomeFragment_to_lB20SAdHocNetworkSettingsFragment
                )
            )
        )

        moduleList.add(
            ConfigModule(
                DataCenterModule(
                    resID = R.drawable.ic_module_datacenter,
                    navId = R.id.action_global_to_universalDataCenterHomeFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "报警测试",
                    desc = "报警功能测试",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_global_to_lB20SAlarmTestFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(OneClickSilenceModule())
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "音量调节",
                    desc = "设置语音音量大小",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_global_to_lB20SVolumeSettingsFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(RebootModule(resID = R.drawable.ic_module_reboot))
        )
        moduleList.add(
            ConfigModule(AdvancedSettingsModule(navId = R.id.action_global_to_advancedSettingFragment))
        )
        if (communicateWay is BleConnect) {
            moduleList.add(
                ConfigModule(
                    CommandDebugConfigModule()
                )
            )
        }

        binding.rvModule.models = moduleList
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {
            is DataCenterModule -> {
                val bundle = UniversalDataCenterHomeFragment.newBundleArguments(
                    3,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().safeNavigate(
                    configModule.navId,
                    bundle
                )
            }

            is OneClickSilenceModule -> {
                showMessage("是否立即关闭语音播报？", "温馨提示", "确定", {
                    commandItems.clear()
                    val command =
                        IOTCommandUtil.getCommand(IOTCommandType.SET_VOICE_BROADCAST_VOLUME_OFF)
                    commandItems.add(command)

                    showLoadingDialog(StringUtils.getString(R.string.processing))
                    sendCommandFromCmdList(isStartTimeoutJob = true)
                }, "取消")
            }

            is CommandDebugConfigModule -> {
                val bundle = BleCustomCommandLogPrintFragment.newBundleArguments(
                    true,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().safeNavigate(configModule.navId, bundle)
            }

            else -> {
                if (configModule.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().safeNavigate(
                        configModule.navId,
                        bundle
                    )
                }
            }
        }
    }

    override fun processOtherCmdResult(commandType: IOTCommandType, cmdStr: String) {
        when (commandType) {
            IOTCommandType.SET_VOICE_BROADCAST_VOLUME_OFF -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "关闭语音播报失败:" + result.message
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("已关闭语音播报")
                        }
                    }
                }
            }

            else -> {

            }
        }
    }
}