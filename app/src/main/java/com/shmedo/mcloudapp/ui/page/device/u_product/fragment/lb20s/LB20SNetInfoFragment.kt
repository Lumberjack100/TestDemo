package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.lb20s

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.model.lb20s.LB20SCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
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
 * 创建时间：2024/9/19
 * 描述： 无线预警广播(LB20S)网络信息
 */
class LB20SNetInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "网络信息"
    }

    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                val dataMap = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<Map<String, LB20SCurrentStateInfo>>(content as String)
                }
                if (dataMap.isNullOrEmpty()) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                val stateInfo = dataMap["000_1"]
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("数据网络"))
                stateInfo._4g_signal.notNullKey {
                    var temp = it
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
                stateInfo.attach_data?.get("imei")?.let {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "IMEI",
                            value = it,
                            isClipboard = true
                        )
                    )
                }
                stateInfo.attach_data?.get("iccid")?.let {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "ICCID",
                            value = it,
                            isClipboard = true,
                            isBottomItem = true
                        )
                    )
                }

                if (stateInfo.datacenterStatus != IOTConstants.NULL_KEY) {
                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                    groupList.add(DeviceStatusInfoGroupItem("数据链路"))
                    //根据逗号分隔
                    val statusList = stateInfo.datacenterStatus.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }

                    statusList.forEachIndexed { index, status ->
                        val statusText = status.compareAndReturn(
                            "1",
                            "已连接",
                            status.compareAndReturn("2", "未连接", "未启用")
                        )

                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "数据链路${index + 1}",
                            value = statusText,
                            textColorRes = if (status == "0" || statusText == "未连接") 0 else ColorUtils.getColor(
                                R.color.online_colorPrimary
                            ),
                            isBottomItem = index == statusList.size - 1
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