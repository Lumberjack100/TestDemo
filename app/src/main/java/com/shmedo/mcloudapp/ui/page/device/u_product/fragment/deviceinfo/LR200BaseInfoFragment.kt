package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

class LR200BaseInfoFragment : BaseDeviceStatusInfoFragment() {

    override fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo =
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content)
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备SN",
                    value = stateInfo.sN,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备IMEI",
                    value = stateInfo.iMEI,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备IMSI",
                    value = stateInfo.iMSI,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备ICCID",
                    value = stateInfo.cCID,

                    )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "硬件版本",
                    value = stateInfo.hw_version,

                    )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "固件版本",
                    value = stateInfo.sw_version,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备位置",
                    value = stateInfo.location,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "测量范围",
                    value = stateInfo.lF_range,
                    unit = "mm",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "电池电量",
                    value = stateInfo.volt_percent.replace("%", ""),
                    defaultValue = "0",
                    downLimitValue = 10.0,
                    digit = 2,
                    unit = "%",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "内部温度",
                    value = stateInfo.temp,
                    defaultValue = "0",
                    digit = 2,
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "内部湿度",
                    value = stateInfo.humidity,
                    defaultValue = "0",
                    digit = 2,
                    unit = "%",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "外部温度",
                    value = stateInfo.temp_out,
                    defaultValue = "0",
                    digit = 2,
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "外部湿度",
                    value = stateInfo.humidity_out,
                    defaultValue = "0",
                    digit = 2,
                    unit = "%",
                )
                stateInfo._4g_signal.notNullKey {
                    groupList.add(
                        DeviceStatusInfoSignalItem(
                            name = "4G信号强度",
                            signalValue = if (it <= 0)
                                it
                            else
                                it * 2 - 113
                        )
                    )
                }
                stateInfo.self_check.notNullKey {
                    val camState = if (it.uppercase().contains("LORA:1")) "有" else "无"
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "LoRa功能",
                            value = camState,
                            textColorRes = if (camState == "有") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                                R.color.red_F13838
                            )
                        )
                    )
                }

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = LR200BaseInfoFragment()
    }
}