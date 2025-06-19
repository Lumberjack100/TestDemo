package com.shmedo.mcloudapp.ui.page.device.u_product.fragment

import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.URCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusEnum
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.LR200MeasureDataItem
import com.shmedo.mcloudapp.model.SensorConfigModule
import com.shmedo.mcloudapp.model.UIMeasureDataItem
import com.shmedo.mcloudapp.model.URMeasureDataItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.NewUniversalBaseDeviceHomeFragment
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
class UProductCommonHomeFragment : NewUniversalBaseDeviceHomeFragment() {

    // 根据产品类型使用不同的测量数据项
    private var uiMeasureDataItem: UIMeasureDataItem = UIMeasureDataItem()
    private var urMeasureDataItem: URMeasureDataItem = URMeasureDataItem()
    private var lr200MeasureDataItem: LR200MeasureDataItem = LR200MeasureDataItem()

    override fun initData() {
        super.initData()
        when (productType) {
            ProductType.U_I_1,//一体化倾斜仪
            ProductType.U_R_1 //一体化雨量计
                -> {
                mHeadStates.productErrorResId.set(R.drawable.device_logo_bhy_3s_error)
                mHeadStates.productAlarmResId.set(R.drawable.device_logo_bhy_3s_alarm)
                mHeadStates.productOfflineResId.set(R.drawable.device_logo_bhy_3s_offline)
                mHeadStates.productNormalResId.set(R.drawable.device_logo_bhy_3s)
            }

            ProductType.LR200 -> {//一体式裂缝计
                mHeadStates.productErrorResId.set(R.drawable.device_logo_bhy_3_lr200_error)
                mHeadStates.productAlarmResId.set(R.drawable.device_logo_bhy_3_lr200_alarm)
                mHeadStates.productOfflineResId.set(R.drawable.device_logo_bhy_3_lr200_offline)
                mHeadStates.productNormalResId.set(R.drawable.device_logo_bhy_3_lr200)
            }

            else -> {
                mHeadStates.productErrorResId.set(R.drawable.device_logo_default_error)
                mHeadStates.productAlarmResId.set(R.drawable.device_logo_default_alarm)
                mHeadStates.productOfflineResId.set(R.drawable.device_logo_default_offline)
                mHeadStates.productNormalResId.set(R.drawable.device_logo_default)
            }
        }
    }

