package com.shmedo.mcloudapp.device.ui.m20.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
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

class M20BaseInfoFragment : BaseDeviceStatusInfoFragment() {
    private val deviceAbnormalList: ArrayList<String> = ArrayList()

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
                    name = "工作模式",
                    value = if (stateInfo.workMode.contains("1")) "基准站" else "移动站",
                )
                stateInfo.self_check.notNullKey {
                    deviceAbnormalList.clear()
                    deviceAbnormalList.addAll(DeviceStatusHelper.checkDeviceAbnormal(it))
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
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "外接电源电压",
                    value = stateInfo.ext_power_volt,
                    defaultValue = "0",
                    thresHold = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "太阳能板电压",
                    value = stateInfo.solar_volt,
                    defaultValue = "0",
                    thresHold = 5.0,
                    digit = 2,
                    unit = "V",
                )
                if (stateInfo.worktime != IOTConstants.NULL_KEY || stateInfo.emmc_storage != IOTConstants.NULL_KEY)
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
                stateInfo.emmc_storage.notNullKey {
                    val storages = it.replace("MB", "").split(",")
                    if (storages.size == 2) {
                        val free = DeviceStatusInfoProcessor.formatDoubleValue(
                            storages[0], "0", 1
                        )
                        val total = DeviceStatusInfoProcessor.formatDoubleValue(
                            storages[1], "0", 1
                        )
                        groupList.add(
                            DeviceStatusInfoBasicItem(
                                name = "存储状态",
                                value = "${free}/${total} MB"
                            )
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

    override fun processItemClick(item: DeviceStatusInfoBasicItem) {
        if (item.isClickable && item.name == "设备状态" && item.value == "异常") {
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
        fun newInstance() = M20BaseInfoFragment()
    }
}