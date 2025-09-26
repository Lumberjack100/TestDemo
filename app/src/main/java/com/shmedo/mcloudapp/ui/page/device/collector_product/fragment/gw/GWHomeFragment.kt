package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.gw

import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigBannerItem
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceHomeFragment

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： 自组网报警网关
 */
class GWHomeFragment : BaseDeviceHomeFragment() {
    override fun initData() {
        super.initData()
        mHeadStates.productErrorResId.set(R.drawable.device_logo_gateway_error)
        mHeadStates.productAlarmResId.set(R.drawable.device_logo_gateway_alarm)
        mHeadStates.productOfflineResId.set(R.drawable.device_logo_gateway_offline)
        mHeadStates.productNormalResId.set(R.drawable.device_logo_gateway)
        mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
    }

    override fun initModuleData() {
        val groupList = mutableListOf<Any>()

        // 添加配置提示Banner
        groupList.add(ConfigBannerItem())
        groupList.add(GapItem(height = ConvertUtils.dp2px(10f)))

        groupList.add(DeviceStatusInfoGroupItem("设备信息"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    CommonModule(
                        name = "基本信息",
                        resID = R.drawable.ic_module_basic_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_gWBaseInfoFragment
                    ).toUnified()
                )
            )
        )

        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree()
        configModuleTree.configModules.add(
            CommonModule(
                name = "电台配置",
                resID = R.drawable.ic_module_sensor_setting_new,
                navId = R.id.action_global_to_radioSettingsFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "LORA配置",
                resID = R.drawable.ic_module_lora_new,
                navId = R.id.action_global_to_loraSettingFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
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
}