package com.shmedo.mcloudapp.device.ui.das.fragment

import com.drake.brv.utils.models
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.device.model.CollectorConfigModule
import com.shmedo.mcloudapp.device.model.CommonModule
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DataCenterModule
import com.shmedo.mcloudapp.device.model.DeviceFunctionModule
import com.shmedo.mcloudapp.device.model.RebootModule
import com.shmedo.mcloudapp.device.model.RunningStatusModule
import com.shmedo.mcloudapp.device.model.SensorConfigModule
import com.shmedo.mcloudapp.device.model.TelemetryDataModule
import com.shmedo.mcloudapp.device.model.TimeCalibrationModule
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.UniversalDeviceHomeFragment
import com.shmedo.mcloudapp.ext.nav

class DasHomeFragment : UniversalDeviceHomeFragment() {
    override fun initData() {
        super.initData()
        mHeadStates.productLightResId.set(R.drawable.device_logo_qingxieyi)
        mHeadStates.productGrayResId.set(R.drawable.device_logo_qingxieyi_gray)
        mHeadStates.productLogoResId.set(mHeadStates.productLightResId.get())
    }

    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    "关于设备",
                    "设备基本信息、运行数据",
                    R.drawable.ic_device_running_info,
                    navId = R.id.action_global_to_commonRunningDeviceInfoFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(TimeCalibrationModule())
        )
        moduleList.add(
            ConfigModule(TelemetryDataModule())
        )
        moduleList.add(
            ConfigModule(RebootModule())
        )
        moduleList.add(
            ConfigModule(CollectorConfigModule(navId = R.id.action_dasHomeFragment_to_dasCollectorSettingFragment))
        )
        moduleList.add(
            ConfigModule(DataCenterModule(navId = R.id.action_dasHomeFragment_to_dasDataCenterHomeFragment))
        )
        moduleList.add(
            ConfigModule(SensorConfigModule(navId = R.id.action_dasHomeFragment_to_dasSensorHomeFragment))
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "上报方式",
                    desc = "上报规则设置",
                    resID = R.drawable.ic_device_data_center,
                    navId = R.id.action_dasHomeFragment_to_dasTerminalParameterFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(AdvancedSettingsModule(navId = R.id.action_global_to_dasAdvancedSettingFragment))
        )
        binding.rvModule.models = moduleList
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {

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