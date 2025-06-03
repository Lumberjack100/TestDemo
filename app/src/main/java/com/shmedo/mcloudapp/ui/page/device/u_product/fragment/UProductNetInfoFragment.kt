package com.shmedo.mcloudapp.ui.page.device.u_product.fragment

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/5/30
 * 描述： TODO
 */
class UProductNetInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toolbarViewModel.toolbarTitleText.set("网络信息")
    }

    override fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val commonCurrentStateInfoList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<CommonCurrentStateInfo2>>(content)
                }
                if (commonCurrentStateInfoList.isNullOrEmpty()) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }

                val stateInfo = commonCurrentStateInfoList[0]
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("数据网络"))
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
                stateInfo.signal.notNullKey {
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
                    value = stateInfo.ccid,
                    isClipboard = true,
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("数据链路"))
                if (stateInfo.dataCenterUseSta != IOTConstants.NULL_KEY
                    && stateInfo.dataCenterStatus != IOTConstants.NULL_KEY
                    && stateInfo.dataCenterUseSta.isNotEmpty()
                    && stateInfo.dataCenterStatus.isNotEmpty()
                ) {
                    //根据逗号分隔
                    val enableStatusList = stateInfo.dataCenterUseSta.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }

                    val onlineStatusList = stateInfo.dataCenterStatus.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }

                    enableStatusList.forEachIndexed { index, status ->
                        val statusText = status.compareAndReturn(
                            "0",
                            "未启用",
                            onlineStatusList[index].compareAndReturn("0", "未连接", "已连接")
                        )

                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "数据链路${index + 1}",
                            value = statusText,
                            textColorRes = if (status == "0" || statusText == "未连接") 0 else ColorUtils.getColor(
                                R.color.online_colorPrimary
                            ),
                            isBottomItem = true
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
}