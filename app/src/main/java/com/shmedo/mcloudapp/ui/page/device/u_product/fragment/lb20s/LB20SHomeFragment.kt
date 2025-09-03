package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.lb20s

import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.OneClickSilenceModule
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceHomeFragment

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： 无线预警广播(LB20S)(江苏赛立科技有限公司)
 */
class LB20SHomeFragment : BaseDeviceHomeFragment() {
    override fun initData() {
        super.initData()
        mHeadStates.productErrorResId.set(R.drawable.device_logo_lb20s_error)
        mHeadStates.productAlarmResId.set(R.drawable.device_logo_lb20s_alarm)
        mHeadStates.productOfflineResId.set(R.drawable.device_logo_lb20s_offline)
        mHeadStates.productNormalResId.set(R.drawable.device_logo_lb20s)
        mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
    }

    override fun initModuleData() {
        val groupList = mutableListOf<Any>()
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    CommonModule(
                        name = "基本信息",
                        resID = R.drawable.ic_module_basic_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_lB20SBaseInfoFragment
                    ).toUnified(),

                    CommonModule(
                        name = "网络信息",
                        resID = R.drawable.ic_module_net_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_lB20SNetInfoFragment
                    ).toUnified(),

                    CommonModule(
                        name = "状态信息",
                        resID = R.drawable.ic_module_state_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_lB20SStatusInfoFragment
                    ).toUnified(),

                    CommonModule(
                        name = "位置信息",
                        resID = R.drawable.ic_module_location_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_commonLocationInfoFragment
                    ).toUnified()
                )
            )
        )

        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree()
        configModuleTree.configModules.add(
            DataCenterModule(
                name = "链路配置",
                resID = R.drawable.ic_module_datacenter_new,
                navId = R.id.action_global_to_dataCenterHomeFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "自组网配置",
                resID = R.drawable.ic_module_work_mode_new,
                navId = R.id.action_global_to_lB20SAdHocNetworkSettingsFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "报警测试",
                resID = R.drawable.ic_module_alarm_new,
                navId = R.id.action_global_to_lB20SAlarmTestFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            OneClickSilenceModule(
                name = "一键消音",
                resID = R.drawable.ic_module_work_mode_new,
                navId = 0
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "音量调节",
                resID = R.drawable.ic_module_work_mode_new,
                navId = R.id.action_global_to_lB20SVolumeSettingsFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "时间校准",
                resID = R.drawable.ic_module_time_calibration_new,
                navId = R.id.action_global_to_time_calibration
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "系统配置",
                resID = R.drawable.ic_module_system_setting,
                navId = R.id.action_global_to_advancedSettingFragment
            ).toUnified()
        )
        if (communicateWay is BleConnect) {
            configModuleTree.configModules.add(
                CommandDebugConfigModule(
                    resID = R.drawable.ic_module_cmd_debug_new,
                ).toUnified()
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
                    BaseDataCenterHomeFragment.newBundleArguments(
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
                    val commands = mutableListOf<String>()
                    val command =
                        IOTCommandUtil.getCommand(IOTCommandType.SET_VOICE_BROADCAST_VOLUME_OFF)
                    commands.add(command)

                    sendCommandSequence(
                        commands = commands,
                        config = CommandSequenceConfig(
                            loadingMessage = StringUtils.getString(R.string.processing),
                            errorConfig = ErrorConfig.dialogConfig()
                        )
                    )
                }, "取消")
            }

            else -> {
                super.processOtherItemClick(configModule)
            }
        }
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.SET_VOICE_BROADCAST_VOLUME_OFF -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "关闭语音播报失败:" + result.message
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        Toaster.show("已关闭语音播报")
                    }
                }
            }

            else -> {
                // 其他指令交给父类处理
                super.handleCommandResponse(cmdStr)
            }
        }
    }
}