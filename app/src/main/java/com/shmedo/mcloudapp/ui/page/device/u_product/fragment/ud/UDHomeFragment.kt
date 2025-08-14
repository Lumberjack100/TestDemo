package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.BindingAdapter.BindingViewHolder
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.ItemUdMeasureDataBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.UDMeasureDataItem
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.CommonSensorDataHistoryFragment
import com.shmedo.mcloudapp.utils.UDDeviceStatusHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/1/24
 * 描述：一体式雷达水位计(DR030)设备主页
 */
class UDHomeFragment : BaseDeviceHomeFragment() {

    private var measureDataItem: UDMeasureDataItem = UDMeasureDataItem()
    private var measureDataLoadingDialogId = ""

    private var abnormalInfoJob: Job? = null
    private var queryMeasureResultTimeoutJob: Job? = null
    private var repeatPollNum = 0 // 重复轮询次数

    override fun initData() {
        super.initData()
        // 设置 UD 设备的 Logo 资源
        mHeadStates.productErrorResId.set(R.drawable.device_logo_dr030_error)
        mHeadStates.productAlarmResId.set(R.drawable.device_logo_dr030_alarm)
        mHeadStates.productOfflineResId.set(R.drawable.device_logo_dr030_offline)
        mHeadStates.productNormalResId.set(R.drawable.device_logo_dr030)
        mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        // 扩展适配器支持 UDMeasureDataItem
        binding.rvModule.bindingAdapter.addType<UDMeasureDataItem>(R.layout.item_ud_measure_data)
    }

