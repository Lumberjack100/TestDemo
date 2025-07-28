package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m20s

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.M20SMeasureDataItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceHomeFragment
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/12/19
 * @desc: 使用优化架构的M20S设备主页
 *
 * 优化特点：
 * 1. 继承自 OptimizedUniversalBaseDeviceHomeFragment，使用新的通信架构
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 保持原有的M20S特定业务逻辑不变
 * 5. 支持4G和蓝牙两种通讯方式
 */
class OptimizedM20SHomeFragment : OptimizedBaseDeviceHomeFragment() {

    private var measureDataItem: M20SMeasureDataItem = M20SMeasureDataItem()

    override fun initData() {
        super.initData()
        // 设置M20S特定的设备Logo资源
        mHeadStates.productErrorResId.set(R.drawable.device_logo_m20_error)
        mHeadStates.productAlarmResId.set(R.drawable.device_logo_m20_alarm)
        mHeadStates.productOfflineResId.set(R.drawable.device_logo_m20_offline)
        mHeadStates.productNormalResId.set(R.drawable.device_logo_m20)
        mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        // 扩展适配器支持 M20SMeasureDataItem
        binding.rvModule.bindingAdapter.addType<M20SMeasureDataItem>(R.layout.item_m20s_measure_data)
    }

    override fun initModuleData() {
        val groupList = mutableListOf<Any>()

        // 添加测量数据作为第一个项目
        groupList.add(measureDataItem)
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))

        // 设备信息模块
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    ConfigModule(
                        CommonModule(
                            name = "基本信息",
                            resID = R.drawable.ic_module_basic_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_m20SBaseInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "网络信息",
                            resID = R.drawable.ic_module_net_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_m20SNetInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "状态信息",
                            resID = R.drawable.ic_module_state_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_m20SStatusInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "位置信息",
                            resID = R.drawable.ic_module_location_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_commonLocationInfoFragment
                        )
                    )
                )
            )
        )

        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        
        // 设备配置模块
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree()
        
        // 根据产品类型添加不同的配置模块
        if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
            configModuleTree.configModules.add(
                ConfigModule(
                    CommonModule(
                        name = "工作模式",
                        resID = R.drawable.ic_module_work_mode_new,
                        navId = R.id.action_global_to_m20SWorkModelFragment
                    )
                )
            )
        }
        
        configModuleTree.configModules.add(
            ConfigModule(
                DataCenterModule(
                    name = "链路配置",
                    resID = R.drawable.ic_module_datacenter_new,
                    navId = R.id.action_global_to_universalDataCenterHomeFragment
                )
            )
        )
        
        if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
            configModuleTree.configModules.add(
                ConfigModule(
                    CommonModule(
                        name = "电台配置",
                        resID = R.drawable.ic_module_lora_new,
                        navId = R.id.action_global_to_m20SRadioSettingFragment
                    )
                )
            )
        }
        
        if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
            configModuleTree.configModules.add(
                ConfigModule(
                    CommonModule(
                        name = "报警配置",
                        resID = R.drawable.ic_module_alarm_new,
                        navId = R.id.action_global_to_alarmSettingFragment
                    )
                )
            )
        }
        
        if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
            configModuleTree.configModules.add(
                ConfigModule(
                    CommonModule(
                        name = "卫星通信",
                        resID = R.drawable.ic_module_cors,
                        navId = 0,
                        isSupport = false
                    )
                )
            )
        }
        
        configModuleTree.configModules.add(
            ConfigModule(
                CommonModule(
                    name = "时间校准",
                    resID = R.drawable.ic_module_time_calibration_new,
                    navId = R.id.action_global_to_time_calibration
                )
            )
        )
        
        configModuleTree.configModules.add(
            ConfigModule(
                CommonModule(
                    name = "系统配置",
                    resID = R.drawable.ic_module_system_setting,
                    navId = R.id.action_global_to_advancedSettingFragment
                )
            )
        )
        
        // 蓝牙连接时添加指令调试模块
        if (communicateWay is BleConnect) {
            configModuleTree.configModules.add(
                ConfigModule(
                    CommandDebugConfigModule(
                        resID = R.drawable.ic_module_cmd_debug_new,
                    )
                )
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

    /**
     * 查询设备状态信息
     */
    override fun queryStatusInfo() {
        val command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                timeout = AppContants.Communication.DELAY_10000_MILLIS,
                errorConfig = ErrorConfig.silentConfig() // 状态查询失败不显示错误
            )
        )
    }

    /**
     * 处理指令响应 - 重写父类方法处理M20S特定的指令
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(cmdStr, IOTCommandType.QUERY_DEVICE_STATUS)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                    }
                    is IOTCommandResult.Success -> {
                        initStatusInfo(result.data)
                    }
                }
            }
            else -> {
                // 其他指令交给父类处理
                super.handleCommandResponse(cmdStr)
            }
        }
    }

    /**
     * 初始化状态信息 - 处理M20S设备状态数据
     */
    private fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle

                // 检查电台模块是否可用
                updateRadioModuleStatus(stateInfo.self_check.uppercase().contains("RADIO:1"))

                val deviceAbnormalList = if (stateInfo.self_check.isEmpty()) {
                    arrayListOf<String>()
                } else {
                    DeviceStatusHelper.checkDeviceAbnormal(stateInfo.self_check)
                }

                // 移除特定的故障信息
                deviceAbnormalList.remove("电台故障")
                deviceAbnormalList.remove("太阳能控制器故障")
                
                val status = if (deviceAbnormalList.isEmpty()) "正常" else "故障"
                val logoResId = if (deviceAbnormalList.isEmpty()) {
                    mHeadStates.productNormalResId.get()
                } else {
                    mHeadStates.productErrorResId.get()
                }
                
                mHeadStates.productLogoResId.set(logoResId)
                mHeadStates.deviceStatusCode.set(if (deviceAbnormalList.isEmpty()) "0" else "-3")
                mHeadStates.warnErrorText.set(status)

                // 更新测量数据
                updateMeasureData(stateInfo)

            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 更新测量数据显示
     */
    private fun updateMeasureData(stateInfo: CommonCurrentStateInfo) {
        val newXAngle = DeviceStatusInfoProcessor.formatDoubleValue(
            stateInfo.x_Angle,
            "--",
            2
        ) + "°"

        val newYAngle = DeviceStatusInfoProcessor.formatDoubleValue(
            stateInfo.y_Angle,
            "--",
            2
        ) + "°"

        val newZAngle = DeviceStatusInfoProcessor.formatDoubleValue(
            stateInfo.z_Angle,
            "--",
            2
        ) + "°"

        // 刷新测量数据项
        binding.rvModule.bindingAdapter.getModel<M20SMeasureDataItem>(0)
            .refreshStatus(newXAngle, newYAngle, newZAngle)
    }

    /**
     * 更新电台模块状态，false 表示电台模块不可用，true 表示电台模块可用
     */
    private fun updateRadioModuleStatus(enable: Boolean) {
        // 刷新模块状态
        binding.rvModule.models?.forEach { item ->
            if (item is ConfigModuleTree) {
                item.configModules.find { configModule ->
                    configModule.functionModule.name.contains("电台配置")
                }?.functionModule?.refreshSupport(enable)
            }
        }
    }
} 