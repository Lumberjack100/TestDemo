package com.shmedo.mcloudapp.device.ui.u_product.fragment

import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.models
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
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
import kotlinx.coroutines.flow.debounce
import timber.log.Timber

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
                mHeadStates.isIOTPlatformStateVisible.set(false)
            }

            ProductType.U_I_1 -> {//倾斜仪
                mHeadStates.productLightResId.set(R.drawable.device_logo_qingxieyi)
                mHeadStates.productGrayResId.set(R.drawable.device_logo_qingxieyi_gray)
                mHeadStates.isIOTPlatformStateVisible.set(false)
            }

            ProductType.U_R_1 -> {//一体化雨量计
                mHeadStates.productLightResId.set(R.drawable.device_logo_rain_gauge)
                mHeadStates.productGrayResId.set(R.drawable.device_logo_rain_gauge_gray)
                mHeadStates.isIOTPlatformStateVisible.set(false)
            }

            ProductType.LR200 -> {//米度一体式裂缝计
                mHeadStates.deviceName.set("BHY-3-LR200")
            }

            else -> {

            }
        }
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
            is SensorConfigModule -> {
                var navId = configModule.navId
                when (productType) {
                    ProductType.U_D_1,//水位
                    ProductType.U_D_2 -> {//泥位
                        navId = R.id.action_uProductHomeFragment_to_uDProductSensorParamFragment
                    }

                    ProductType.U_I_1 -> {//倾斜仪
                        navId = R.id.action_uProductHomeFragment_to_uIProductSensorParamFragment
                    }

                    ProductType.U_R_1 -> {//一体化雨量计
                        navId = R.id.action_uProductHomeFragment_to_uRProductSensorParamFragment
                    }

                    ProductType.LR200 -> {//米度一体式裂缝计
                        navId = R.id.action_uProductHomeFragment_to_lR200SensorParamFragment
                    }

                    else -> {

                    }
                }
                val bundle = BaseIOTDeviceFragment.newBundleArguments(
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(navId, bundle)
            }

            is DataCenterModule -> {
                var navId = configModule.navId
                var centerNum = 3
                when (productType) {
                    ProductType.U_D_1,//水位
                    ProductType.U_D_2 -> {//泥位
                        navId = R.id.action_uProductHomeFragment_to_uDProductDataCenterHomeFragment
                        centerNum = 3
                    }

                    ProductType.U_I_1,//倾斜仪
                    ProductType.U_R_1 //一体化雨量计
                    -> {
                        centerNum = 3
                    }

                    ProductType.LR200 -> {//米度一体式裂缝计
                        centerNum = 4
                    }

                    else -> {

                    }
                }
                nav().navigate(
                    navId,
                    UniversalDataCenterHomeFragment.newBundleArguments(
                        centerNum,
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
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

    override fun createObserver() {
        super.createObserver()
        setupHeartbeat()
    }

    // 设置心跳检查
    private fun setupHeartbeat() {
        launchWithViewLifecycle {
            lastCommunicationTime
                .debounce(AppContants.Communication.DELAY_10000_MILLIS)  // 30秒无更新触发
                .collect { lastUpdateTime ->
                    val updateTime = TimeUtils.millis2String(lastUpdateTime, "yyyy-MM-dd HH:mm:ss")
                    Timber.d("startTime: ${TimeUtils.getNowString()}，lastUpdateTime：$updateTime")
                    // 仅当设备连接并且需要发送心跳时，才发送心跳包
                    if (mHeadStates.isConnected.get() && isNearbyCommunicationTimeout(lastUpdateTime)) {
                        Timber.d("bingo startTime: ${TimeUtils.getNowString()}，lastUpdateTime：$updateTime")
                        val command = IOTCommandUtil.getCommand(IOTCommandType.HEART_BEAT)
                        Timber.d("发送心跳包指令: $command")
                        sendHeartbeatIOTCommand(command)
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