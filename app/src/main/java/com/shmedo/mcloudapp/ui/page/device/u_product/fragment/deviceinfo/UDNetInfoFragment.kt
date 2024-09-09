package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.UDCommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/8/29
 * 描述： 一体化雷达泥位计网络信息
 */
class UDNetInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {
    private val platformList by lazy { Utils.getApp().resources.getStringArray(R.array.data_center_register_platform) }

    override fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "value=1")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCommonCurrentStateInfo>(content)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("数据网络", bgColorRes = ColorUtils.getColor(R.color.main_bg_gray)))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "移动网络",
                    value = stateInfo.mobileNet.compareAndReturn("1", "开启", "关闭"),
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "网络类型",
                    value = stateInfo.netType,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "运营商",
                    value = when (stateInfo.operator) {
                        "CMCC" -> "中国移动"
                        "CU" -> "中国联通"
                        "CT" -> "中国电信"
                        else -> "--"
                    }
                )
                stateInfo.csq.notNullKey {
                    val temp = it.toIntOrNull() ?: 0
                    groupList.add(
                        DeviceStatusInfoSignalItem(
                            name = "信号强度",
                            signalValue = if (temp <= 0)
                                temp
                            else
                                temp * 2 - 113
                        )
                    )
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "IMEI",
                    value = stateInfo.imei,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "ICCID",
                    value = stateInfo.iccid,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电台",
                    value = stateInfo.radioEnableStatus.compareAndReturn("1", "已启用", "未启用"),
                    textColorRes = if (stateInfo.radioEnableStatus == "1") ColorUtils.getColor(
                        R.color.green_00B26B
                    ) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "蓝牙",
                    value = stateInfo.bt_connected.compareAndReturn("1", "已连接", "未连接"),
                    textColorRes = if (stateInfo.radioEnableStatus == "1") ColorUtils.getColor(
                        R.color.green_00B26B
                    ) else 0
                )
                groupList.add(DeviceStatusInfoGroupItem("数据链路", bgColorRes = ColorUtils.getColor(R.color.main_bg_gray)))
                if (stateInfo.dataCenterEnableStatus != IOTConstants.NULL_KEY
                    && stateInfo.dataCenterLinkStatus != IOTConstants.NULL_KEY
                    && stateInfo.dataCenterPlatformType != IOTConstants.NULL_KEY
                    && stateInfo.dataCenterEnableStatus.isNotEmpty()
                    && stateInfo.dataCenterLinkStatus.isNotEmpty()
                    && stateInfo.dataCenterPlatformType.isNotEmpty()
                ) {
                    //根据逗号分隔
                    val enableStatusList = stateInfo.dataCenterEnableStatus.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }
                    val onlineStatusList = stateInfo.dataCenterLinkStatus.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }
                    val platformTypeList = stateInfo.dataCenterPlatformType.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }

                    enableStatusList.forEachIndexed { index, enableStatus ->
                        val platformIndex = platformTypeList.getOrNull(index)?.toIntOrNull() ?: 0
                        val platformType = platformList.getOrNull(platformIndex) ?: "未知"
                        val onlineStatus = onlineStatusList.getOrNull(index)
                            ?.compareAndReturn("1", "已连接", "未连接") ?: "未连接"

                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "数据链路${index + 1}",
                            value = enableStatus.compareAndReturn(
                                "1",
                                "$onlineStatus($platformType)",
                                "未启用"
                            ),
                            textColorRes = if (enableStatus == "0" || onlineStatus == "未连接") ColorUtils.getColor(
                                R.color.red_F13838
                            ) else ColorUtils.getColor(R.color.green_00B26B)
                        )
                    }
                }

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = UDNetInfoFragment()
    }
}