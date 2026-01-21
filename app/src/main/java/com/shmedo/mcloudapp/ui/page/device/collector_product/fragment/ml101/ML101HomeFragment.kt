package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.ml101

import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceHomeFragment

/**
 * 创建者：gonghe
 * 创建时间：2026/1/21
 * 描述：一体化通信终端配置主页 - 支持 ML101(LoRa) 和 MS101(卫星) 两种产品类型
 *
 * 产品类型说明：
 * - ML101 (ProductType.C_L_1): 一体化 LoRa 通信终端，使用 LoRa 电台进行数据传输
 * - MS101 (ProductType.C_S_2): 一体化卫星通信终端，使用卫星链路进行数据传输
 *
 * 功能模块差异：
 * - 设备信息分组：
 *   - ML101: 基本信息、电台信息、状态信息、位置信息
 *   - MS101: 基本信息、卫通信息、状态信息、位置信息
 * - 高级配置分组：
 *   - ML101: 电台配置、RS485配置、RS232配置、时间校准、系统配置
 *   - MS101: 卫通配置、RS485配置、RS232配置、时间校准、系统配置
 */
class ML101HomeFragment : BaseDeviceHomeFragment() {

    // ==================== 辅助属性 ====================

    /**
     * 判断当前设备是否为 ML101 (LoRa 终端)
     * - true: ML101 设备，显示电台相关模块
     * - false: MS101 设备，显示卫通相关模块
     */
    private val isML101: Boolean
        get() = productType == ProductType.C_L_1

    // ==================== 生命周期方法 ====================

    /**
     * 初始化设备数据
     * 设置 ML101/MS101 设备的 Logo 图标资源（正常/告警/离线/故障状态）
     */
    override fun initData() {
        super.initData()
        // ML101/MS101 共用同一套图标资源
        mHeadStates.productErrorResId.set(R.drawable.device_logo_ml101_error)
        mHeadStates.productAlarmResId.set(R.drawable.device_logo_ml101_alarm)
        mHeadStates.productOfflineResId.set(R.drawable.device_logo_ml101_offline)
        mHeadStates.productNormalResId.set(R.drawable.device_logo_ml101)
        // 设置当前显示的 Logo 为正常状态
        mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
    }

