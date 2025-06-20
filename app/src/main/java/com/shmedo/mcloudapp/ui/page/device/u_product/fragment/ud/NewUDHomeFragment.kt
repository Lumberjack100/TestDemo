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
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
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
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.UDMeasureDataItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.CommonSensorDataHistoryFragment
import com.shmedo.mcloudapp.ui.page.device.common.NewUniversalBaseDeviceHomeFragment
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
 * 描述：一体化雷达水位计(DR030)设备主页 - 支持4G和蓝牙两种通讯方式（重构版本）
 */
class NewUDHomeFragment : NewUniversalBaseDeviceHomeFragment() {
    private var measureDataItem: UDMeasureDataItem = UDMeasureDataItem()
    private var measureDataLoadingDialogId = ""
    
    private var abnormalInfoJob: Job? = null
    private var queryMeasureResultTimeoutJob: Job? = null
    private var repeatPollNum = 0 //重复轮询次数

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
            val measureDataItem = getModel<UDMeasureDataItem>()

            // 设置数据绑定参数
            binding.setVariable(BR.m, measureDataItem)
            binding.setVariable(BR.click, UDClickProxy())
            binding.executePendingBindings()
        }
    }

    override fun initModuleData() {
        val groupList = mutableListOf<Any>()

        // 添加测量数据作为第一个项目
        groupList.add(measureDataItem)
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))

        groupList.add(DeviceStatusInfoGroupItem("设备信息"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    ConfigModule(
                        CommonModule(
                            name = "基本信息",
                            resID = R.drawable.ic_module_basic_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_udBaseInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "网络信息",
                            resID = R.drawable.ic_module_net_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_udNetInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "状态信息",
                            resID = R.drawable.ic_module_state_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_udSensorInfoFragment
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
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree(
            configModules = arrayListOf(
                ConfigModule(
                    CommonModule(
                        name = "工作模式",
                        resID = R.drawable.ic_module_work_mode_new,
                        navId = R.id.action_global_to_udWorkModelParamFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "网络配置",
                        resID = R.drawable.ic_module_network_setting,
                        navId = R.id.action_global_to_udMobileNetworkParamFragment
                    )
                ),
                ConfigModule(
                    DataCenterModule(
                        name = "链路配置",
                        resID = R.drawable.ic_module_datacenter_new,
                        navId = R.id.action_global_to_udProductDataCenterHomeFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "海拔配置",
                        resID = R.drawable.ic_module_cors,
                        navId = R.id.action_global_to_udCORSParamFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "传感配置",
                        resID = R.drawable.ic_module_sensor_setting_new,
                        navId = R.id.action_global_to_udProductSensorParamFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "端口配置",
                        resID = R.drawable.ic_module_serial_port,
                        navId = R.id.action_global_to_udSerialPortParamFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "电台配置",
                        resID = R.drawable.ic_module_lora_new,
                        navId = R.id.action_global_to_udRadioParamFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "报警配置",
                        resID = R.drawable.ic_module_alarm_new,
                        navId = R.id.action_global_to_alarmSettingFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "时间校准",
                        resID = R.drawable.ic_module_time_calibration_new,
                        navId = R.id.action_global_to_time_calibration
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "系统配置",
                        resID = R.drawable.ic_module_system_setting,
                        navId = R.id.action_global_to_advancedSettingFragment
                    )
                )
            )
        )

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

    override fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "method=0")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        super.lazyLoadData()
        initLastHistorySensorData()
    }

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

                binding.rvModule.bindingAdapter.getModel<UDMeasureDataItem>(0)
                    .refreshMeasureData(
                        waterSurfaceElevation,
                        airDistance,
                        installationAngle,
                        todayRainfall,
                        measurementTime
                    )
            }
        }
    }

    /**
     * 查询测量数据
     */
    private fun queryMeasureData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=0")
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 测量数据
     */
    private fun measureData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=1")
        commandItems.add(command)

        measureDataLoadingDialogId =
            showLoadingWithUUID(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 拍照
     */
    private fun takePhoto() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=2")
        commandItems.add(command)

        measureDataLoadingDialogId =
            showLoadingWithUUID(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.SAMPLE -> {
                stopMeasurementAnimation()
                if (cmdStr.contains("method=1")) {
                    super.doCmdResponseResultError(
                        cmdStr = cmdStr,
                        errMsg = "测量数据指令下发出错: $errMsg",
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                } else if (cmdStr.contains("method=2")) {
                    super.doCmdResponseResultError(
                        cmdStr = cmdStr,
                        errMsg = "拍照指令下发出错: $errMsg",
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                }
            }

            else -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog
                )
            }
        }
    }

    override fun doCmdResponseResultTimeOut(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.SAMPLE -> {
                stopMeasurementAnimation()
                if (!cmdStr.contains("method=0")) {
                    super.doCmdResponseResultTimeOut(
                        cmdStr = cmdStr,
                        errMsg = errMsg,
                        isShowErrMsg = true,
                        isMessageDialog = true
                    )
                }
            }

            else -> {
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog
                )
            }
        }
    }

    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean,
        errMsg: String
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.SAMPLE -> {
                stopMeasurementAnimation()
                if (cmdStr.contains("method=0")) {
                    super.showNearbyCommunicationTimeoutAlert(
                        cmdStr = cmdStr,
                        isDismissLoadingDialog = isDismissLoadingDialog,
                        isShowErrMsg = false,
                        isMessageDialog = isMessageDialog,
                        errMsg = errMsg
                    )
                } else {
                    super.showNearbyCommunicationTimeoutAlert(
                        cmdStr = cmdStr,
                        isDismissLoadingDialog = isDismissLoadingDialog,
                        isShowErrMsg = true,
                        isMessageDialog = true,
                        errMsg = errMsg
                    )
                }
            }

            else -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }
        }
    }

    override fun processOtherCmdResult(commandType: IOTCommandType, cmdStr: String) {
        when (commandType) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result =
                    iotParseManager.parse<String>(cmdStr, IOTCommandType.MD_GET_DEVICE_STATUS)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initStatusInfo(cmdStr, result.data)
                    }
                }
            }

            IOTCommandType.SAMPLE -> { //召测
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "召测出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                        stopMeasurementAnimation()
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        dismissLoadingDialog(measureDataLoadingDialogId)
                        processSampleResponse(cmdStr, result.data)
                    }
                }
            }

            else -> {

            }
        }
    }

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
                    status.compareAndReturn(
                        "故障",
                        R.drawable.device_logo_dr030_error,
                        status.compareAndReturn(
                            "告警",
                            R.drawable.device_logo_dr030_alarm,
                            R.drawable.device_logo_dr030
                        )
                    )
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

    /**
     * 处理召测响应
     */
    private fun processSampleResponse(cmdStr: String, content: String) {
        try {
            //{"time":"2024-09-04 11:07:34"}
            //{"filename":"20240904111431"}
            //{"obj_alt":28.320,"ld_value":3.514,"z_angle":86.4,"time":"2024-09-04 14:32:32"}
            if (cmdStr.contains("method=1")) {
                startMeasurementAnimation()
                clearQueryMeasureResultTimeoutJob()
                startQueryMeasureResultJob()
            } else if (cmdStr.contains("method=2")) {
                showMessageDialog("拍照指令已下发，请稍后在历史数据中查看拍照图片")
            } else {
                val resultMap: Map<String, String> =
                    if (content.isEmpty()) mapOf() else MoshiUtil.fromJson<Map<String, String>>(
                        content
                    ) ?: mapOf()

                //已经有数据
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

                    binding.rvModule.bindingAdapter.getModel<UDMeasureDataItem>(0)
                        .refreshMeasureData(
                            waterSurfaceElevation,
                            airDistance,
                            installationAngle,
                            todayRainfall,
                            measurementTime
                        )
                    return
                }

                startQueryMeasureResultJob()
            }
        } catch (e: Exception) {
            stopMeasurementAnimation()
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun startMeasurementAnimation() {
        // 显示进度条并开始动画
        measureDataItem.setMeasuringStatus(true)
        binding.rvModule.bindingAdapter.getModel<UDMeasureDataItem>(0).let {
            val viewHolder = binding.rvModule.findViewHolderForAdapterPosition(0)
            viewHolder?.itemView?.findViewById<com.shmedo.mcloudapp.ui.widget.ProgressMaterialButton>(R.id.btn_measure_data)
                ?.startProgressAnimation()
        }
    }

    private fun stopMeasurementAnimation() {
        dismissLoadingDialog(measureDataLoadingDialogId)
        // 隐藏进度条并停止动画
        measureDataItem.setMeasuringStatus(false)
        binding.rvModule.bindingAdapter.getModel<UDMeasureDataItem>(0)?.let {
            val viewHolder = binding.rvModule.findViewHolderForAdapterPosition(0)
            viewHolder?.itemView?.findViewById<com.shmedo.mcloudapp.ui.widget.ProgressMaterialButton>(R.id.btn_measure_data)
                ?.stopProgressAnimation()
        }
    }

    private fun startQueryMeasureResultJob() {
        //启动一个新的协程作为超时Job
        queryMeasureResultTimeoutJob?.cancel()
        queryMeasureResultTimeoutJob = launchWithViewLifecycle {
            if (repeatPollNum >= REPEAT_POLL_NUM) {
                stopMeasurementAnimation()
                return@launchWithViewLifecycle
            }
            delay(AppContants.Communication.DELAY_5000_MILLIS) //延迟 timeMillis 秒
            repeatPollNum++
            Timber.d("查询测量数据轮询次数：$repeatPollNum")
            queryMeasureData()
        }
    }

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

    // 扩展 ClickProxy 以支持 UD 特有的功能
    inner class UDClickProxy {
        fun onMeasureDataClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (measureDataItem.isMeasuring.get())
                return

            measureData()
        }

        fun onTakePhotoClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            takePhoto()
        }

        fun onGoToSensorDataHistoryClick() {
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