package com.shmedo.mcloudapp.ui.page.device.u_product.fragment

import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.BindingAdapter.BindingViewHolder
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.URCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.ItemUlMeasureDataBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusEnum
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.LR200MeasureDataItem
import com.shmedo.mcloudapp.model.SensorConfigModule
import com.shmedo.mcloudapp.model.UIMeasureDataItem
import com.shmedo.mcloudapp.model.ULMeasureDataItem
import com.shmedo.mcloudapp.model.URMeasureDataItem
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceHomeFragment
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述：  一体式传感器配置主页 - 支持4G和蓝牙两种通讯方式
 */
class UProductHomeFragment : BaseDeviceHomeFragment() {

    // 根据产品类型使用不同的测量数据项
    private var uiMeasureDataItem: UIMeasureDataItem = UIMeasureDataItem()
    private var urMeasureDataItem: URMeasureDataItem = URMeasureDataItem()
    private var lr200MeasureDataItem: LR200MeasureDataItem = LR200MeasureDataItem()
    private var ulMeasureDataItem: ULMeasureDataItem = ULMeasureDataItem()

    override fun initData() {
        super.initData()
        when (productType) {
            ProductType.LR200 -> {//一体式裂缝计
                mHeadStates.productErrorResId.set(R.drawable.device_logo_bhy_3_lr200_error)
                mHeadStates.productAlarmResId.set(R.drawable.device_logo_bhy_3_lr200_alarm)
                mHeadStates.productOfflineResId.set(R.drawable.device_logo_bhy_3_lr200_offline)
                mHeadStates.productNormalResId.set(R.drawable.device_logo_bhy_3_lr200)
            }

            ProductType.U_I_1,//一体式倾斜仪
            ProductType.U_R_1 //一体式雨量计
                -> {
                mHeadStates.productErrorResId.set(R.drawable.device_logo_bhy_3s_error)
                mHeadStates.productAlarmResId.set(R.drawable.device_logo_bhy_3s_alarm)
                mHeadStates.productOfflineResId.set(R.drawable.device_logo_bhy_3s_offline)
                mHeadStates.productNormalResId.set(R.drawable.device_logo_bhy_3s)
            }

            ProductType.U_L_1 -> {//北斗林木生长监测终端
                mHeadStates.productErrorResId.set(R.drawable.device_logo_lr100_error)
                mHeadStates.productAlarmResId.set(R.drawable.device_logo_lr100_alarm)
                mHeadStates.productOfflineResId.set(R.drawable.device_logo_lr100_offline)
                mHeadStates.productNormalResId.set(R.drawable.device_logo_lr100)
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

    override fun BindingViewHolder.processOtherItemViewBind(itemViewType: Int) {
        if (itemViewType == R.layout.item_ul_measure_data) {
            val binding = getBinding<ItemUlMeasureDataBinding>()

            // 设置数据绑定参数
            binding.setVariable(BR.m, ulMeasureDataItem)
            binding.setVariable(BR.click, ClickProxy())
            binding.executePendingBindings()
        }
    }

    override fun initModuleData() {
        // 扩展适配器支持
        when (productType) {
            ProductType.LR200 //一体式裂缝计
                -> binding.rvModule.bindingAdapter.addType<LR200MeasureDataItem>(R.layout.item_lr200_measure_data)

            ProductType.U_I_1//一体式倾斜仪
                -> binding.rvModule.bindingAdapter.addType<UIMeasureDataItem>(R.layout.item_ui_measure_data)

            ProductType.U_R_1 //一体式雨量计
                -> binding.rvModule.bindingAdapter.addType<URMeasureDataItem>(R.layout.item_ur_measure_data)

            ProductType.U_L_1 //北斗林木生长监测终端
                -> binding.rvModule.bindingAdapter.addType<ULMeasureDataItem>(R.layout.item_ul_measure_data)

            else -> {}
        }


        val groupList = mutableListOf<Any>()

        // 根据产品类型添加对应的测量数据作为第一个项目
        when (productType) {
            ProductType.U_I_1 -> groupList.add(uiMeasureDataItem)
            ProductType.U_R_1 -> groupList.add(urMeasureDataItem)
            ProductType.LR200 -> groupList.add(lr200MeasureDataItem)
            ProductType.U_L_1 -> groupList.add(ulMeasureDataItem)
            else -> {}
        }

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
                        navId = when (productType) {
                            ProductType.LR200 -> R.id.action_global_to_lR200BaseInfoFragment //米度一体式裂缝计
                            ProductType.U_I_1,//倾斜仪
                            ProductType.U_R_1 -> R.id.action_global_to_uProductBaseInfoFragment //一体式雨量计
                            ProductType.U_L_1 -> R.id.action_global_to_uLBaseInfoFragment //北斗林木生长监测终端
                            else -> 0
                        }
                    ).toUnified(),

                    CommonModule(
                        name = if (productType == ProductType.U_L_1) "LORA信息" else "网络信息",
                        resID = R.drawable.ic_module_net_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = when (productType) {
                            ProductType.LR200 -> R.id.action_global_to_lR200NetInfoFragment //米度一体式裂缝计
                            ProductType.U_I_1,//倾斜仪
                            ProductType.U_R_1 -> R.id.action_global_to_uProductNetInfoFragment //一体式雨量计
                            ProductType.U_L_1 -> R.id.action_global_to_uLLORAInfoFragment //北斗林木生长监测终端
                            else -> 0
                        },
                        isSupport = productType != ProductType.U_L_1
                    ).toUnified(),

                    CommonModule(
                        name = "状态信息",
                        resID = R.drawable.ic_module_state_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = when (productType) {
                            ProductType.LR200 -> R.id.action_global_to_lR200StatusInfoFragment //米度一体式裂缝计
                            ProductType.U_I_1 -> R.id.action_global_to_uIStatusInfoFragment//倾斜仪
                            ProductType.U_R_1 -> R.id.action_global_to_uRStatusInfoFragment //一体式雨量计
                            ProductType.U_L_1 -> R.id.action_global_to_uLStatusInfoFragment //北斗林木生长监测终端
                            else -> 0
                        },
                        isSupport = productType != ProductType.U_L_1
                    ).toUnified(),

                    CommonModule(
                        name = "位置信息",
                        resID = R.drawable.ic_module_location_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_commonLocationInfoFragment,
                        isSupport = productType != ProductType.U_L_1
                    ).toUnified()
                )
            )
        )

        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        // 设备配置模块
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree()

