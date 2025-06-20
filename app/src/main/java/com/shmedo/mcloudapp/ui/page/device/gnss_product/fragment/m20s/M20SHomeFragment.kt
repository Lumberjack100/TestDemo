package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m20s

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showDialogFragment
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
import com.shmedo.mcloudapp.ui.page.device.common.NewUniversalBaseDeviceHomeFragment
import com.shmedo.mcloudapp.ui.page.device.u_product.dialog.FindDeviceBeepDialog
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： 普适型 GNSS 接收机(M20S)配置主页 - 支持4G和蓝牙两种通讯方式
 */
class M20SHomeFragment : NewUniversalBaseDeviceHomeFragment() {

    private var measureDataItem: M20SMeasureDataItem = M20SMeasureDataItem()

    override fun initData() {
        super.initData()
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
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree()
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

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        updateLastCommunicationTime()

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

            IOTCommandType.MD_SEARCH_DEVICE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设备查找出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            showDialogFragment(FindDeviceBeepDialog.Companion.TAG) {
                                FindDeviceBeepDialog.Companion.newInstance(ProductType.GNSS_M_5)
                            }
                        }
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

                // 刷新测量数据项
                binding.rvModule.bindingAdapter.getModel<M20SMeasureDataItem>(0)
                    .refreshStatus(newXAngle, newYAngle, newZAngle)

            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }
}