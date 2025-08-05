package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceHomeFragment

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： 遥测终端机(MR702)配置主页 - 支持4G和蓝牙两种通讯方式
 */
class MR702HomeFragment : BaseDeviceHomeFragment() {

    override fun initData() {
        super.initData()
        mHeadStates.productErrorResId.set(R.drawable.device_logo_mr702_error)
        mHeadStates.productAlarmResId.set(R.drawable.device_logo_mr702_alarm)
        mHeadStates.productOfflineResId.set(R.drawable.device_logo_mr702_offline)
        mHeadStates.productNormalResId.set(R.drawable.device_logo_mr702)
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
                        navId = R.id.action_global_to_mR702BaseInfoFragment
                    ).toUnified(),
                    CommonModule(
                        name = "网络信息",
                        resID = R.drawable.ic_module_net_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_mR702NetInfoFragment
                    ).toUnified(),
                    CommonModule(
                        name = "状态信息",
                        resID = R.drawable.ic_module_state_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_mR702StatusInfoFragment
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
        val configModuleTree = ConfigModuleTree(
            configModules = arrayListOf(
                CommonModule(
                    name = "网络配置",
                    resID = R.drawable.ic_module_network_setting,
                    navId = R.id.action_global_to_mR702NetworkConfigFragment
                ).toUnified(),
                DataCenterModule(
                    name = "链路配置",
                    resID = R.drawable.ic_module_datacenter_new,
                    navId = R.id.action_global_to_mR702DataCenterHomeFragment
                ).toUnified(),
                CommonModule(
                    name = "上报配置",
                    resID = R.drawable.ic_module_work_mode_new,
                    navId = R.id.action_global_to_mR702ReportConfigFragment
                ).toUnified(),
                CommonModule(
                    name = "端口配置",
                    resID = R.drawable.ic_module_serial_port,
                    navId = R.id.action_global_to_mR702PortHomeFragment
                ).toUnified(),
                CommonModule(
                    name = "设备操作",
                    resID = R.drawable.ic_module_equip_operation,
                    navId = R.id.action_global_to_mR702EquipmentOperationFragment
                ).toUnified(),
                CommonModule(
                    name = "时间校准",
                    resID = R.drawable.ic_module_time_calibration_new,
                    navId = R.id.action_global_to_time_calibration
                ).toUnified(),
                CommonModule(
                    name = "系统配置",
                    resID = R.drawable.ic_module_system_setting,
                    navId = R.id.action_global_to_advancedSettingFragment
                ).toUnified(),
            )
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
                        centerNum = 5,
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