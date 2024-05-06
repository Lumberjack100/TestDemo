package com.shmedo.mcloudapp.device.ui.lr200.fragment

import com.drake.brv.utils.models
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DataCenterModule
import com.shmedo.mcloudapp.device.model.DeviceFunctionModule
import com.shmedo.mcloudapp.device.model.FirmwareUpgradeModule
import com.shmedo.mcloudapp.device.model.RebootModule
import com.shmedo.mcloudapp.device.model.RestoreFactoryModule
import com.shmedo.mcloudapp.device.model.RunningStatusModule
import com.shmedo.mcloudapp.device.model.TelemetryDataModule
import com.shmedo.mcloudapp.device.model.TimeCalibrationModule
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.device.ui.common.UniversalDataCenterHomeFragment
import com.shmedo.mcloudapp.device.ui.common.UniversalDeviceHomeFragment
import com.shmedo.mcloudapp.ext.nav

class LR200HomeFragment : UniversalDeviceHomeFragment() {

    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    resID = R.drawable.ic_module_current_state,
                    navId = R.id.action_lR200HomeFragment_to_lR200DeviceInfoFragment
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
            ConfigModule(TelemetryDataModule())
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
            ConfigModule(RestoreFactoryModule(resID = R.drawable.ic_module_reset))
        )
        moduleList.add(
            ConfigModule(
                FirmwareUpgradeModule(
                    resID = R.drawable.ic_module_firmware_upgrade,
                    navId = R.id.action_global_to_firmwareUpgradeFragment,
                    isSupport = false
                )
            )
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

    }
}