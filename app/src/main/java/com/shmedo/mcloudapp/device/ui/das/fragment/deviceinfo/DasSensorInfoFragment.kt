package com.shmedo.mcloudapp.device.ui.das.fragment.deviceinfo

import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.IOTRainStation
import com.shmedo.lib.device.base.iot_cmd.enums.SensorErrorType
import com.shmedo.lib.device.base.iot_cmd.model.das.DasSensorStatusInfo
import com.shmedo.lib.device.base.iot_cmd.model.das.DasSubSensorStatusInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.model.GapItem
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/6/11
 * 描述： TODO
 */
class DasSensorInfoFragment : BaseDeviceStatusInfoFragment() {
    override fun queryStatusInfo() {
        binding.recyclerview.models = mutableListOf()

        commandItems.clear()
        var command =
            IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS)
        commandItems.add(command)

        command =
            IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_SENSOR_STATUS, "index=0")
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        if (!isResumed) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_SUB_SENSOR_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询辅传感器状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initInternalSensorData(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_GET_SENSOR_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_SENSOR_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询扩展传感器状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initExternalSensorData(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initInternalSensorData(content: String) {
        launchWithViewLifecycle {
            try {
                if (content.isEmpty() || content == "{}") {
                    return@launchWithViewLifecycle
                }
                val subSensorStatusInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<DasSubSensorStatusInfo>(content)
                } ?: return@launchWithViewLifecycle

                val groupList = mutableListOf<Any>()
                //开关量传感器
                subSensorStatusInfo.io?.let { ioBean ->
                    if (ioBean.type.isNotEmpty()) {
                        groupList.add(DeviceStatusInfoGroupItem("开关量传感器"))
                        when (IOTRainStation.value(ioBean.type)) {
                            IOTRainStation.CLOSE -> {//关闭
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "状态",
                                        value = "未接入",
                                        textColorRes = ColorUtils.getColor(R.color.device_offline_platform)
                                    )
                                )
                            }

                            IOTRainStation.RAIN_OPEN -> {//雨量计
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "状态",
                                        value = "接入",
                                        textColorRes = ColorUtils.getColor(R.color.device_online_platform)
                                    )
                                )
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "雨量值(毫米)",
                                        value = ioBean.vaule
                                    )
                                )
                            }

                            IOTRainStation.ALARM_OPEN -> {//断线报警器
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "状态",
                                        value = "接入",
                                        textColorRes = ColorUtils.getColor(R.color.device_online_platform)
                                    )
                                )
                                groupList.add(
                                    DeviceStatusInfoBasicItem(
                                        name = "断线报警器",
                                        value = if (ioBean.vaule == "1") "断线" else "未断线",
                                        textColorRes = if (ioBean.vaule == "1") ColorUtils.getColor(R.color.device_offline_platform) else ColorUtils.getColor(
                                            R.color.device_online_platform
                                        )
                                    )
                                )
                            }
                        }
                    }
                }

                //数字水位计
                subSensorStatusInfo.vwp?.let { vwpBean ->
                    if (vwpBean.errno.isNotEmpty()) {
                        groupList.add(DeviceStatusInfoGroupItem("数字水位计"))
                        groupList.add(
                            DeviceStatusInfoBasicItem(
                                name = "状态",
                                value = if (vwpBean.errno == "0") "正常" else SensorErrorType.getErrorMessageByCode(
                                    vwpBean.errno
                                ),
                                textColorRes = if (vwpBean.errno == "0") ColorUtils.getColor(
                                    R.color.device_online_platform
                                ) else ColorUtils.getColor(
                                    R.color.device_offline_platform
                                )
                            )
                        )

                        val dataList =
                            vwpBean.vaule.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        if (dataList.isNotEmpty()) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "水深(米)",
                                    value = dataList[0]
                                )
                            )
                        }
                        if (dataList.size >= 2) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "空管距离(米)",
                                    value = dataList[1]
                                )
                            )
                        }
                        if (dataList.size >= 3) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "水温(℃)",
                                    value = dataList[2]
                                )
                            )
                        }
                    }
                }
                //倾角计
                subSensorStatusInfo.mems?.let { memsBean ->
                    if (memsBean.errno.isNotEmpty()) {
                        groupList.add(DeviceStatusInfoGroupItem("倾角计"))
                        groupList.add(
                            DeviceStatusInfoBasicItem(
                                name = "状态",
                                value = if (memsBean.errno == "0") "正常" else SensorErrorType.getErrorMessageByCode(
                                    memsBean.errno
                                ),
                                textColorRes = if (memsBean.errno == "0") ColorUtils.getColor(
                                    R.color.device_online_platform
                                ) else ColorUtils.getColor(
                                    R.color.device_offline_platform
                                )
                            )
                        )
                        val dataList =
                            memsBean.vaule.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        if (dataList.isNotEmpty()) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "X轴角度(°)",
                                    value = dataList[0]
                                )
                            )
                        }
                        if (dataList.size >= 2) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "Y轴角度(°)",
                                    value = dataList[1]
                                )
                            )
                        }
                        if (dataList.size >= 3) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "Z轴角度(°)",
                                    value = dataList[2]
                                )
                            )
                        }
                        if (dataList.size >= 4) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "X轴加速度(mg)",
                                    value = dataList[3]
                                )
                            )
                        }
                        if (dataList.size >= 5) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "Y轴加速度(mg)",
                                    value = dataList[4]
                                )
                            )
                        }
                        if (dataList.size >= 6) {
                            groupList.add(
                                DeviceStatusInfoBasicItem(
                                    name = "Z轴加速度(mg)",
                                    value = dataList[5]
                                )
                            )
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

    private fun initExternalSensorData(content: String) {
        launchWithViewLifecycle {
            try {
                val dataList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<DasSensorStatusInfo>>(content)
                }
                if (dataList.isNullOrEmpty()) {
                    if (binding.recyclerview.models?.isEmpty() == true)
                        binding.refreshLayout.showEmpty()

                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                binding.recyclerview.mutable.add(DeviceStatusInfoGroupItem("扩展传感器"))
                dataList.forEach { sensorInfo ->
                    sensorInfo.valueList =
                        sensorInfo._val.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    binding.recyclerview.mutable.add(sensorInfo)
                    binding.recyclerview.mutable.add(
                        GapItem(
                            height = ConvertUtils.dp2px(8f)
                        )
                    )
                }

                binding.recyclerview.bindingAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = DasSensorInfoFragment()
    }
}