        if (productType != ProductType.U_L_1) {
            configModuleTree.configModules.add(
                DataCenterModule(
                    name = "链路配置",
                    resID = R.drawable.ic_module_datacenter_new,
                    navId = R.id.action_global_to_dataCenterHomeFragment,
                ).toUnified()
            )
        }
        configModuleTree.configModules.add(
            CommonModule(
                name = "LORA配置",
                resID = R.drawable.ic_module_lora_new,
                navId = R.id.action_global_to_loraSettingFragment,
                isSupport = productType != ProductType.U_L_1
            ).toUnified()
        )

        configModuleTree.configModules.add(
            SensorConfigModule(
                name = "传感配置",
                resID = R.drawable.ic_module_sensor_setting_new,
                navId = 0,
                isSupport = productType != ProductType.U_L_1
            ).toUnified()
        )

        if (productType != ProductType.U_L_1) {
            configModuleTree.configModules.add(
                CommonModule(
                    name = "报警配置",
                    resID = R.drawable.ic_module_alarm_new,
                    navId = R.id.action_global_to_alarmSettingFragment
                ).toUnified()
            )
        }

        configModuleTree.configModules.add(
            CommonModule(
                name = "时间校准",
                resID = R.drawable.ic_module_time_calibration_new,
                navId = R.id.action_global_to_time_calibration,
            ).toUnified()
        )

