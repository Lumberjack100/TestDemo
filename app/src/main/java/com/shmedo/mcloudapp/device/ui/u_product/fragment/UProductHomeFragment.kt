package com.shmedo.mcloudapp.device.ui.u_product.fragment

import com.drake.brv.utils.models
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.AlarmConfigModule
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DataCenterModule
import com.shmedo.mcloudapp.device.model.DeviceFunctionModule
import com.shmedo.mcloudapp.device.model.FirmwareUpgradeModule
import com.shmedo.mcloudapp.device.model.LoraConfigModule
import com.shmedo.mcloudapp.device.model.RebootModule
import com.shmedo.mcloudapp.device.model.RestoreFactoryModule
import com.shmedo.mcloudapp.device.model.RunningStatusModule
import com.shmedo.mcloudapp.device.model.SensorConfigModule
import com.shmedo.mcloudapp.device.model.TimeCalibrationModule
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.device.ui.common.UniversalDataCenterHomeFragment
import com.shmedo.mcloudapp.device.ui.common.UniversalDeviceHomeFragment
import com.shmedo.mcloudapp.ext.nav

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： INTEGRATION(一体化传感器产品线)
 */
class UProductHomeFragment : UniversalDeviceHomeFragment() {
    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionVisible.set(true)
        when (productType) {
            ProductType.U_D_1,//水位
            ProductType.U_D_2 -> {//泥位
                mHeadStates.productLightResId.set(R.drawable.device_logo_niweiji)
                mHeadStates.productGrayResId.set(R.drawable.device_logo_niweiji_gray)
                mHeadStates.isPlatformConnectionStateVisible.set(false)
            }

            ProductType.U_I_1 -> {//倾斜仪
                mHeadStates.productLightResId.set(R.drawable.device_logo_qingxieyi)
                mHeadStates.productGrayResId.set(R.drawable.device_logo_qingxieyi_gray)
                mHeadStates.isPlatformConnectionStateVisible.set(false)
            }

            ProductType.U_R_1 -> {//雨量计
                mHeadStates.productLightResId.set(R.drawable.device_logo_rain_gauge)
                mHeadStates.productGrayResId.set(R.drawable.device_logo_rain_gauge_gray)
                mHeadStates.isPlatformConnectionStateVisible.set(false)
            }

            else -> {
                mHeadStates.isPlatformConnectionStateVisible.set(false)
            }
        }
//        mHeadStates.deviceName.set(if (deviceInfo.deviceToken.endsWith(ProductType.GNSS_M_1.newSuffix)) "M20 (单北斗)" else "M20 (全星座)")
    }

    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    resID = R.drawable.ic_module_current_state,
                    navId = R.id.action_uProductHomeFragment_to_uProductDeviceInfoFragment
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
                SensorConfigModule(
                    navId = 0
                )
            )
        )
        moduleList.add(
            ConfigModule(
                LoraConfigModule(
                    resID = R.drawable.ic_module_lora,
                    navId = R.id.action_global_to_loraSettingFragment
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
                    navId = R.id.action_global_to_firmwareUpgradeFragment
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
            is RunningStatusModule -> {
                val bundle = UniversalDeviceHomeFragment.newBundleArguments(
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

            is SensorConfigModule -> {
                val bundle = BaseIOTDeviceFragment.newBundleArguments(
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                when (productType) {
                    ProductType.U_D_1,//水位
                    ProductType.U_D_2 -> {//泥位

                    }

                    ProductType.U_I_1 -> {//倾斜仪
                        nav().navigate(
                            R.id.action_uProductHomeFragment_to_uIProductSensorParamFragment,
                            bundle
                        )
                    }

                    ProductType.U_R_1 -> {//雨量计
                        nav().navigate(
                            R.id.action_uProductHomeFragment_to_uRProductSensorParamFragment,
                            bundle
                        )
                    }

                    else -> {

                    }
                }
            }

            is LoraConfigModule -> {//LORA设置
                val bundle = UniversalDeviceHomeFragment.newBundleArguments(
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(
                    R.id.action_global_to_loraSettingFragment,
                    bundle
                )
            }

            is AlarmConfigModule -> {
                val bundle = UniversalDeviceHomeFragment.newBundleArguments(
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