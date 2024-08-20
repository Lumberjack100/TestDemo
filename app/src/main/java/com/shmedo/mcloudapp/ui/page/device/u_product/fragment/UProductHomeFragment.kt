package com.shmedo.mcloudapp.ui.page.device.u_product.fragment

import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.model.AlarmConfigModule
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.LoraConfigModule
import com.shmedo.mcloudapp.model.RebootModule
import com.shmedo.mcloudapp.model.RunningStatusModule
import com.shmedo.mcloudapp.model.SensorConfigModule
import com.shmedo.mcloudapp.model.TimeCalibrationModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDeviceHomeFragment
import kotlinx.coroutines.flow.debounce
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： INTEGRATION(一体化传感器产品线) 设备主页面
 */
class UProductHomeFragment : UniversalDeviceHomeFragment() {

    override fun initData() {
        super.initData()
        when (productType) {
            ProductType.U_D_1,//
            ProductType.U_D_2 -> {//一体化雷达液位计
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
        mHeadStates.productLogoResId.set(mHeadStates.productLightResId.get())
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
            is SensorConfigModule -> {
                var navId = configModule.navId
                when (productType) {
                    ProductType.U_D_1,//
                    ProductType.U_D_2 -> {//一体化雷达液位计
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
                    ProductType.U_D_1,//
                    ProductType.U_D_2 -> {//一体化雷达液位计
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
                        sendBleCommand(command)
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