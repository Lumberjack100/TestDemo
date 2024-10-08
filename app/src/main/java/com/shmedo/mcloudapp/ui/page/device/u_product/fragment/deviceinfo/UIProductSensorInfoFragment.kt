package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.cmd.base.iot_cmd.model.u_product.URCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc: 倾斜仪传感器状态
 *
 */
class UIProductSensorInfoFragment : BaseDeviceStatusInfoFragment() {

    override fun queryStatusInfo() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }


    override fun initStatusInfo(content: String) {
        val groupList = mutableListOf<Any>()

        groupList.add(DeviceStatusInfoGroupItem("倾角计"))
        if (content.contains("attach_data")) {
            initStatusInfo2(content)
        } else {
            initStatusInfo1(content)
        }
    }

    private fun initStatusInfo1(content: String) {
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
                val groupList = mutableListOf<Any>()

                val camState =
                    if (stateInfo.scl != IOTConstants.NULL_KEY && stateInfo.scl.uppercase()
                            .contains("OK")
                    )
                        "正常" else "异常"
                groupList.add(
                    DeviceStatusInfoBasicItem(
                        name = "倾角MEMS状态",
                        value = camState,
                        textColorRes = if (camState == "正常") ColorUtils.getColor(R.color.green_00B26B) else ColorUtils.getColor(
                            R.color.error_FF4400
                        )
                    )
                )

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initStatusInfo2(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<URCurrentStateInfo>(content)
                }
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                val uRSensorInfoList = stateInfo.attach_data
                if (uRSensorInfoList.isNullOrEmpty()) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                uRSensorInfoList.forEach { info ->
                    when (info.key) {
                        "initAngle" -> {
                            info.value.notNullKey { value ->
                                //根据逗号分隔
                                val initAngle =
                                    value.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                if (initAngle.isNotEmpty()) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "X轴初始角度值",
                                        value = initAngle[0],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                if (initAngle.size > 1) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Y轴初始角度值",
                                        value = initAngle[1],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                if (initAngle.size > 2) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Z轴初始角度值",
                                        value = initAngle[2],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                groupList.add(GapItem())
                            }
                        }

                        "angle" -> {
                            info.value.notNullKey { value ->
                                //根据逗号分隔
                                val angle =
                                    value.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                if (angle.isNotEmpty()) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "X轴当前角度值",
                                        value = angle[0],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                if (angle.size > 1) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Y轴当前角度值",
                                        value = angle[1],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                if (angle.size > 2) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Z轴当前角度值",
                                        value = angle[2],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "°",
                                    )
                                }
                                groupList.add(GapItem())
                            }
                        }

                        "acc" -> {
                            info.value.notNullKey { value ->
                                //根据逗号分隔
                                val acc = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                if (acc.isNotEmpty()) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "X轴加速度值",
                                        value = acc[0],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "mg",
                                    )
                                }
                                if (acc.size > 1) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Y轴加速度值",
                                        value = acc[1],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "mg",
                                    )
                                }
                                if (acc.size > 2) {
                                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                                        groupList,
                                        name = "Z轴加速度值",
                                        value = acc[2],
                                        defaultValue = "0",
                                        digit = 2,
                                        unit = "mg",
                                    )
                                }
                                groupList.add(GapItem())
                            }
                        }

                        "worktime" -> {
                            info.value.toIntOrNull()?.let {
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "连续运行时间",
                                        value = DeviceStatusInfoProcessor.millis2FitTimeSpan(
                                            it * 1000L,
                                            3
                                        )
                                    )
                                )
                            }
                        }
                    }
                }

                binding.recyclerview.mutable.addAll(groupList)
                binding.recyclerview.bindingAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }

    }

    companion object {
        fun newInstance() = UIProductSensorInfoFragment()
    }
}