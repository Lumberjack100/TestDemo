package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.ext.notNullKey
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class UProductBaseInfoFragment : BaseDeviceStatusInfoFragment() {
    private val deviceAbnormalList: ArrayList<String> = ArrayList()

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

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备SN",
                    value = stateInfo.sn,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备IMEI",
                    value = stateInfo.imei,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备IMSI",
                    value = stateInfo.imsi,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备ICCID",
                    value = stateInfo.ccid,
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
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "外部供电电压",
                    value = stateInfo.extPowerVolt,
                    defaultValue = "0",
                    thresHold = 5.0,
                    digit = 2,
                    unit = "V",
                )
                deviceAbnormalList.clear()
                deviceAbnormalList.addAll(DeviceStatusHelper.checkDeviceAbnormal(stateInfo))
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "设备状态",
                        value = if (deviceAbnormalList.isEmpty()) "正常" else "异常",
                        textColorRes = if (deviceAbnormalList.isEmpty()) ColorUtils.getColor(
                            R.color.text_color_3AD094
                        ) else ColorUtils.getColor(R.color.device_offline_platform),
                        isClickable = deviceAbnormalList.isNotEmpty()
                    )
                )
                if (stateInfo.worktime != IOTConstants.NULL_KEY || stateInfo.emmcStorage != IOTConstants.NULL_KEY)
                    groupList.add(DeviceStatusInfoGroupItem("运行数据"))

                stateInfo.worktime.notNullKey {
                    val tempValue = it.toDoubleOrNull()?.div(3600) ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "运行时间",
                            value = DeviceStatusInfoProcessor.formatDoubleValue(
                                tempValue.toString(),
                                "0",
                                1
                            ) + " 小时"
                        )
                    )
                }

                if (stateInfo.emmcStorage != IOTConstants.NULL_KEY && stateInfo.emmcFree != IOTConstants.NULL_KEY) {
                    val free = DeviceStatusInfoProcessor.formatDoubleValue(
                        stateInfo.emmcFree.replace(
                            "MB",
                            ""
                        ), "0", 1
                    )
                    val total = DeviceStatusInfoProcessor.formatDoubleValue(
                        stateInfo.emmcStorage.replace(
                            "MB",
                            ""
                        ), "0", 1
                    )
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "存储状态",
                            value = "${free}/${total}MB"
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

    override fun processItemClick(item: DeviceStatusInfoBasicItem) {
        if (item.name == "设备状态" && item.value == "异常") {
            showErrorModulesInfoDialog()
        }
    }

    private fun showErrorModulesInfoDialog() {
        if (deviceAbnormalList.isEmpty()) {
            Toaster.show("设备异常信息为空")
            return
        }
        XPopup.Builder(context)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .enableDrag(false)
            .asCenterList(
                "异常信息", deviceAbnormalList.toTypedArray(),
                null, -1,
                null, 0, R.layout.custom_xpopup_adapter_abnormal_info
            )
            .show()
    }

    companion object {
        fun newInstance() = UProductBaseInfoFragment()
    }
}