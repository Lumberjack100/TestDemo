package com.shmedo.mcloudapp.ui.page.device.lb20s.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class LB20SGatewayInfoFragment : BaseDeviceStatusInfoFragment() {

    override fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS, "devicetype=1")
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

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备SN",
                    value = stateInfo.sn,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "生产日期",
                    value = stateInfo.productDate,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "系统版本",
                    value = stateInfo.rttVersion,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "硬件版本",
                    value = stateInfo.hardwareVersion,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "固件版本",
                    value = stateInfo.firmwareVersion,
                )
                stateInfo.lora.notNullKey {
                    val camState = if (it.uppercase().contains("OK")) "OK" else "FAIL"
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "LoRa状态",
                            value = camState,
                            textColorRes = if (camState == "OK") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                                R.color.red_F13838
                            )
                        )
                    )
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "LoRa版本",
                    value = stateInfo.loraVersion,
                )
                stateInfo.bt.notNullKey {
                    val camState = if (it.uppercase().contains("OK")) "OK" else "FAIL"
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "蓝牙状态",
                            value = camState,
                            textColorRes = if (camState == "OK") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                                R.color.red_F13838
                            )
                        )
                    )
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "蓝牙版本",
                    value = stateInfo.btVersion,
                )
                stateInfo.radio.notNullKey {
                    val camState = if (it.uppercase().contains("OK")) "OK" else "FAIL"
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "电台状态",
                            value = camState,
                            textColorRes = if (camState == "OK") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                                R.color.red_F13838
                            )
                        )
                    )
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电台版本",
                    value = stateInfo.radioVersion,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "电台EUI",
                    value = stateInfo.radioEUI,
                )
                stateInfo.flash.notNullKey {
                    val camState = if (it.uppercase().contains("OK")) "OK" else "FAIL"
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "flash状态",
                            value = camState,
                            textColorRes = if (camState == "OK") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                                R.color.red_F13838
                            )
                        )
                    )
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备位置",
                    value = stateInfo.location,
                )

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = LB20SGatewayInfoFragment()
    }
}