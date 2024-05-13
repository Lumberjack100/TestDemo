package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.formatDoubleValue
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
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

                if (stateInfo.sn != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备SN",
                            value = stateInfo.sn
                        )
                    )
                }
                if (stateInfo.imei != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备IMEI",
                            value = stateInfo.imei
                        )
                    )
                }
                if (stateInfo.imsi != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备IMSI",
                            value = stateInfo.imsi
                        )
                    )
                }
                if (stateInfo.ccid != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备ICCID",
                            value = stateInfo.ccid
                        )
                    )
                }
                if (stateInfo.hardwareVersion != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "硬件版本",
                            value = stateInfo.hardwareVersion
                        )
                    )
                }
                if (stateInfo.firmwareVersion != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "固件版本",
                            value = stateInfo.firmwareVersion
                        )
                    )
                }
                if (stateInfo.extPowerVolt != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.extPowerVolt.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "电源电压",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "V",
                            colorRes = if (tempValue <= 5) ColorUtils.getColor(R.color.device_offline_platform) else ColorUtils.getColor(
                                R.color.text_color_3AD094
                            )
                        )
                    )
                }
                deviceAbnormalList.clear()
                deviceAbnormalList.addAll(DeviceStatusHelper.checkDeviceAbnormal(stateInfo))
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "设备状态",
                        value = if (deviceAbnormalList.isEmpty()) "正常" else "异常",
                        colorRes = if (deviceAbnormalList.isEmpty()) ColorUtils.getColor(
                            R.color.text_color_3AD094
                        ) else ColorUtils.getColor(R.color.device_offline_platform),
                        isClickable = deviceAbnormalList.isNotEmpty()
                    )
                )
                if (stateInfo.worktime != IOTConstants.NULL_KEY || stateInfo.emmcStorage != IOTConstants.NULL_KEY)
                    groupList.add(DeviceStatusInfoGroupItem("运行数据"))
                if (stateInfo.worktime != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.worktime.toIntOrNull() ?: 0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "运行时间(小时)",
                            value = decimalFormat.format(tempValue / 3600)
                        )
                    )
                }
                if (stateInfo.emmcStorage != IOTConstants.NULL_KEY && stateInfo.emmcFree != IOTConstants.NULL_KEY) {
                    decimalFormat.applyPattern("#.#")
                    val free = stateInfo.emmcFree.replace("MB", "").toDoubleOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: ""
                    val total = stateInfo.emmcStorage.replace("MB", "").toDoubleOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: ""
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