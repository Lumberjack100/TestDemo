package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.lb20s

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.model.lb20s.LB20SCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/9/19
 * 描述： 无线预警广播(LB20S)基本信息
 */
class LB20SBaseInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "基本信息"
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

                groupList.add(DeviceStatusInfoGroupItem("设备信息"))
                stateInfo.attach_data?.get("SN")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备SN", value = it))
                }
                stateInfo.sw_version.notNullKey {
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "固件版本",
                        value = stateInfo.sw_version,
                        isBottomItem = true
                    )
                }

                stateInfo.volumelevel.notNullKey { volumeLevelStr ->
                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                    groupList.add(DeviceStatusInfoGroupItem("工作信息"))
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "音量等级",
                        value = if (volumeLevelStr == "0") "无" else if (volumeLevelStr == "1") "低" else if (volumeLevelStr == "2") "中" else if (volumeLevelStr == "3") "高" else "无",
                        isBottomItem = true
                    )
                }

                stateInfo.attach_data?.get("volumelevel")?.let { volumeLevelStr ->
                    groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                    groupList.add(DeviceStatusInfoGroupItem("工作信息"))
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "音量等级",
                            value = if (volumeLevelStr == "0") "无" else if (volumeLevelStr == "1") "低" else if (volumeLevelStr == "2") "中" else if (volumeLevelStr == "3") "高" else "无",
                            isBottomItem = true
                        )
                    )
                }

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

}