    override fun BindingViewHolder.processOtherItemViewBind(itemViewType: Int) {
        if (itemViewType == R.layout.item_ud_measure_data) {
            val binding = getBinding<ItemUdMeasureDataBinding>()

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
                        navId = R.id.action_global_to_udBaseInfoFragment
                    ).toUnified(),

                    CommonModule(
                        name = "网络信息",
                        resID = R.drawable.ic_module_net_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_udNetInfoFragment
                    ).toUnified(),

                    CommonModule(
                        name = "状态信息",
                        resID = R.drawable.ic_module_state_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_udSensorInfoFragment
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

        // 设备配置模块
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree(
            configModules = arrayListOf(
                CommonModule(
                    name = "工作模式",
                    resID = R.drawable.ic_module_work_mode_new,
                    navId = R.id.action_global_to_udWorkModelParamFragment
                ).toUnified(),

                CommonModule(
                    name = "网络配置",
                    resID = R.drawable.ic_module_network_setting,
                    navId = R.id.action_global_to_udMobileNetworkParamFragment
                ).toUnified(),

                DataCenterModule(
                    name = "链路配置",
                    resID = R.drawable.ic_module_datacenter_new,
                    navId = R.id.action_global_to_udProductDataCenterHomeFragment
                ).toUnified(),

                CommonModule(
                    name = "海拔配置",
                    resID = R.drawable.ic_module_cors,
                    navId = R.id.action_global_to_udCORSParamFragment
                ).toUnified(),

                CommonModule(
                    name = "传感配置",
                    resID = R.drawable.ic_module_sensor_setting_new,
                    navId = R.id.action_global_to_udProductSensorParamFragment
                ).toUnified(),

                CommonModule(
                    name = "端口配置",
                    resID = R.drawable.ic_module_serial_port,
                    navId = R.id.action_global_to_udSerialPortParamFragment
                ).toUnified(),

                CommonModule(
                    name = "LORA配置",
                    resID = R.drawable.ic_module_lora_new,
                    navId = R.id.action_global_to_loraSettingFragment
                ).toUnified(),

                CommonModule(
                    name = "报警配置",
                    resID = R.drawable.ic_module_alarm_new,
                    navId = R.id.action_global_to_alarmSettingFragment
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
                ).toUnified()
            )
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
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "method=0")

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false,
                errorConfig = ErrorConfig.silentConfig(), // 状态查询失败不显示错误
                enableBusinessParseFailureInterrupt = false
            )
        )
    }

    override fun lazyLoadData() {
        super.lazyLoadData()
        initLastHistorySensorData()
    }

    /**
     * 初始化历史传感器数据
     */
    private fun initLastHistorySensorData() {
        if (NetworkUtils.isConnected()) {
            launchWithViewLifecycle {
                val resultMap: Map<String, String> =
                    deviceRequestViewModel.queryLatestSensorData(
                        deviceInfo.deviceToken,
                        iotSensorTypeList = arrayListOf("904", "206")
                    )
                if (resultMap.isEmpty())
                    return@launchWithViewLifecycle

                updateMeasureDataFromMap(resultMap)
            }
        }
    }

    /**
     * 从Map更新测量数据显示
     */
    private fun updateMeasureDataFromMap(resultMap: Map<String, String>) {
        val waterSurfaceElevation = resultMap["liquid_surface_alt"]?.let { "$it m" }
            ?: AppContants.PLACE_HOLDER_VALUE
        val airDistance =
            resultMap["ullage"]?.let { "$it m" } ?: AppContants.PLACE_HOLDER_VALUE
        val installationAngle =
            resultMap["z"]?.let { "$it °" } ?: AppContants.PLACE_HOLDER_VALUE
        val todayRainfall =
            resultMap["today_rain"]?.let { "$it mm" } ?: AppContants.PLACE_HOLDER_VALUE
        val measurementTime = resultMap["time"]?.replace(".000", "")?.replace("-", ".")
            ?: AppContants.PLACE_HOLDER_VALUE

        measureDataItem.refreshMeasureData(
            waterSurfaceElevation,
            airDistance,
            installationAngle,
            todayRainfall,
            measurementTime
        )
    }

    /**
     * 查询测量数据
     */
    private fun queryMeasureData() {
        val command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=0")

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false,
                errorConfig = ErrorConfig.customConfig { errorMsg ->
                    stopMeasurementAnimation()
                    showMessageDialog("出错了: $errorMsg")
                }
            )
        )
    }

    /**
     * 测量数据
     */
    private fun measureData() {
        val command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=1")

        measureDataLoadingDialogId =
            showLoadingWithUUID(StringUtils.getString(R.string.processing))

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 已经显示了自定义加载对话框
                errorConfig = ErrorConfig.customConfig { errorMsg ->
                    stopMeasurementAnimation()
                    showMessageDialog("测量数据出错: $errorMsg")
                }
            )
        )
    }

    /**
     * 拍照
     */
    private fun takePhoto() {
        val command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=2")

        measureDataLoadingDialogId =
            showLoadingWithUUID(StringUtils.getString(R.string.processing))

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 已经显示了自定义加载对话框
                errorConfig = ErrorConfig.customConfig { errorMsg ->
                    stopMeasurementAnimation()
                    showMessageDialog("拍照出错: $errorMsg")
                }
            )
        )
    }

    /**
     * 处理指令响应 - 重写父类方法处理UD特定的指令
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result =
                    iotParseManager.parse<String>(cmdStr, IOTCommandType.MD_GET_DEVICE_STATUS)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                    }

                    is IOTCommandResult.Success -> {
                        initStatusInfo(cmdStr, result.data)
                    }
                }
            }

            IOTCommandType.SAMPLE -> { // 召测
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "出错了: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                        stopMeasurementAnimation()
                    }

                    is IOTCommandResult.Success -> {
                        dismissLoadingDialog(measureDataLoadingDialogId)
                        processSampleResponse(cmdStr, result.data)
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
     * 初始化状态信息
     */
    private fun initStatusInfo(cmdStr: String, content: String) {
        if (!cmdStr.contains("method=0"))
            return

        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle

                val status = when (stateInfo.deviceStatus) {
                    "-2" -> "告警"
                    "-3" -> "故障"
                    else -> "正常"
                }

                mHeadStates.deviceStatusCode.set(stateInfo.deviceStatus)
                mHeadStates.productLogoResId.set(
                    when (status) {
                        "告警" -> mHeadStates.productAlarmResId.get()
                        "故障" -> mHeadStates.productErrorResId.get()
                        else -> mHeadStates.productNormalResId.get()
                    }
                )

                if (status == "正常") {
                    mHeadStates.warnErrorText.set("正常")
                    return@launchWithViewLifecycle
                }

                processAbnormalInfo(stateInfo.deviceError, stateInfo.deviceWarn)
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 处理异常信息
     */
    private fun processAbnormalInfo(
        deviceError: Map<String, String>? = null,
        deviceWarn: Map<String, String>? = null
    ) {
        try {
            val errorInfoList = UDDeviceStatusHelper.processAbnormalInfo(deviceError, deviceWarn)
            handleAbnormalInfo(errorInfoList)
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 处理设备异常信息轮播展示
     * 每隔1.5秒切换一次，取出异常信息列表中的每一条异常信息，轮播显示
     */
    private fun handleAbnormalInfo(errorInfoList: List<String>) {
        // 取消之前的job（如果存在）
        abnormalInfoJob?.cancel()

        // 如果列表为空，直接返回
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
                        delay(1500) // 延迟1.5秒
                    }
                }
            }.collect { errorInfo ->
                mHeadStates.warnErrorText.set(errorInfo)
            }
        }
    }

    /**
     * 处理召测响应
     */
    private fun processSampleResponse(cmdStr: String, content: String) {
        try {
            // {"time":"2024-09-04 11:07:34"}
            // {"filename":"20240904111431"}
            // {"obj_alt":28.320,"ld_value":3.514,"z_angle":86.4,"time":"2024-09-04 14:32:32"}

            if (cmdStr.contains("method=1")) {
                // 测量数据
                startMeasurementAnimation()
                clearQueryMeasureResultTimeoutJob()
                startQueryMeasureResultJob()

            } else if (cmdStr.contains("method=2")) {
                // 拍照
                showMessageDialog("拍照指令已下发，请稍后在历史数据中查看拍照图片")

            } else {
                // method=0 查询最新数据
                val resultMap: Map<String, String> = if (content.isEmpty()) mapOf()
                else MoshiUtil.fromJson<Map<String, String>>(content) ?: mapOf()

                // 已经有数据
                if (resultMap.containsKey("obj_alt")
                    || resultMap.containsKey("ld_value")
                    || resultMap.containsKey("z_angle")
                    || resultMap.containsKey("today_rain")
                    || resultMap.containsKey("time")
                ) {
                    stopMeasurementAnimation()

                    val waterSurfaceElevation =
                        resultMap["obj_alt"]?.let { "$it m" } ?: AppContants.PLACE_HOLDER_VALUE
                    val airDistance =
                        resultMap["ld_value"]?.let { "$it m" } ?: AppContants.PLACE_HOLDER_VALUE
                    val installationAngle =
                        resultMap["z_angle"]?.let { "$it °" } ?: AppContants.PLACE_HOLDER_VALUE
                    val todayRainfall =
                        resultMap["today_rain"]?.let { "$it mm" } ?: AppContants.PLACE_HOLDER_VALUE
                    val measurementTime =
                        resultMap["time"]?.replace("-", ".") ?: AppContants.PLACE_HOLDER_VALUE

                    measureDataItem .refreshMeasureData(
                            waterSurfaceElevation,
                            airDistance,
                            installationAngle,
                            todayRainfall,
                            measurementTime
                        )
                    return
                }

                // 无数据，启动轮询
                startQueryMeasureResultJob()
            }
        } catch (e: Exception) {
            stopMeasurementAnimation()
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 开始测量动画
     */
    private fun startMeasurementAnimation() {
        // 显示进度条并开始动画
        measureDataItem.setMeasuringStatus(true)
        binding.rvModule.bindingAdapter.getModel<UDMeasureDataItem>(0).let {
            val viewHolder = binding.rvModule.findViewHolderForAdapterPosition(0)
            viewHolder?.itemView?.findViewById<com.shmedo.mcloudapp.ui.widget.ProgressMaterialButton>(
                R.id.btn_measure_data
            )?.startProgressAnimation()
        }
    }

    /**
     * 停止测量动画
     */
    private fun stopMeasurementAnimation() {
        dismissLoadingDialog(measureDataLoadingDialogId)
        // 隐藏进度条并停止动画
        measureDataItem.setMeasuringStatus(false)

        val viewHolder = binding.rvModule.findViewHolderForAdapterPosition(0)
        viewHolder?.itemView?.findViewById<com.shmedo.mcloudapp.ui.widget.ProgressMaterialButton>(
            R.id.btn_measure_data
        )?.stopProgressAnimation()
    }

    /**
     * 启动查询测量结果的轮询任务
     */
    private fun startQueryMeasureResultJob() {
        // 启动一个新的协程作为超时Job
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = launchWithViewLifecycle {
            if (repeatPollNum >= REPEAT_POLL_NUM) {
                stopMeasurementAnimation()
                return@launchWithViewLifecycle
            }
            delay(AppContants.Communication.DELAY_5000_MILLIS) // 延迟5秒
            repeatPollNum++
            Timber.d("查询测量数据结果轮询次数：$repeatPollNum")
            queryMeasureData()
        }
    }

    /**
     * 清理查询测量结果超时任务
     */
    private fun clearQueryMeasureResultTimeoutJob() {
        repeatPollNum = 0
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        abnormalInfoJob?.cancel()
        abnormalInfoJob = null
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = null
    }

    /**
     * 扩展 ClickProxy 以支持 UD 特有的功能
     */
    inner class ClickProxy : BaseClickProxy() {
        override fun onMeasureDataClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (measureDataItem.isMeasuring.get())
                return

            measureData()
        }

        override fun onTakePhotoClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            takePhoto()
        }

        override fun onGoToSensorDataHistoryClick() {
            nav().safeNavigate(
                R.id.action_global_to_commonSensorDataHistoryFragment,
                CommonSensorDataHistoryFragment.newBundleArguments(productType, deviceInfo)
            )
        }
    }

    companion object {
        const val REPEAT_POLL_NUM = 10
    }
}
