package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mg301

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
 * 创建时间：2026/1/16
 * 描述：多模融合通讯数据网关(MG301)配置主页 - 支持4G和蓝牙两种通讯方式
 *
 */
class MG301HomeFragment : BaseDeviceHomeFragment() {

    /**
     * 初始化设备数据
     * 设置 MG301 设备的 Logo 图标资源（正常/告警/离线/故障状态）
     */
    override fun initData() {
        super.initData()
        // MG301 使用多模网关图标资源
        mHeadStates.productErrorResId.set(R.drawable.device_logo_multimode_gateway_error)
        mHeadStates.productAlarmResId.set(R.drawable.device_logo_multimode_gateway_alarm)
        mHeadStates.productOfflineResId.set(R.drawable.device_logo_multimode_gateway_offline)
        mHeadStates.productNormalResId.set(R.drawable.device_logo_multimode_gateway)
        // 设置当前显示的 Logo 为正常状态
        mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
    }

    /**
     * 初始化功能模块数据
     * 构建页面的功能模块列表，包括设备信息、快捷配置和高级配置三个分组
     */
    override fun initModuleData() {
        // 创建模块列表容器
        val groupList = mutableListOf<Any>()

        // ==================== 设备信息分组 ====================
        // 添加设备信息分组标题
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    // 基本信息模块 - 显示设备型号、序列号等基本参数
                    CommonModule(
                        name = "基本信息",
                        resID = R.drawable.ic_module_basic_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_mg301BaseInfoFragment
                    ).toUnified(),

                    // 网络信息模块 - 显示4G/网络连接状态
                    CommonModule(
                        name = "网络信息",
                        resID = R.drawable.ic_module_net_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_mg301NetInfoFragment
                    ).toUnified(),

                    // 状态信息模块 - 显示设备运行状态、信号强度等
                    CommonModule(
                        name = "状态信息",
                        resID = R.drawable.ic_module_state_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_mg301StatusInfoFragment
                    ).toUnified(),

                    // 位置信息模块 - 显示GPS位置坐标（复用通用位置信息页面）
                    CommonModule(
                        name = "位置信息",
                        resID = R.drawable.ic_module_location_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_commonLocationInfoFragment
                    ).toUnified()
                )
            )
        )

        // ==================== 快捷配置分组 ====================
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        // 添加快捷配置分组标题
        groupList.add(DeviceStatusInfoGroupItem("快捷配置"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    // 一键配置模块 - 快速配置设备参数（复用通用一键配置页面）
                    CommonModule(
                        name = "一键配置",
                        resID = R.drawable.ic_module_cmd_debug_new,
                        navId = R.id.action_global_to_quickConfigCommandParam
                    ).toUnified(),
                )
            )
        )

        // ==================== 高级配置分组 ====================
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        // 添加高级配置分组标题
        groupList.add(DeviceStatusInfoGroupItem("高级配置"))

        // 构建高级配置模块树
        val configModuleTree = ConfigModuleTree(
            configModules = arrayListOf(
                // 端口配置模块 - 暂不支持，置灰处理
                // isSupport = false 表示该模块暂不支持，UI 显示为灰色，点击无响应
                CommonModule(
                    name = "端口配置",
                    resID = R.drawable.ic_module_serial_port,
                    navId = 0,  // navId = 0 表示无导航目标
                    isSupport = false  // 置灰处理
                ).toUnified(),

                // 网络配置模块 - 暂不支持，置灰处理
                CommonModule(
                    name = "网络配置",
                    resID = R.drawable.ic_module_network_setting,
                    navId = 0,  // navId = 0 表示无导航目标
                    isSupport = false  // 置灰处理
                ).toUnified(),

                // 上报配置模块 - 配置数据上报策略
                CommonModule(
                    name = "上报配置",
                    resID = R.drawable.ic_module_work_mode_new,
                    navId = R.id.action_global_to_mg301ReportConfigFragment
                ).toUnified(),

                // 链路配置模块 - 配置数据中心连接参数
                DataCenterModule(
                    name = "链路配置",
                    resID = R.drawable.ic_module_datacenter_new,
                    navId = R.id.action_global_to_mg301DataCenterHomeFragment
                ).toUnified(),

                // LORA配置模块 - 配置LoRa无线通信参数
                CommonModule(
                    name = "LORA配置",
                    resID = R.drawable.ic_module_lora_new,
                    navId = R.id.action_global_to_mg301LoraSettingFragment
                ).toUnified(),

                // 卫通配置模块 - 暂不支持，置灰处理
                CommonModule(
                    name = "卫通配置",
                    resID = R.drawable.ic_module_cors,
                    navId = 0,  // navId = 0 表示无导航目标
                    isSupport = false  // 置灰处理
                ).toUnified(),

                // 设备操作模块 - 设备控制操作（重启、召测等）
                CommonModule(
                    name = "设备操作",
                    resID = R.drawable.ic_module_equip_operation,
                    navId = R.id.action_global_to_mg301EquipmentOperationFragment
                ).toUnified(),

                // 时间校准模块 - 设备时间同步（复用通用时间校准页面）
                CommonModule(
                    name = "时间校准",
                    resID = R.drawable.ic_module_time_calibration_new,
                    navId = R.id.action_global_to_time_calibration
                ).toUnified(),

                // 系统配置模块 - 系统高级设置（复用通用高级设置页面）
                CommonModule(
                    name = "系统配置",
                    resID = R.drawable.ic_module_system_setting,
                    navId = R.id.action_global_to_advancedSettingFragment
                ).toUnified(),
            )
        )

        // 如果是蓝牙连接方式，添加指令调试模块（方便开发调试）
        if (communicateWay is BleConnect) {
            configModuleTree.configModules.add(
                CommandDebugConfigModule(
                    resID = R.drawable.ic_module_cmd_debug_new,
                ).toUnified()
            )
        }

        // 添加高级配置模块树到列表
        groupList.add(configModuleTree)

        // 设置 RecyclerView 的数据模型
        binding.rvModule.models = groupList
    }

    /**
     * 处理其他特殊模块的点击事件
     * 主要处理链路配置模块的特殊导航逻辑（需要传递额外参数）
     *
     * @param configModule 被点击的模块
     */
    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {
            // 链路配置模块需要特殊处理 - 传递数据中心数量等参数
            is DataCenterModule -> {
                nav().safeNavigate(
                    configModule.navId,
                    BaseDataCenterHomeFragment.newBundleArguments(
                        centerNum = 5,  // MG301 支持5个数据中心
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                )
            }

            // 其他模块使用默认处理逻辑
            else -> {
                super.processOtherItemClick(configModule)
            }
        }
    }
}
