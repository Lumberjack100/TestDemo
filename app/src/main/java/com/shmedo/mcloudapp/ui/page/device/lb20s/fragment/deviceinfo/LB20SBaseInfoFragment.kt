package com.shmedo.mcloudapp.ui.page.device.lb20s.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.SensorErrorType
import com.shmedo.lib.cmd.base.iot_cmd.model.lb20s.LB20SCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/5/12
 * @desc: 预警广播
 *
 */
class LB20SBaseInfoFragment : BaseDeviceStatusInfoFragment() {

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val dataMap = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<Map<String, LB20SCurrentStateInfo>>(content)
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
                stateInfo.attach_data?.get("SN")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备SN", value = it))
                }
                stateInfo.attach_data?.get("imei")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备IMEI", value = it))
                }
                stateInfo.attach_data?.get("imsi")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备IMSI", value = it))
                }
                stateInfo.attach_data?.get("iccid")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备ICCID", value = it))
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "硬件版本",
                    value = stateInfo.sw_version,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备位置",
                    value = stateInfo.location,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "外部供电电压",
                    value = stateInfo.ext_power_volt,
                    defaultValue = "0",
                    downLimitValue = 5.0,
                    digit = 2,
                    unit = "V",
                )
                stateInfo._4g_signal.notNullKey {
                    groupList.add(
                        DeviceStatusInfoSignalItem(
                            name = "4G信号强度",
                            signalValue = if (it <= 0)
                                it
                            else
                                it * 2 - 113)
                    )
                }
                stateInfo.attach_data?.get("volumelevel")?.let { volumeLevelStr ->
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "音量等级",
                            value = if (volumeLevelStr == "0") "无" else if (volumeLevelStr == "1") "低" else if (volumeLevelStr == "2") "中" else if (volumeLevelStr == "3") "高" else "无"
                        )
                    )
                }
                groupList.add(DeviceStatusInfoGroupItem("太阳能控制器"))
                stateInfo.sensor_errno?.let {
                    val item = it[0]
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "状态",
                            value = if (item.errno.toString() == "0") "正常" else SensorErrorType.getErrorMessageByCode(
                                item.errno.toString()
                            ),
                            textColorRes = if (item.errno.toString() == "0") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                                R.color.red_F13838
                            )
                        )
                    )
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "太阳能板电压",
                    value = stateInfo.solar_volt,
                    defaultValue = "0",
                    downLimitValue = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "蓄电池电压",
                    value = stateInfo.battery_volt,
                    defaultValue = "0",
                    downLimitValue = 5.0,
                    digit = 2,
                    unit = "V",
                )
                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = LB20SBaseInfoFragment()
    }
}