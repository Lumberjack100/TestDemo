package com.shmedo.mcloudapp.device.ui.m20s.fragment

import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.device.model.AlarmConfigModule
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.device.model.CommonModule
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DataCenterModule
import com.shmedo.mcloudapp.device.model.DeviceFunctionModule
import com.shmedo.mcloudapp.device.model.RebootModule
import com.shmedo.mcloudapp.device.model.RunningStatusModule
import com.shmedo.mcloudapp.device.model.SetupWizard
import com.shmedo.mcloudapp.device.model.TimeCalibrationModule
import com.shmedo.mcloudapp.device.model.WorkModeModule
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.device.ui.common.UniversalDataCenterHomeFragment
import com.shmedo.mcloudapp.device.ui.common.UniversalDeviceHomeFragment
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import timber.log.Timber

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：  M20S 配置主页
 */
class M20SHomeFragment : UniversalDeviceHomeFragment() {
    override fun initData() {
        super.initData()
        mHeadStates.productLightResId.set(R.drawable.device_logo_m20)
        mHeadStates.productGrayResId.set(R.drawable.device_logo_m20_gray)
        mHeadStates.productLogoResId.set(mHeadStates.productLightResId.get())
        mHeadStates.isIOTPlatformStateVisible.set(false)
        mHeadStates.deviceName.set(if (deviceInfo.deviceToken.endsWith(ProductType.GNSS_M_1.newSuffix)) "M20 (单北斗)" else "M20 (全星座)")
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
        moduleList.add(ConfigModule(SetupWizard(name = "初始化")))
        moduleList.add(
            ConfigModule(
                WorkModeModule(
                    resID = R.drawable.ic_module_work_model,
                    navId = R.id.action_m20SHomeFragment_to_m20SWorkModelFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "电台设置",
                    desc = "RTCM电台设置",
                    resID = R.drawable.ic_module_radio_setting,
                    navId = R.id.action_m20SHomeFragment_to_m20SRadioSettingFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                AlarmConfigModule(
                    navId = R.id.action_global_to_alarmSettingFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "卫星通信",
                    desc = "卫星通信终端设置",
                    resID = R.drawable.ic_module_satellite_communications,
                    navId = 0,
                    isSupport = false
                )
            )
        )

        moduleList.add(
            ConfigModule(
                DataCenterModule(
                    resID = R.drawable.ic_module_datacenter,
                    navId = R.id.action_global_universalDataCenterHomeFragment
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
            is SetupWizard -> {
                showMessage("确定进行初始化吗？", "温馨提示", "确定", {
                    commandItems.clear()
                    val command = IOTCommandUtil.getCommand(
                        IOTCommandType.MD_SET_SENSOR_INITIAL,
                        "method=1&type=gnss"
                    )
                    commandItems.add(command)

                    showLoadingDialog(StringUtils.getString(R.string.processing))
                    sendCommandFromCmdList(isStartTimeoutJob = true)
                }, "取消")
            }

            is DataCenterModule -> {
                val bundle = UniversalDataCenterHomeFragment.newBundleArguments(
                    4,
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

    override fun processOtherCmdResult(commandType: IOTCommandType, cmdStr: String) {
        when (commandType) {
            IOTCommandType.MD_SET_SENSOR_INITIAL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "初始化失败: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("初始化完成")
                        }
                    }
                }
            }

            else -> {

            }
        }
    }
}