package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import com.blankj.utilcode.util.ConvertUtils
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
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.OneClickSilenceModule
import com.shmedo.mcloudapp.model.TimeCalibrationModule
import com.shmedo.mcloudapp.ui.page.device.common.NewUniversalBaseDeviceHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterHomeFragment

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： 物联网采集器(DAS)(江苏赛立科技有限公司)
 */
class DASHomeFragment : NewUniversalBaseDeviceHomeFragment() {
    override fun initData() {
        super.initData()
        mHeadStates.productErrorResId.set(R.drawable.device_logo_das_error)
        mHeadStates.productAlarmResId.set(R.drawable.device_logo_das_alarm)
        mHeadStates.productOfflineResId.set(R.drawable.device_logo_das_offline)
        mHeadStates.productNormalResId.set(R.drawable.device_logo_das)
    }

    override fun initModuleData() {
        val groupList = mutableListOf<Any>()
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    ConfigModule(
                        CommonModule(
                            name = "基本信息",
                            resID = R.drawable.ic_module_basic_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_dasBaseInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "网络信息",
                            resID = R.drawable.ic_module_net_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_dasNetInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "状态信息",
                            resID = R.drawable.ic_module_state_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_dasStatusInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "位置信息",
                            resID = R.drawable.ic_module_location_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_commonLocationInfoFragment
                        )
                    )
                )
            )
        )

        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree()
        configModuleTree.configModules.add(
            ConfigModule(
                DataCenterModule(
                    name = "采集配置",
                    resID = R.drawable.ic_module_work_mode_new,
                    navId = R.id.action_global_to_dasCollectorSettingFragment
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                DataCenterModule(
                    name = "链路配置",
                    resID = R.drawable.ic_module_datacenter_new,
                    navId = R.id.action_global_to_dasDataCenterHomeFragment
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                DataCenterModule(
                    name = "上报配置",
                    resID = R.drawable.ic_module_work_mode_new,
                    navId = R.id.action_global_to_dasTerminalParameterFragment
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                DataCenterModule(
                    name = "传感配置",
                    resID = R.drawable.ic_module_sensor_setting_new,
                    navId = R.id.action_global_to_dasSensorHomeFragment
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                TimeCalibrationModule(
                    name = "时间校准",
                    resID = R.drawable.ic_module_time_calibration_new,
                    navId = R.id.action_global_to_time_calibration
                )
            )
        )
        configModuleTree.configModules.add(
            ConfigModule(
                AdvancedSettingsModule(
                    name = "系统配置",
                    resID = R.drawable.ic_module_system_setting,
                    navId = R.id.action_global_to_advancedSettingFragment
                )
            )
        )
        if (communicateWay is BleConnect) {
            configModuleTree.configModules.add(
                ConfigModule(
                    CommandDebugConfigModule(
                        resID = R.drawable.ic_module_cmd_debug_new,
                    )
                )
            )
        }

        groupList.add(configModuleTree)
        binding.rvModule.models = groupList
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {
            is DataCenterModule -> {
                nav().safeNavigate(
                    configModule.navId,
                    UniversalDataCenterHomeFragment.Companion.newBundleArguments(
                        centerNum = 3,
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
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

            else -> {
                super.processOtherItemClick(configModule)
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