package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m20s

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.BindingAdapter.BindingViewHolder
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product.UDInitialValueEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmTriggerValueInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.ItemM50MeasureDataBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigBannerItem
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.M50MeasureDataItem
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.CommonSensorDataHistoryFragment
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/12/19
 * @desc:  普适型 GNSS 接收机(M20S)配置主页
 *
 * 优化特点：
 * 1. 继承自 OptimizedBaseDeviceHomeFragment，使用新的通信架构
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 保持原有的M20S特定业务逻辑不变
 * 5. 支持4G和蓝牙两种通讯方式
 */
class M20SHomeFragment : BaseDeviceHomeFragment() {

    private var measureDataItem: M50MeasureDataItem = M50MeasureDataItem()
    private var abnormalInfoJob: Job? = null

    // 角度数据存储
    private data class AngleData(
        var xCurrent: Double = 0.0,
        var yCurrent: Double = 0.0,
        var zCurrent: Double = 0.0,
        var xInitial: Double = 0.0,
        var yInitial: Double = 0.0,
        var zInitial: Double = 0.0,
        var angleTrigger: Double = 0.0
    )

    private val angleData = AngleData()


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

        // 扩展适配器支持 M50MeasureDataItem
        binding.rvModule.bindingAdapter.addType<M50MeasureDataItem>(R.layout.item_m50_measure_data)
    }

    override fun BindingViewHolder.processOtherItemViewBind(itemViewType: Int) {
        if (itemViewType == R.layout.item_m50_measure_data) {
            val binding = getBinding<ItemM50MeasureDataBinding>()

            // 设置数据绑定参数
            binding.setVariable(BR.m, measureDataItem)
            binding.setVariable(BR.click, ClickProxy())
            binding.executePendingBindings()
        }
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
                    CommonModule(
                        name = "基本信息",
                        resID = R.drawable.ic_module_basic_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_m20SBaseInfoFragment
                    ).toUnified(),
                    CommonModule(
                        name = "网络信息",
                        resID = R.drawable.ic_module_net_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_m20SNetInfoFragment
                    ).toUnified(),
                    CommonModule(
                        name = "状态信息",
                        resID = R.drawable.ic_module_state_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_m20SStatusInfoFragment
                    ).toUnified(),
                    CommonModule(
                        name = "运行信息",
                        resID = R.drawable.ic_module_satellite_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_m20SRunningInfoFragment
                    ).toUnified()
                )
            )
        )

        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))

        // 设备配置模块
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree()

        // 根据产品类型添加不同的配置模块
        configModuleTree.configModules.add(
            CommonModule(
                name = "工作模式",
                resID = R.drawable.ic_module_work_mode_new,
                navId = R.id.action_global_to_m20SWorkModelParamFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "网络配置",
                resID = R.drawable.ic_module_network_setting,
                navId = R.id.action_global_to_m50NetworkConfigFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            DataCenterModule(
                name = "链路配置",
                resID = R.drawable.ic_module_datacenter_new,
                navId = R.id.action_global_to_dataCenterHomeFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "电台配置",
                resID = R.drawable.ic_module_lora_new,
                navId = R.id.action_global_to_m20SRadioSettingFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "GNSS配置",
                resID = R.drawable.ic_module_cors,
                navId = R.id.action_global_to_m50GNSSConfigFragment,
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "倾斜触发",
                resID = R.drawable.ic_module_sensor_setting_new,
                navId = R.id.action_global_to_m50SensorConfigFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "串口配置",
                resID = R.drawable.ic_module_serial_port,
                navId = 0,
                isSupport = false
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "报警配置",
                resID = R.drawable.ic_module_alarm_new,
                navId = R.id.action_global_to_alarmSettingFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "时间校准",
                resID = R.drawable.ic_module_time_calibration_new,
                navId = R.id.action_global_to_time_calibration
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "系统配置",
                resID = R.drawable.ic_module_system_setting,
                navId = R.id.action_global_to_advancedSettingFragment
            ).toUnified()
        )

        // 蓝牙连接时添加指令调试模块
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

    /**
     * 查询设备状态信息
     */
    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        // 召测 method=0  获取最新数据时间及数据
        commands.add(IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=0"))

        // 召测 method=2 获取最新初始化完成时间
        commands.add(IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=2"))

        //获取当前角度值，通过遥测获取，物模型103_1
        commands.add(IOTCommandUtil.getCommand(IOTCommandType.SAMPLE))

        //获取初始角度值
        commands.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_SENSOR_INITIAL,
                UDInitialValueEntity(method = "0", type = "2").toCommandString()
            )
        )

        //获取角度触发值
        commands.add(IOTCommandUtil.getCommand(IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE))

        // 查询设备状态
        commands.add(IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS))

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false,
                errorConfig = ErrorConfig.silentConfig(), // 状态查询失败不显示错误
                enableBusinessParseFailureInterrupt = false // 不启用业务层解析失败中断功能，中断后续指令执行
            )
        )
    }

    /**
     * 处理指令响应 - 重写父类方法处理M20S特定的指令
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.SAMPLE -> {
                val result = iotParseManager.parse<String>(cmdStr, IOTCommandType.SAMPLE)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "召测出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                    }

                    is IOTCommandResult.Success -> {
                        processSampleResponse(cmdStr, result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_SENSOR_INITIAL -> {
                val result = iotParseManager.parse<Map<String, String>>(
                    cmdStr,
                    IOTCommandType.MD_SET_SENSOR_INITIAL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询倾角初始值出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                    }

                    is IOTCommandResult.Success -> {
                        processInitialAngleResponse(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE -> {
                val result = iotParseManager.parse<AlarmTriggerValueInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询角度触发值出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                    }

                    is IOTCommandResult.Success -> {
                        processAngleTriggerResponse(result.data)
                    }
                }
            }

            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result =
                    iotParseManager.parse<String>(cmdStr, IOTCommandType.QUERY_DEVICE_STATUS)
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
     * 处理召测响应
     */
    private fun processSampleResponse(cmdStr: String, content: String) {
        try {
            // $cmd=sample&method=0&datastreams={"date":"2025-07-18 17:12:22","sum_value":6013.101,"x_value":-1429.354,"y_value":-0.006,"z_value":-5840.747}
            // $cmd=sample&datastreams={"103_1":"0.012,-0.020,89.985,3.788,-28.510,-1022.529","224_1":"0.000,0.000,0.000"}
            val resultMap = MoshiUtil.fromJson<Map<String, Any>>(content) ?: return

            // 处理位移数据 (method=0)
            if (cmdStr.contains("method=0") && resultMap.containsKey("x_value")
                && resultMap.containsKey("y_value")
                && resultMap.containsKey("z_value")
            ) {
                val xDisplacement = resultMap["x_value"]?.let { "$it mm" }
                    ?: AppContants.PLACE_HOLDER_VALUE
                val yDisplacement = resultMap["y_value"]?.let { "$it mm" }
                    ?: AppContants.PLACE_HOLDER_VALUE
                val zDisplacement = resultMap["z_value"]?.let { "$it mm" }
                    ?: AppContants.PLACE_HOLDER_VALUE

                // 检查是否包含时间信息
                if (resultMap.containsKey("date")) {
                    val latestDataTime = resultMap["date"] ?: AppContants.PLACE_HOLDER_VALUE
                    measureDataItem.refreshStatusWithTime(
                        xDisplacement,
                        yDisplacement,
                        zDisplacement,
                        latestDataTime.toString()
                    )
                }
                return
            }

            //$cmd=sample&method=2&datastreams={"sw":1,"mode":8,"initdate":"0000-00-00 00:00:00","initENU":"0.000000,0.000000,0.000000","baseLine":0.000000,"fixRate":100.0,"gap_fixRate":100.0,"result":"0.000,0.000,0.000","status":"base-not-ready","dataSource":"ntrip","ntrip":{"status":"recv_rtcm","onlineRate":100.0,"connectCnt":1}}
            if (cmdStr.contains("method=2") && resultMap.containsKey("initdate")) {
                val initCompletionTime = resultMap["initdate"] ?: AppContants.PLACE_HOLDER_VALUE
                measureDataItem.refreshInitCompletionTime(initCompletionTime.toString())
            }

            // 处理角度数据 (103_1)
            if (!cmdStr.contains("method=") && resultMap.containsKey("103_1")) {
                val currentAngle = resultMap["103_1"]?.toString() ?: return
                currentAngle.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .let {
                        if (it.size >= 3) {
                            // 保存当前角度值
                            angleData.xCurrent = it[0].toDoubleOrNull() ?: 0.0
                            angleData.yCurrent = it[1].toDoubleOrNull() ?: 0.0
                            angleData.zCurrent = it[2].toDoubleOrNull() ?: 0.0

                            Timber.d("当前角度值: X=${angleData.xCurrent}, Y=${angleData.yCurrent}, Z=${angleData.zCurrent}")
                        }
                    }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 处理初始角度值响应
     */
    private fun processInitialAngleResponse(resultMap: Map<String, String>) {
        try {
            val method = resultMap["method"] ?: ""
            if (method == "0") { // 轮询测得的初始值
                if (resultMap.containsKey("xAxis") && resultMap.containsKey("yAxis") && resultMap.containsKey(
                        "zAxis"
                    )
                ) {
                    val xAxis = resultMap["xAxis"] ?: ""
                    val yAxis = resultMap["yAxis"] ?: ""
                    val zAxis = resultMap["zAxis"] ?: ""

                    // 保存初始角度值
                    angleData.xInitial = xAxis.toDoubleOrNull() ?: 0.0
                    angleData.yInitial = yAxis.toDoubleOrNull() ?: 0.0
                    angleData.zInitial = zAxis.toDoubleOrNull() ?: 0.0

                    Timber.d("初始角度值: X=${angleData.xInitial}, Y=${angleData.yInitial}, Z=${angleData.zInitial}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 处理角度触发值响应
     */
    private fun processAngleTriggerResponse(info: AlarmTriggerValueInfo) {
        try {
            // 解析角度触发值，参考 M50SensorConfigFragment 中的逻辑
            val level1 = info.level1.toDoubleOrNull() ?: 0.0

            // 保存角度触发值
            angleData.angleTrigger = level1

            Timber.d("角度触发值: ${angleData.angleTrigger}")
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
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
                //移除特定的故障信息
                deviceAbnormalList.remove("电台模块故障")
                deviceAbnormalList.remove("太阳能控制器故障")

                val deviceWarnList =
                    if (content.isEmpty()) arrayListOf<String>() else DeviceStatusHelper.checkM20Warn(
                        content
                    )
                // 添加角度告警检查
                val angleWarnings = calculateAngleWarnings()

                // 合并告警列表，但如果倾角加速度模块故障，则不显示角度告警
                val hasTiltSensorFault =
                    deviceAbnormalList.any { it.contains("倾角加速度模块故障") }
                val warnListWithAngles = if (hasTiltSensorFault) {
                    deviceWarnList
                } else {
                    deviceWarnList + angleWarnings
                }

                // 合并故障和告警信息，并进行过滤
                val mergedList = DeviceStatusHelper.mergeM50StatusInfo(
                    deviceAbnormalList,
                    warnListWithAngles as ArrayList<String>
                )

                val status =
                    if (deviceAbnormalList.isEmpty() && deviceWarnList.isEmpty()) "正常" else if (deviceAbnormalList.isNotEmpty()) "故障" else "告警"
                mHeadStates.productLogoResId.set(
                    when (status) {
                        "告警" -> mHeadStates.productAlarmResId.get()
                        "故障" -> mHeadStates.productErrorResId.get()
                        else -> mHeadStates.productNormalResId.get()
                    }
                )
                mHeadStates.deviceStatusCode.set(
                    when (status) {
                        "告警" -> "-2"
                        "故障" -> "-3"
                        else -> "0"
                    }
                )
                if (status == "正常") {
                    mHeadStates.warnErrorText.set("正常")
                    return@launchWithViewLifecycle
                }

                handleAbnormalInfo(mergedList)
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 计算角度偏移值并返回角度告警信息
     */
    private fun calculateAngleWarnings(): List<String> {
        val warnings = mutableListOf<String>()

        try {
            // 计算偏移角度值 = 当前角度值 - 初始角度值
            val xOffset = angleData.xCurrent - angleData.xInitial
            val yOffset = angleData.yCurrent - angleData.yInitial
            val zOffset = angleData.zCurrent - angleData.zInitial

            // 检查是否超过触发值
            if (kotlin.math.abs(xOffset) > angleData.angleTrigger) {
                warnings.add("X轴偏移角度过大")
            }
            if (kotlin.math.abs(yOffset) > angleData.angleTrigger) {
                warnings.add("Y轴偏移角度过大")
            }
            if (kotlin.math.abs(zOffset) > angleData.angleTrigger) {
                warnings.add("Z轴偏移角度过大")
            }

            Timber.d("角度偏移检查: X偏移=$xOffset, Y偏移=$yOffset, Z偏移=$zOffset, 触发值=${angleData.angleTrigger}")
        } catch (e: Exception) {
            Timber.e(e, "计算角度偏移值时出错")
        }

        return warnings
    }

    /**
     * 处理设备异常信息轮播展示
     * 每隔3秒切换一次，取出异常信息列表中的每一条异常信息，轮播显示
     */
    private fun handleAbnormalInfo(errorInfoList: List<String>) {
        //取消之前的job（如果存在）
        abnormalInfoJob?.cancel()

        //如果列表为空，直接返回
        if (errorInfoList.isEmpty()) {
            return
        }
        if (errorInfoList.size == 1) {
            mHeadStates.warnErrorText.set(errorInfoList[0])
            return
        }
        abnormalInfoJob = launchWithViewLifecycle {
            flow {
                while (true) {
                    errorInfoList.forEach { errorInfo ->
                        emit(errorInfo)
                        delay(1500) // 延迟3秒
                    }
                }
            }.collect { errorInfo ->
                mHeadStates.warnErrorText.set(errorInfo)
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 跳转到位置信息页面
         */
        override fun onGotoLocationClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            nav().safeNavigate(
                R.id.action_global_to_commonLocationInfoFragment,
                BaseIOTDeviceFragment.newBundleArguments(
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
            )
        }

        override fun onTakePhotoClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            // TODO: 实现拍照功能
        }

        override fun onGoToSensorDataHistoryClick() {
            nav().safeNavigate(
                R.id.action_global_to_commonSensorDataHistoryFragment,
                CommonSensorDataHistoryFragment.newBundleArguments(
                    productType,
                    deviceInfo
                )
            )
        }
    }

    /**
     * 更新电台模块状态，false 表示电台模块不可用，true 表示电台模块可用
     */
    private fun updateRadioModuleStatus(enable: Boolean) {
        // 刷新模块状态
        binding.rvModule.models?.forEach { item ->
            if (item is ConfigModuleTree) {
                item.configModules.find { functionModule ->
                    functionModule.name.contains("电台配置")
                }?.refreshSupport(enable)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        abnormalInfoJob?.cancel()
        abnormalInfoJob = null
    }
} 