    /**
     * 初始化功能模块数据
     * 根据产品类型（ML101/MS101）构建页面的功能模块列表
     * 包括设备信息、快捷配置和高级配置三个分组
     */
    override fun initModuleData() {
        // 创建模块列表容器
        val groupList = mutableListOf<Any>()

        // ==================== 设备信息分组 ====================
        // 添加设备信息分组标题
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))
        groupList.add(buildDeviceInfoModuleTree())

        // ==================== 快捷配置分组 ====================
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        // 添加快捷配置分组标题
        groupList.add(DeviceStatusInfoGroupItem("快捷配置"))
        groupList.add(buildQuickConfigModuleTree())

        // ==================== 高级配置分组 ====================
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        // 添加高级配置分组标题
        groupList.add(DeviceStatusInfoGroupItem("高级配置"))
        groupList.add(buildAdvancedConfigModuleTree())

        // 设置 RecyclerView 的数据模型
        binding.rvModule.models = groupList
    }

    // ==================== 模块构建方法 ====================

    /**
     * 构建设备信息模块树
     * 根据产品类型显示不同的信息模块：
     * - ML101: 基本信息、电台信息、状态信息、位置信息
     * - MS101: 基本信息、卫通信息、状态信息、位置信息
     *
     * @return 设备信息模块树配置
     */
    private fun buildDeviceInfoModuleTree(): ConfigModuleTree {
        return ConfigModuleTree(
            configModules = arrayListOf(
                // 基本信息模块 - 显示设备型号、序列号等基本参数
                CommonModule(
                    name = "基本信息",
                    resID = R.drawable.ic_module_basic_info,
                    iconSize = ConvertUtils.dp2px(34f),
                    navId = R.id.action_global_to_ml101BaseInfoFragment
                ).toUnified(),

                // 电台信息/卫通信息模块 - 根据产品类型显示不同内容
                // ML101 显示 LoRa 电台参数，MS101 显示卫星通信参数
                CommonModule(
                    name = if (isML101) "电台信息" else "卫通信息",
                    resID = R.drawable.ic_module_net_info,
                    iconSize = ConvertUtils.dp2px(34f),
                    navId = if (isML101) R.id.action_global_to_ml101RadioInfoFragment else R.id.action_global_to_ms101SatInfoFragment
                ).toUnified(),

                // 状态信息模块 - 显示设备运行状态、信号强度等
                CommonModule(
                    name = "状态信息",
                    resID = R.drawable.ic_module_state_info,
                    iconSize = ConvertUtils.dp2px(34f),
                    navId = R.id.action_global_to_ml101StatusInfoFragment
                ).toUnified(),

                // 位置信息模块 - 显示 GPS 位置坐标（复用通用位置信息页面）
                CommonModule(
                    name = "位置信息",
                    resID = R.drawable.ic_module_location_info,
                    iconSize = ConvertUtils.dp2px(34f),
                    navId = R.id.action_global_to_commonLocationInfoFragment
                ).toUnified()
            )
        )
    }

    /**
     * 构建快捷配置模块树
     * ML101 和 MS101 共用相同的快捷配置模块
     *
     * @return 快捷配置模块树配置
     */
    private fun buildQuickConfigModuleTree(): ConfigModuleTree {
        return ConfigModuleTree(
            configModules = arrayListOf(
                // 一键配置模块 - 快速配置设备参数（复用通用一键配置页面）
                CommonModule(
                    name = "一键配置",
                    resID = R.drawable.ic_module_cmd_debug_new,
                    navId = R.id.action_global_to_quickConfigCommandParam
                ).toUnified()
            )
        )
    }

    /**
     * 构建高级配置模块树
     * 根据产品类型显示不同的配置模块：
     * - ML101: 电台配置、RS485配置、RS232配置、时间校准、系统配置
     * - MS101: 卫通配置、RS485配置、RS232配置、时间校准、系统配置
     *
     * @return 高级配置模块树配置
     */
    private fun buildAdvancedConfigModuleTree(): ConfigModuleTree {
        val configModuleTree = ConfigModuleTree(
            configModules = arrayListOf(
                // 电台配置/卫通配置模块 - 根据产品类型显示不同内容
                // ML101 配置 LoRa 电台参数，MS101 配置卫星通信参数
                CommonModule(
                    name = if (isML101) "电台配置" else "卫通配置",
                    resID = if (isML101) R.drawable.ic_module_lora_new else R.drawable.ic_module_cors,
                    navId = if (isML101) R.id.action_global_to_ml101RadioConfigFragment else R.id.action_global_to_ms101SatConfigFragment
                ).toUnified(),

                // RS485 配置模块 - 配置 RS485 串口通信参数
                CommonModule(
                    name = "RS485配置",
                    resID = R.drawable.ic_module_serial_port,
                    navId = R.id.action_global_to_ml101RS485ConfigFragment
                ).toUnified(),

                // RS232 配置模块 - 配置 RS232 串口通信参数
                CommonModule(
                    name = "RS232配置",
                    resID = R.drawable.ic_module_serial_port,
                    navId = R.id.action_global_to_ml101RS232ConfigFragment
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
                ).toUnified()
            )
        )

        // 如果是蓝牙连接方式，添加指令调试模块（方便开发调试）
        if (communicateWay is BleConnect) {
            configModuleTree.configModules.add(
                CommandDebugConfigModule(
                    resID = R.drawable.ic_module_cmd_debug_new
                ).toUnified()
            )
        }

        return configModuleTree
    }

    // ==================== 点击事件处理 ====================

    /**
     * 处理其他特殊模块的点击事件
     * 可在此处理需要传递额外参数的模块导航
     *
     * @param configModule 被点击的模块
     */
    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        // 当前所有模块使用默认导航逻辑
        // 如需特殊处理（如传递额外参数），可在此添加 when 分支
        super.processOtherItemClick(configModule)
    }
}
