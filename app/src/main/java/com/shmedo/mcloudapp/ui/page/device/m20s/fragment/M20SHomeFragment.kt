package com.shmedo.mcloudapp.ui.page.device.m20s.fragment

import com.drake.brv.utils.models
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.model.AlarmConfigModule
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.RebootModule
import com.shmedo.mcloudapp.model.RunningStatusModule
import com.shmedo.mcloudapp.model.TimeCalibrationModule
import com.shmedo.mcloudapp.model.WorkModeModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDeviceHomeFragment

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
        mHeadStates.productToken.set(if (deviceInfo.deviceToken.endsWith(ProductType.GNSS_M_1.newSuffix)) "M20 (单北斗)" else "M20 (全星座)")
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
                    resID = R.drawable.ic_module_time_calibration_new
                )
            )
        )

        moduleList.add(
            ConfigModule(
                WorkModeModule(
                    resID = R.drawable.ic_module_work_mode_new,
                    navId = R.id.action_m20SHomeFragment_to_m20SWorkModelFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "电台设置",
                    desc = "RTCM电台设置",
                    resID = R.drawable.ic_module_lora,
                    navId = R.id.action_m20SHomeFragment_to_m20SRadioSettingFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                AlarmConfigModule(
                    resID = R.drawable.ic_module_alarm,
                    navId = R.id.action_global_to_alarmSettingFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "卫星通信",
                    desc = "卫星通信终端设置",
                    resID = R.drawable.ic_module_cors,
                    navId = 0,
                    isSupport = false
                )
            )
        )

        moduleList.add(
            ConfigModule(
                DataCenterModule(
                    resID = R.drawable.ic_module_datacenter_new,
                    navId = R.id.action_global_to_universalDataCenterHomeFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(RebootModule(resID = R.drawable.ic_module_reboot))
        )
        moduleList.add(
            ConfigModule(
                AdvancedSettingsModule(
                    name = "系统配置",
                    resID = R.drawable.ic_module_system_setting,
                    navId = R.id.action_global_to_advancedSettingFragment
                )
            )
        )
        if (communicateWay is BleConnect) {
            moduleList.add(
                ConfigModule(
                    CommandDebugConfigModule(resID = R.drawable.ic_module_cmd_debug_new)
                )
            )
        }
        binding.rvModule.models = moduleList
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {
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

            else -> {

            }
        }
    }
}