        configModuleTree.configModules.add(
            CommonModule(
                name = "系统配置",
                resID = R.drawable.ic_module_system_setting,
                navId = R.id.action_global_to_advancedSettingFragment,
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
                val centerNum = when (productType) {
                    ProductType.U_I_1,//倾斜仪
                    ProductType.U_R_1 //一体式雨量计
                        -> 3

                    ProductType.LR200 -> 4//米度一体式裂缝计
                    else -> 3
                }
                nav().safeNavigate(
                    configModule.navId,
                    BaseDataCenterHomeFragment.newBundleArguments(
                        centerNum,
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                )
            }

            is SensorConfigModule -> {
                val navId = when (productType) {
                    ProductType.U_I_1 -> R.id.action_global_to_uISensorParamFragment//倾斜仪
                    ProductType.U_R_1 -> R.id.action_global_to_uRSensorParamFragment//一体式雨量计
                    ProductType.LR200 -> R.id.action_global_to_lR200SensorParamFragment//米度一体式裂缝计
                    ProductType.U_L_1 -> R.id.action_global_to_uLSensorParamFragment//北斗林木生长监测终端
                    else -> 0
                }
                nav().safeNavigate(
                    navId, BaseIOTDeviceFragment.newBundleArguments(
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
        val commands = mutableListOf<String>()

        if (productType != ProductType.U_L_1)
            commands.add(IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS))
        else
            commands.add(IOTCommandUtil.getCommand(IOTCommandType.SAMPLE))

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false,
                errorConfig = ErrorConfig.silentConfig(), // 状态查询失败不显示错误
                enableBusinessParseFailureInterrupt = false // 不启用业务层解析失败中断功能，会续指令执行
            )
        )
    }


    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
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
                        when (productType) {
                            ProductType.U_I_1 -> initUIStatusInfo(result.data)
                            ProductType.U_R_1 -> initURStatusInfo(result.data)
                            ProductType.LR200 -> initLR200StatusInfo(result.data)
                            else -> {}
                        }
                    }
                }
            }

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


            else -> {
                // 其他指令交给父类处理
                super.handleCommandResponse(cmdStr)
            }
        }
    }

    private fun initUIStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<URCurrentStateInfo>(content)
                }
                if (stateInfo == null) {
                    return@launchWithViewLifecycle
                }
                val uISensorInfoList = stateInfo.attach_data
                if (uISensorInfoList.isNullOrEmpty()) {
                    return@launchWithViewLifecycle
                }

                mHeadStates.deviceStatusCode.set(DeviceStatusEnum.UNKNOWN.code)

                // 初始化数据
                var xInitialAngle = AppContants.PLACE_HOLDER_VALUE
                var yInitialAngle = AppContants.PLACE_HOLDER_VALUE
                var zInitialAngle = AppContants.PLACE_HOLDER_VALUE
                var xAngle = AppContants.PLACE_HOLDER_VALUE
                var yAngle = AppContants.PLACE_HOLDER_VALUE
                var zAngle = AppContants.PLACE_HOLDER_VALUE
                var xAcc = AppContants.PLACE_HOLDER_VALUE
                var yAcc = AppContants.PLACE_HOLDER_VALUE
                var zAcc = AppContants.PLACE_HOLDER_VALUE

                uISensorInfoList.forEach { info ->
                    when (info.key) {
                        "initAngle" -> {
                            info.value.notNullKey { value ->
                                //根据逗号分隔
                                val initAngle =
                                    value.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                if (initAngle.isNotEmpty()) {
                                    xInitialAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                        initAngle[0],
                                        AppContants.PLACE_HOLDER_VALUE,
                                        2
                                    ) + "°"
                                }
                                if (initAngle.size > 1) {
                                    yInitialAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                        initAngle[1],
                                        AppContants.PLACE_HOLDER_VALUE,
                                        2
                                    ) + "°"
                                }
                                if (initAngle.size > 2) {
                                    zInitialAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                        initAngle[2],
                                        AppContants.PLACE_HOLDER_VALUE,
                                        2
                                    ) + "°"
                                }
                            }
                        }

                        "angle" -> {
                            info.value.notNullKey { value ->
                                //根据逗号分隔
                                val angle =
                                    value.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                if (angle.isNotEmpty()) {
                                    xAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                        angle[0],
                                        AppContants.PLACE_HOLDER_VALUE,
                                        2
                                    ) + "°"
                                }
                                if (angle.size > 1) {
                                    yAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                        angle[1],
                                        AppContants.PLACE_HOLDER_VALUE,
                                        2
                                    ) + "°"
                                }
                                if (angle.size > 2) {
                                    zAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                        angle[2],
                                        AppContants.PLACE_HOLDER_VALUE,
                                        2
                                    ) + "°"
                                }
                            }
                        }

                        "acc" -> {
                            info.value.notNullKey { value ->
                                //根据逗号分隔
                                val acc = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                if (acc.isNotEmpty()) {
                                    xAcc = DeviceStatusInfoProcessor.formatDoubleValue(
                                        acc[0],
                                        AppContants.PLACE_HOLDER_VALUE,
                                        2
                                    ) + "°"
                                }
                                if (acc.size > 1) {
                                    yAcc = DeviceStatusInfoProcessor.formatDoubleValue(
                                        acc[1],
                                        AppContants.PLACE_HOLDER_VALUE,
                                        2
                                    ) + "°"
                                }
                                if (acc.size > 2) {
                                    zAcc = DeviceStatusInfoProcessor.formatDoubleValue(
                                        acc[2],
                                        AppContants.PLACE_HOLDER_VALUE,
                                        2
                                    ) + "°"
                                }
                            }
                        }
                    }
                }

                // 更新测量数据项
                binding.rvModule.bindingAdapter.getModel<UIMeasureDataItem>(0)
                    .refreshStatus(
                        xInitialAngle,
                        yInitialAngle,
                        zInitialAngle,
                        xAngle,
                        yAngle,
                        zAngle,
                        xAcc,
                        yAcc,
                        zAcc
                    )

            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initURStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<URCurrentStateInfo>(content)
                }
                if (stateInfo == null) {
                    return@launchWithViewLifecycle
                }
                val uRSensorInfoList = stateInfo.attach_data
                if (uRSensorInfoList.isNullOrEmpty()) {
                    return@launchWithViewLifecycle
                }

                mHeadStates.deviceStatusCode.set(DeviceStatusEnum.UNKNOWN.code)

                var rain24h = "--mm"

                uRSensorInfoList.forEach { info ->
                    when (info.key) {
                        "dayRain" -> {//24小时雨量值
                            rain24h = DeviceStatusInfoProcessor.formatDoubleValue(
                                info.value,
                                AppContants.PLACE_HOLDER_VALUE,
                                2
                            ) + "mm"
                        }
                    }
                }

                // 更新测量数据项
                binding.rvModule.bindingAdapter.getModel<URMeasureDataItem>(0)
                    .refreshStatus(rain24h)

            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initLR200StatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle

                val deviceAbnormalList =
                    if (stateInfo.self_check.isEmpty()) arrayListOf<String>() else DeviceStatusHelper.checkDeviceAbnormal(
                        stateInfo.self_check
                    )
                val status = if (deviceAbnormalList.isEmpty()) "正常" else "故障"
                mHeadStates.productLogoResId.set(if (deviceAbnormalList.isEmpty()) mHeadStates.productNormalResId.get() else mHeadStates.productErrorResId.get())
                mHeadStates.deviceStatusCode.set(if (deviceAbnormalList.isEmpty()) "0" else "-3")
                mHeadStates.warnErrorText.set(status)

                // 更新测量数据
                val newXAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                    stateInfo.x_Angle,
                    AppContants.PLACE_HOLDER_VALUE,
                    2
                ) + "°"

                val newYAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                    stateInfo.y_Angle,
                    AppContants.PLACE_HOLDER_VALUE,
                    2
                ) + "°"

                val newZAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                    stateInfo.z_Angle,
                    AppContants.PLACE_HOLDER_VALUE,
                    2
                ) + "°"

                val newLFInitial = DeviceStatusInfoProcessor.formatDoubleValue(
                    stateInfo.lF_initial,
                    AppContants.PLACE_HOLDER_VALUE,
                    2
                ) + "mm"

                val newLFCurrent = DeviceStatusInfoProcessor.formatDoubleValue(
                    stateInfo.lF_current,
                    AppContants.PLACE_HOLDER_VALUE,
                    2
                ) + "mm"

                val newLFCumulative = DeviceStatusInfoProcessor.formatDoubleValue(
                    stateInfo.lF_Cumulative,
                    AppContants.PLACE_HOLDER_VALUE,
                    2
                ) + "mm"

                // 刷新测量数据项
                binding.rvModule.bindingAdapter.getModel<LR200MeasureDataItem>(0)
                    .refreshStatus(
                        newXAngle,
                        newYAngle,
                        newZAngle,
                        newLFInitial,
                        newLFCurrent,
                        newLFCumulative
                    )

            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 处理召测响应
     */
    private fun processSampleResponse(cmdStr: String, content: String) {
        try {
            // $cmd=sample&datastreams={"203_1":"0.000","103_1":"0.000,0.000,0.000,1.000,1.000,90.000"}
            val resultMap = MoshiUtil.fromJson<Map<String, Any>>(content) ?: return

            // 初始化数据
            var newLFInitial = AppContants.PLACE_HOLDER_VALUE
            var newLFCurrent = AppContants.PLACE_HOLDER_VALUE
            var newLFCumulative = AppContants.PLACE_HOLDER_VALUE
            var xAngle = AppContants.PLACE_HOLDER_VALUE
            var yAngle = AppContants.PLACE_HOLDER_VALUE
            var zAngle = AppContants.PLACE_HOLDER_VALUE

            // 处理位移数据 (203_1)
            if (resultMap.containsKey("203_1")) {
                val displacement = resultMap["203_1"]?.toString() ?: return
                displacement.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .let {
                        if (it.isNotEmpty()) {
                            newLFCurrent = DeviceStatusInfoProcessor.formatDoubleValue(
                                it[0],
                                AppContants.PLACE_HOLDER_VALUE,
                                2
                            ) + "mm"

                            ulMeasureDataItem.refreshDisplacementStatus(
                                newLFInitial,
                                newLFCurrent,
                                newLFCumulative
                            )
                        }
                    }
            }

            // 处理角度数据 (103_1)
            if (resultMap.containsKey("103_1")) {
                val currentAngle = resultMap["103_1"]?.toString() ?: return
                currentAngle.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .let { angle ->
                        if (angle.isNotEmpty()) {
                            xAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                angle[0],
                                AppContants.PLACE_HOLDER_VALUE,
                                3
                            ) + "°"
                        }
                        if (angle.size > 1) {
                            yAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                angle[1],
                                AppContants.PLACE_HOLDER_VALUE,
                                3
                            ) + "°"
                        }
                        if (angle.size > 2) {
                            zAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                angle[2],
                                AppContants.PLACE_HOLDER_VALUE,
                                3
                            ) + "°"
                        }
                        ulMeasureDataItem.refreshAngleStatus(xAngle, yAngle, zAngle)
                    }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onSampleDataClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }

            val command = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE)
            sendCommandSequence(
                commands = listOf(command),
                config = CommandSequenceConfig(
                    loadingMessage = StringUtils.getString(R.string.processing),
                    errorConfig = ErrorConfig.dialogConfig()
                )
            )
        }
    }
}