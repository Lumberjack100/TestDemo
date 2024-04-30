package com.shmedo.mcloudapp.device.ui.common

import com.drake.brv.utils.models
import com.shmedo.mcloudapp.device.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DeviceFunctionModule
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ext.nav

/**
 * 创建者：gonghe
 * 创建时间：2024/3/8
 * 描述： TODO
 */
class TestDeviceHomeFragment : UniversalDeviceHomeFragment() {

    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                CommandDebugConfigModule()
            )
        )

        binding.rvModule.models = moduleList
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {
            is CommandDebugConfigModule -> {
                val bundle = BleCustomCommandLogPrintFragment.newBundleArguments(
                    communicateWay,
                    deviceInfo,
                    bleDevice,
                    true
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
}