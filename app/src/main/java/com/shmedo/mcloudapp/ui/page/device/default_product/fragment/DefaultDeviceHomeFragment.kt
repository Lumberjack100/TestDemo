package com.shmedo.mcloudapp.ui.page.device.default_product.fragment

import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.TimeCalibrationModule
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceHomeFragment

/**
 * 创建者：gonghe
 * 创建时间：2024/3/8
 * 描述： TODO
 */
class DefaultDeviceHomeFragment : BaseDeviceHomeFragment() {

    override fun initData() {
        super.initData()
        when (productType) {
            ProductType.GNSS_E_1, ProductType.GNSS_E_2 -> {
                mHeadStates.productErrorResId.set(R.drawable.device_logo_e40_error)
                mHeadStates.productAlarmResId.set(R.drawable.device_logo_e40_alarm)
                mHeadStates.productOfflineResId.set(R.drawable.device_logo_e40_offline)
                mHeadStates.productNormalResId.set(R.drawable.device_logo_e40)
            }

            ProductType.GNSS_E_3 -> {
                mHeadStates.productErrorResId.set(R.drawable.device_logo_e50_pro_error)
                mHeadStates.productAlarmResId.set(R.drawable.device_logo_e50_pro_alarm)
                mHeadStates.productOfflineResId.set(R.drawable.device_logo_e50_pro_offline)
                mHeadStates.productNormalResId.set(R.drawable.device_logo_e50_pro)
            }

            ProductType.GNSS_T_1 -> {
                mHeadStates.productErrorResId.set(R.drawable.device_logo_gt600_error)
                mHeadStates.productAlarmResId.set(R.drawable.device_logo_gt600_alarm)
                mHeadStates.productOfflineResId.set(R.drawable.device_logo_gt600_offline)
                mHeadStates.productNormalResId.set(R.drawable.device_logo_gt600)
            }

            else -> {
                mHeadStates.productErrorResId.set(R.drawable.device_logo_default_error)
                mHeadStates.productAlarmResId.set(R.drawable.device_logo_default_alarm)
                mHeadStates.productOfflineResId.set(R.drawable.device_logo_default_offline)
                mHeadStates.productNormalResId.set(R.drawable.device_logo_default)
            }
        }
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
                        navId = R.id.action_global_to_defaultBaseInfoFragment
                    ).toUnified(),

                    CommonModule(
                        name = "网络信息",
                        resID = R.drawable.ic_module_net_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_defaultNetInfoFragment
                    ).toUnified(),

                    CommonModule(
                        name = "状态信息",
                        resID = R.drawable.ic_module_state_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_defaultStatusInfoFragment
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
                navId = R.id.action_global_to_universalDataCenterHomeFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            TimeCalibrationModule(
                name = "时间校准",
                resID = R.drawable.ic_module_time_calibration_new,
                navId = R.id.action_global_to_time_calibration
            ).toUnified()
        )
        configModuleTree.configModules.add(
            AdvancedSettingsModule(
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
                        centerNum = 4,
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                )
            }

            else -> {
                super.processOtherItemClick(configModule)
            }
        }
    }
}