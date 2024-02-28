package com.shmedo.mcloudapp.device.ui.lr200.fragment

import com.drake.brv.utils.models
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DataCenterModule
import com.shmedo.mcloudapp.device.model.RebootModule
import com.shmedo.mcloudapp.device.model.RunningStatusModule
import com.shmedo.mcloudapp.device.model.TelemetryDataModule
import com.shmedo.mcloudapp.device.model.TimeCalibrationModule
import com.shmedo.mcloudapp.device.ui.common.UniversalDeviceHomeFragment

class LR200HomeFragment : UniversalDeviceHomeFragment() {

    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    "关于设备",
                    "设备基本信息、运行数据",
                    R.drawable.ic_device_running_info,
                    navId = R.id.action_lR200HomeFragment_to_lR200DeviceInfoFragment
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
            ConfigModule(DataCenterModule(navId = R.id.action_dasHomeFragment_to_dasDataCenterHomeFragment))
        )
        moduleList.add(
            ConfigModule(AdvancedSettingsModule(navId = R.id.action_global_to_dasAdvancedSettingFragment))
        )
        binding.recyclerview.models = moduleList
    }

    override fun processOtherCmdResult(commandType: IOTCommandType, cmdStr: String) {

    }
}