package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.NewDataCenterPlatform
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.UDCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterParamFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/8/29
 * 描述： 一体式雷达水位/泥位计网络信息
 */
class UDNetInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "网络信息"
    }

    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "method=1")
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCurrentStateInfo>(content as String)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showError()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("数据网络"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "移动网络",
                    value = stateInfo.mobileNet.compareAndReturn("1", "已连接", "未连接"),
                    textColorRes = if (stateInfo.mobileNet == "1") ColorUtils.getColor(
                        R.color.online_colorPrimary
                    ) else 0
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "网络类型",
                    value = stateInfo.netType,
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "运营商",
                    value = when (stateInfo.operator.uppercase()) {
                        "CMCC" -> "中国移动"
                        "CU" -> "中国联通"
                        "CT" -> "中国电信"
                        else -> AppContants.Companion.PLACE_HOLDER_VALUE
                    }
                )
                stateInfo.csq.notNullKey {
                    var temp = it.toIntOrNull() ?: 0
                    if (temp !in -110..-50) {
                        temp = 0
                    }
                    groupList.add(
                        DeviceStatusInfoSignalItem(
                            name = "信号强度",
                            signalValue = temp,
                            textColorRes = if (temp == 0) ColorUtils.getColor(
                                R.color.error_FF4400
                            ) else 0
                        )
                    )
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "IMEI",
                    value = stateInfo.imei,
                    isClipboard = true
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "ICCID",
                    value = stateInfo.iccid,
                    isClipboard = true
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电台",
                    value = stateInfo.radioEnableStatus.compareAndReturn("1", "已启用", "未启用"),
                    textColorRes = if (stateInfo.radioEnableStatus == "1") ColorUtils.getColor(
                        R.color.online_colorPrimary
                    ) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "蓝牙",
                    value = stateInfo.bt_connected.compareAndReturn("1", "已连接", "未连接"),
                    textColorRes = if (stateInfo.bt_connected == "1") ColorUtils.getColor(
                        R.color.online_colorPrimary
                    ) else 0,
                    isBottomItem = true
                )

                if (stateInfo.dataCenterEnableStatus != IOTConstants.NULL_KEY
                    && stateInfo.dataCenterLinkStatus != IOTConstants.NULL_KEY
                    && stateInfo.dataCenterPlatformType != IOTConstants.NULL_KEY
                    && stateInfo.dataCenterEnableStatus.isNotEmpty()
                    && stateInfo.dataCenterLinkStatus.isNotEmpty()
                    && stateInfo.dataCenterPlatformType.isNotEmpty()
                ) {
                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                    groupList.add(DeviceStatusInfoGroupItem("数据链路"))

                    //根据逗号分隔
                    val enableStatusList = stateInfo.dataCenterEnableStatus.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }
                    val onlineStatusList = stateInfo.dataCenterLinkStatus.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }
                    val platformTypeList = stateInfo.dataCenterPlatformType.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }

                    enableStatusList.forEachIndexed { index, enableStatus ->
                        val platformIndex = platformTypeList.getOrNull(index)?.toIntOrNull() ?: 0
                        val platformType =
                            NewDataCenterPlatform.valueByPlatType(platformIndex.toString())
                                .getPlatFormName()
                        val onlineStatus = onlineStatusList.getOrNull(index)
                            ?.compareAndReturn("1", "已连接", "未连接") ?: "未连接"

                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "数据链路${index + 1}",
                            value = enableStatus.compareAndReturn(
                                "1",
                                onlineStatus,
                                "未启用"
                            ),
                            textColorRes = if (enableStatus == "0" || onlineStatus == "未连接") 0 else ColorUtils.getColor(
                                R.color.online_colorPrimary
                            ),
                            isClickable = true,
                            isBottomItem = index == onlineStatusList.size - 1
                        )
                    }
                }

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    override fun processItemClick(infoBasicItem: DeviceStatusInfoBasicItem) {
        if (infoBasicItem.name.startsWith("数据链路")) {
            val index = infoBasicItem.name.substringAfter("数据链路").toIntOrNull() ?: 0
            val item = DataCenterStatusItem(
                centerid = index,
                name = "数据链路$index",
                status = when {
                    infoBasicItem.value.contains("未启用") -> "0"
                    infoBasicItem.value.contains("已连接") -> "1"
                    infoBasicItem.value.contains("未连接") -> "2"
                    else -> "0"
                }
            )

            val bundle = UniversalDataCenterParamFragment.newBundleArguments(
                item,
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(
                R.id.action_global_to_dataCenterParamFragment,
                bundle
            )
        }
    }

}