    override fun initModuleData() {
        // 扩展适配器支持
        when (productType) {
            ProductType.U_I_1//一体化倾斜仪
                -> binding.rvModule.bindingAdapter.addType<UIMeasureDataItem>(R.layout.item_ui_measure_data)

            ProductType.U_R_1 //一体化雨量计
                -> binding.rvModule.bindingAdapter.addType<URMeasureDataItem>(R.layout.item_ur_measure_data)

            ProductType.LR200 //一体化裂缝计
                -> binding.rvModule.bindingAdapter.addType<LR200MeasureDataItem>(R.layout.item_lr200_measure_data)

            else -> {}
        }

        val groupList = mutableListOf<Any>()

        // 根据产品类型添加对应的测量数据作为第一个项目
        when (productType) {
            ProductType.U_I_1 -> groupList.add(uiMeasureDataItem)
            ProductType.U_R_1 -> groupList.add(urMeasureDataItem)
            ProductType.LR200 -> groupList.add(lr200MeasureDataItem)
            else -> {}
        }

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
                            navId = when (productType) {
                                ProductType.LR200 -> R.id.action_global_to_lR200BaseInfoFragment //米度一体式裂缝计
                                ProductType.U_I_1,//倾斜仪
                                ProductType.U_R_1 -> R.id.action_global_to_uProductBaseInfoFragment //一体化雨量计
                                else -> 0
                            }
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "网络信息",
                            resID = R.drawable.ic_module_net_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = when (productType) {
                                ProductType.LR200 -> R.id.action_global_to_lR200NetInfoFragment //米度一体式裂缝计
                                ProductType.U_I_1,//倾斜仪
                                ProductType.U_R_1 -> R.id.action_global_to_uProductNetInfoFragment //一体化雨量计
                                else -> 0
                            }
                        )
                    ),
                    ConfigModule(
                        CommonModule(
                            name = "状态信息",
                            resID = R.drawable.ic_module_state_info,
                            iconSize = ConvertUtils.dp2px(34f),
                            navId = when (productType) {
                                ProductType.LR200 -> R.id.action_global_to_lR200StatusInfoFragment //米度一体式裂缝计
                                ProductType.U_I_1 -> R.id.action_global_to_uProductStatusInfoFragment//倾斜仪
                                ProductType.U_R_1 -> R.id.action_global_to_uRProductStatusInfoFragment //一体化雨量计
                                else -> 0
                            }
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
                    DataCenterModule(
                        name = "链路配置",
                        resID = R.drawable.ic_module_datacenter_new,
                        navId = R.id.action_global_to_universalDataCenterHomeFragment
                    )
                ),

                ConfigModule(
                    CommonModule(
                        name = "LORA配置",
                        resID = R.drawable.ic_module_lora_new,
                        navId = R.id.action_global_to_loraSettingFragment
                    )
                ),
                ConfigModule(
                    CommonModule(
                        name = "传感配置",
                        resID = R.drawable.ic_module_sensor_setting_new,
                        navId = 0
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
                val centerNum = when (productType) {
                    ProductType.U_I_1,//倾斜仪
                    ProductType.U_R_1 //一体化雨量计
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
                var navId = when (productType) {
                    ProductType.U_I_1 -> R.id.action_global_to_uIProductSensorParamFragment//倾斜仪
                    ProductType.U_R_1 -> R.id.action_global_to_uRProductSensorParamFragment//一体化雨量计
                    ProductType.LR200 -> R.id.action_global_to_lR200SensorParamFragment//米度一体式裂缝计
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
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
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
                        when (productType) {
                            ProductType.U_I_1 -> initUIStatusInfo(result.data)
                            ProductType.U_R_1 -> initURStatusInfo(result.data)
                            ProductType.LR200 -> initLR200StatusInfo(result.data)
                            else -> {}
                        }
                    }
                }
            }

            else -> {

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
                var xInitialAngle = "--°"
                var yInitialAngle = "--°"
                var zInitialAngle = "--°"
                var xAngle = "--°"
                var yAngle = "--°"
                var zAngle = "--°"
                var xAcc = "--°"
                var yAcc = "--°"
                var zAcc = "--°"

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
                                        "--",
                                        2
                                    ) + "°"
                                }
                                if (initAngle.size > 1) {
                                    yInitialAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                        initAngle[1],
                                        "--",
                                        2
                                    ) + "°"
                                }
                                if (initAngle.size > 2) {
                                    zInitialAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                        initAngle[2],
                                        "--",
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
                                        "--",
                                        2
                                    ) + "°"
                                }
                                if (angle.size > 1) {
                                    yAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                        angle[1],
                                        "--",
                                        2
                                    ) + "°"
                                }
                                if (angle.size > 2) {
                                    zAngle = DeviceStatusInfoProcessor.formatDoubleValue(
                                        angle[2],
                                        "--",
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
                                        "--",
                                        2
                                    ) + "°"
                                }
                                if (acc.size > 1) {
                                    yAcc = DeviceStatusInfoProcessor.formatDoubleValue(
                                        acc[1],
                                        "--",
                                        2
                                    ) + "°"
                                }
                                if (acc.size > 2) {
                                    zAcc = DeviceStatusInfoProcessor.formatDoubleValue(
                                        acc[2],
                                        "--",
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
                                "--",
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

                val newLFInitial = DeviceStatusInfoProcessor.formatDoubleValue(
                    stateInfo.lF_initial,
                    "--",
                    2
                ) + "mm"

                val newLFCurrent = DeviceStatusInfoProcessor.formatDoubleValue(
                    stateInfo.lF_current,
                    "--",
                    2
                ) + "mm"

                val newLFCumulative = DeviceStatusInfoProcessor.formatDoubleValue(
                    stateInfo.lF_Cumulative,
                    "--",
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
}