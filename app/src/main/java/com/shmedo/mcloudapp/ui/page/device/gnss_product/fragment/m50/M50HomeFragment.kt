package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.BindingAdapter.BindingViewHolder
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50CurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ItemM50MeasureDataBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.M50MeasureDataItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.CommonSensorDataHistoryFragment
import com.shmedo.mcloudapp.ui.page.device.common.NewUniversalBaseDeviceHomeFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/1/24
 * 描述：一体式自供电 GNSS 接收机(M50)设备主页 - 支持4G和蓝牙两种通讯方式
 *
 */
class M50HomeFragment : NewUniversalBaseDeviceHomeFragment() {
    private var measureDataItem: M50MeasureDataItem = M50MeasureDataItem()

    override fun initData() {
        super.initData()
        // 设置 M50 设备的 Logo 资源
        mHeadStates.productErrorResId.set(R.drawable.device_logo_m50_error)
        mHeadStates.productAlarmResId.set(R.drawable.device_logo_m50_alarm)
        mHeadStates.productOfflineResId.set(R.drawable.device_logo_m50_offline)
        mHeadStates.productNormalResId.set(R.drawable.device_logo_m50)
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
            val measureDataItem = getModel<M50MeasureDataItem>()

            // 设置数据绑定参数
            binding.setVariable(BR.m, measureDataItem)
            binding.setVariable(BR.click, M50ClickProxy())
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
                            navId = R.id.action_global_to_m50BaseInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "网络信息",
                            resID = R.drawable.ic_module_net_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_m50NetInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "状态信息",
                            resID = R.drawable.ic_module_state_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = R.id.action_global_to_m50StatusInfoFragment
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "卫星信息",
                            resID = R.drawable.ic_module_satellite_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = 0, // 暂未实现
                            isSupport = false
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
                        navId = R.id.action_global_to_m50WorkModelParamFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "网络配置",
                        resID = R.drawable.ic_module_network_setting,
                        navId = R.id.action_global_to_m50NetworkConfigFragment
                    )
                ),
                ConfigModule(
                    DataCenterModule(
                        name = "链路配置",
                        resID = R.drawable.ic_module_datacenter_new,
                        navId = R.id.action_global_to_universalDataCenterHomeFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "电台配置",
                        resID = R.drawable.ic_module_lora_new,
                        navId = R.id.action_global_to_m50RadioSettingFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "传感配置",
                        resID = R.drawable.ic_module_sensor_setting_new,
                        navId = R.id.action_global_to_m50SensorConfigFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "端口配置",
                        resID = R.drawable.ic_module_serial_port,
                        navId = R.id.action_global_to_m50SerialPortParamFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "CORS接入",
                        resID = R.drawable.ic_module_cors,
                        navId = 0,
                        isSupport = false
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
                ),
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

        var command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=0")
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 拍照
     */
    private fun takePhoto() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=1")
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
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
                if (cmdStr.contains("method=1")) {
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
                if (cmdStr.contains("method=1")) {
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
                if (cmdStr.contains("method=1")) {
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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result =
                    iotParseManager.parse<String>(cmdStr, IOTCommandType.QUERY_DEVICE_STATUS)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initStatusInfo(result.data)
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
                        val errMsg = "拍照出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = false)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        processSampleResponse(result.data)
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<M50CurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle

                val status = when (stateInfo.deviceStatus) {
                    "-2" -> "告警"
                    "-3" -> "故障"
                    else -> "正常"
                }
                mHeadStates.productLogoResId.set(
                    status.compareAndReturn(
                        "故障",
                        R.drawable.device_logo_m50_error,
                        status.compareAndReturn(
                            "告警",
                            R.drawable.device_logo_m50_alarm,
                            R.drawable.device_logo_m50
                        )
                    )
                )
                mHeadStates.deviceStatusCode.set(stateInfo.deviceStatus)
                mHeadStates.warnErrorText.set(status)
            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 处理召测响应
     */
    private fun processSampleResponse(content: String) {
        try {
            // {"sum_value":2,"x_value":"22","y_value":"22","z_value":"22"}
            val resultMap = MoshiUtil.fromJson<Map<String, String>>(content) ?: return
            if (resultMap.containsKey("sum_value")
                && resultMap.containsKey("x_value")
                && resultMap.containsKey("y_value")
                && resultMap.containsKey("z_value")
            ) {
                val resultantDisplacement =
                    resultMap["sum_value"]?.let { "$it mm" }
                        ?: AppContants.PLACE_HOLDER_VALUE
                val xDisplacement =
                    resultMap["x_value"]?.let { "$it mm" }
                        ?: AppContants.PLACE_HOLDER_VALUE
                val yDisplacement =
                    resultMap["y_value"]?.let { "$it mm" }
                        ?: AppContants.PLACE_HOLDER_VALUE
                val zDisplacement =
                    resultMap["z_value"]?.let { "$it mm" }
                        ?: AppContants.PLACE_HOLDER_VALUE

                // 刷新测量数据项
                binding.rvModule.bindingAdapter.getModel<M50MeasureDataItem>(0)
                    .refreshStatus(
                        resultantDisplacement,
                        xDisplacement,
                        yDisplacement,
                        zDisplacement
                    )
                return
            }
        } catch (e: Exception) {
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    // 扩展 ClickProxy 以支持 M50 特有的功能
    inner class M50ClickProxy {
        /**
         * 跳转到位置信息页面
         */
        fun onGotoLocationClick() {
            if (isBleDisconnected()) {
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
                CommonSensorDataHistoryFragment.newBundleArguments(
                    productType,
                    deviceInfo
                )
            )
        }
    }
} 