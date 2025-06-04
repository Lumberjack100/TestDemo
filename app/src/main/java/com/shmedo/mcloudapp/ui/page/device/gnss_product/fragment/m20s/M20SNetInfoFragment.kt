package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m20s

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
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
 * 创建时间：2024/9/19
 * 描述： 网络信息
 */
class M20SNetInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toolbarViewModel.toolbarTitleText.set("网络信息")
    }

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content)
                }
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
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "IMEI",
                    value = stateInfo.iMEI,
                    isClipboard = true
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "ICCID",
                    value = stateInfo.cCID,
                    isClipboard = true,
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("数据链路"))
                val status1 = stateInfo.dataCenter1.compareAndReturn(
                    0, "未启用", stateInfo.dataCenter1.compareAndReturn(1, "已连接", "未连接")
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "数据链路1",
                    value = status1,
                    textColorRes = if (stateInfo.dataCenter1 == 0 || status1 == "未连接") 0 else ColorUtils.getColor(
                        R.color.online_colorPrimary
                    )
                )

                val status2 = stateInfo.dataCenter2.compareAndReturn(
                    0, "未启用", stateInfo.dataCenter2.compareAndReturn(1, "已连接", "未连接")
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "数据链路2",
                    value = status2,
                    textColorRes = if (stateInfo.dataCenter2 == 0 || status2 == "未连接") 0 else ColorUtils.getColor(
                        R.color.online_colorPrimary
                    )
                )

                val status3 = stateInfo.dataCenter3.compareAndReturn(
                    0, "未启用", stateInfo.dataCenter3.compareAndReturn(1, "已连接", "未连接")
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "数据链路3",
                    value = "$status3(米度物联平台)",
                    textColorRes = if (stateInfo.dataCenter3 == 0 || status3 == "未连接") 0 else ColorUtils.getColor(
                        R.color.online_colorPrimary
                    )
                )

                val status4 = stateInfo.dataCenter4.compareAndReturn(
                    0, "未启用", stateInfo.dataCenter4.compareAndReturn(1, "已连接", "未连接")
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "数据链路4",
                    value = status4,
                    textColorRes = if (stateInfo.dataCenter4 == 0 || status4 == "未连接") 0 else ColorUtils.getColor(
                        R.color.online_colorPrimary
                    ),
                    isBottomItem = true
                )

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

}